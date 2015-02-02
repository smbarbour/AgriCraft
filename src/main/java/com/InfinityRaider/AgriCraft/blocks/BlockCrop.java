package com.InfinityRaider.AgriCraft.blocks;

import com.InfinityRaider.AgriCraft.AgriCraft;
import com.InfinityRaider.AgriCraft.compatibility.applecore.AppleCoreHelper;
import com.InfinityRaider.AgriCraft.handler.ConfigurationHandler;
import com.InfinityRaider.AgriCraft.init.Items;
import com.InfinityRaider.AgriCraft.items.ItemCrop;
import com.InfinityRaider.AgriCraft.items.ItemDebugger;
import com.InfinityRaider.AgriCraft.reference.Constants;
import com.InfinityRaider.AgriCraft.reference.Names;
import com.InfinityRaider.AgriCraft.tileentity.TileEntityCrop;
import com.InfinityRaider.AgriCraft.utility.SeedHelper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFarmland;
import net.minecraft.block.IGrowable;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.particle.EffectRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemSeeds;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BlockCrop extends BlockModPlant implements ITileEntityProvider, IGrowable {

    // TODO: textures in 1.8?
    // @SideOnly(Side.CLIENT)
    // private IIcon[] weedIcons;

    public BlockCrop() {
        super(Blocks.farmland, null, null, 0, 0, 6);
        this.isBlockContainer = true;
        //set the bounding box dimensions
        this.maxX = Constants.unit*14;
        this.minX = Constants.unit*2;
        this.maxZ = this.maxX;
        this.minZ = this.minX;
        this.maxY = Constants.unit*13;
        this.minY = 0;
    }

    //this makes a new tile entity every time you place the block
    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileEntityCrop();
    }

    @Override
    public void updateTick(World world, BlockPos pos, IBlockState state, Random rnd) {
        TileEntityCrop crop = (TileEntityCrop) world.getTileEntity(pos);
        if(crop.hasPlant()) {
            Event.Result allowGrowthResult = AppleCoreHelper.validateGrowthTick(this, world, pos, rnd);
            if (allowGrowthResult != Event.Result.DENY) {
                int age = getAge(world, pos);
                if (age < 7 && crop.isFertile()) {
                    double multiplier = 1.0 + (crop.growth + 0.00) / 10;
                    float growthRate = (float) SeedHelper.getBaseGrowth((ItemSeeds) crop.seed, crop.seedMeta);
                    boolean shouldGrow = (rnd.nextDouble()<=(growthRate * multiplier)/100);
                    if (shouldGrow) {
                        world.setBlockState(pos, state.withProperty(AGE, Integer.valueOf(age + 1)), 2);
                        AppleCoreHelper.announceGrowthTick(this, world, pos);
                    }
                }
            }
        } else if(crop.weed) {
            Event.Result allowGrowthResult = AppleCoreHelper.validateGrowthTick(this, world, pos, rnd);
            if (allowGrowthResult != Event.Result.DENY) {
                int age = getAge(world, pos);
                if (age < 7) {
                    double multiplier = 1.0 + (10 + 0.00) / 10;
                    float growthRate = (float) Constants.growthTier1;
                    boolean shouldGrow = (rnd.nextDouble()<=(growthRate * multiplier)/100);
                    if (shouldGrow) {
                        world.setBlockState(pos, state.withProperty(AGE, Integer.valueOf(age + 1)), 2);
                        AppleCoreHelper.announceGrowthTick(this, world, pos);
                    }
                }
                else {
                    if(ConfigurationHandler.enableWeeds) {
                        crop.spreadWeed();
                    }
                }
            }
        } else {
            //10%chance to spawn weeds
            if(ConfigurationHandler.enableWeeds && Math.random()<0.10) {
                crop.spawnWeed();
            }
            else if(crop.crossCrop) {
                crop.crossOver();
            }
        }
    }

    //this harvests the crop
    public boolean harvest(World world, BlockPos pos) {
        if(!world.isRemote) {
            boolean update = false;
            TileEntityCrop crop = (TileEntityCrop) world.getTileEntity(pos);
            if(crop.weed) {
                crop.clearWeed();   //update is not needed because it is called in the clearWeed() method
            }else if(crop.crossCrop) {
                crop.crossCrop = false;
                dropBlockAsItem(world, pos, world.getBlockState(pos), 0);
                update = true;
            } else if(crop.isMature()) {
                crop.getWorld().setBlockState(pos, crop.getWorld().getBlockState(pos).withProperty(AGE, 2), 2);
                update = true;
                ArrayList<ItemStack> drops = SeedHelper.getPlantFruits((ItemSeeds) crop.seed, world, pos.getX(), pos.getY(), pos.getZ(), crop.gain, crop.seedMeta);
                // TODO: Figure out how drops work in 1.8
                for (ItemStack drop : drops) {
                    // this.dropBlockAsItem(world, x, y, z, drop);
                    dropBlockAsItem(world, pos, world.getBlockState(pos), 0);
                }
            }
            if (update) {
                crop.markDirtyAndMarkForUpdate();
            }
            return update;
        }
        return false;
    }

    public void setCrossCrop(World world, BlockPos pos, EntityPlayer player) {
        if(!world.isRemote) {
            boolean update = false;
            TileEntityCrop crop = (TileEntityCrop) world.getTileEntity(pos);
            if(!crop.crossCrop && !crop.hasPlant()) {
                crop.crossCrop=true;
                player.getCurrentEquippedItem().stackSize = player.capabilities.isCreativeMode?player.getCurrentEquippedItem().stackSize:player.getCurrentEquippedItem().stackSize - 1;
                update = true;
            }
            else {
                this.harvest(world, pos);
            }
            if (update) {
                crop.markDirtyAndMarkForUpdate();
            }
        }
    }

    public void plantSeed(World world, BlockPos pos, EntityPlayer player) {
        if(!world.isRemote) {
            TileEntityCrop crop = (TileEntityCrop) world.getTileEntity(pos);
            //is the cropEmpty a crosscrop or does it already have a plant
            if (crop.crossCrop || crop.hasPlant() || !(player.getCurrentEquippedItem().getItem() instanceof ItemSeeds)) {
                return;
            }
            //the seed can be planted here
            else {
                ItemStack stack = player.getCurrentEquippedItem();
                Block blockBelow = world.getBlockState(pos.down()).getBlock();
                int moisture = (Integer) world.getBlockState(pos.down()).getValue(BlockFarmland.MOISTURE);
                if (!SeedHelper.isValidSeed((ItemSeeds) stack.getItem(), stack.getItemDamage()) || !SeedHelper.isCorrectSoil(blockBelow, moisture, (ItemSeeds) stack.getItem(), stack.getItemDamage())) {
                    return;
                }
                //get NBT data from the seeds
                if (player.getCurrentEquippedItem().getTagCompound() != null && player.getCurrentEquippedItem().getTagCompound().hasKey(Names.NBT.growth)) {
                    //NBT data was found: copy data to plant
                    crop.setPlant(stack.getTagCompound().getInteger(Names.NBT.growth), stack.getTagCompound().getInteger(Names.NBT.gain), stack.getTagCompound().getInteger(Names.NBT.strength), stack.getTagCompound().getBoolean(Names.NBT.analyzed), (ItemSeeds) stack.getItem(), stack.getItemDamage());
                } else {
                    //NBT data was not initialized: set defaults
                    crop.setPlant(Constants.defaultGrowth, Constants.defaultGain, Constants.defaultStrength, false, (ItemSeeds) stack.getItem(), stack.getItemDamage());
                }
                //take one seed away if the player is not in creative
                player.getCurrentEquippedItem().stackSize = player.capabilities.isCreativeMode ? player.getCurrentEquippedItem().stackSize : player.getCurrentEquippedItem().stackSize - 1;
            }
            crop.markDirtyAndMarkForUpdate();
        }
    }

    //This gets called when the block is right clicked (player uses the block)
    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumFacing side, float hitX, float hitY, float hitZ) {
        //only make things happen serverside
        if(!world.isRemote) {
            if(player.isSneaking()) {
                this.harvest(world, pos);
            }
            else if(player.getCurrentEquippedItem()==null) {
                //harvest operation
                this.harvest(world, pos);
            }
            //check to see if the player clicked with crops (crosscrop attempt)
            else if(player.getCurrentEquippedItem().getItem()==Items.crops) {
                this.setCrossCrop(world, pos, player);
            }
            //check to see if the player wants to use bonemeal
            else if(player.getCurrentEquippedItem().getItem()==net.minecraft.init.Items.dye && player.getCurrentEquippedItem().getItemDamage()==15) {
                return false;
            }
            //allow the debugger to be used
            else if(player.getCurrentEquippedItem().getItem() instanceof ItemDebugger) {
                return false;
            }
            else {
                //harvest operation
                this.harvest(world, pos);
                //check to see if clicked with seeds
                if(player.getCurrentEquippedItem().getItem() instanceof ItemSeeds) {
                    this.plantSeed(world, pos, player);
                }
            }
        }
        //Returning true will prevent other things from happening
        return true;
    }

    //This gets called when the block is left clicked (player hits the block)
    @Override
    public void onBlockClicked(World world, BlockPos pos, EntityPlayer player) {
        if(!world.isRemote) {
            if(!player.capabilities.isCreativeMode) {       //drop items if the player is not in creative
                dropBlockAsItem(world, pos, world.getBlockState(pos), 0);
            }
            world.setBlockToAir(pos);
            world.removeTileEntity(pos);
        }
    }

    // TODO: Figure out block drops on 1.8
     /*
    //item drops
    @Override
    public void dropBlockAsItemWithChance(World world, int x, int y, int z, int meta, float f, int i) {
        if(!world.isRemote) {
            TileEntityCrop crop = (TileEntityCrop) world.getTileEntity(x, y, z);
            if (crop != null) {
                ArrayList<ItemStack> drops = new ArrayList<ItemStack>();
                if (crop.crossCrop) {
                    drops.add(new ItemStack(Items.crops, 2));
                } else {
                    drops.add(new ItemStack(Items.crops, 1));
                    if (crop.hasPlant()) {
                        drops.add(crop.getSeedStack());
                        if (this.isMature(world, pos)) {
                            drops.addAll(SeedHelper.getPlantFruits((ItemSeeds) crop.seed, world, x, y, z, crop.gain, crop.seedMeta));
                        }
                    }
                }
                for (ItemStack drop : drops) {
                    this.dropBlockAsItem(world, x, y, z, drop);
                }
            }
        }
    } */

    @Override
    public boolean canUseBonemeal(World world, Random rand, BlockPos pos, IBlockState state) {
        TileEntityCrop crop = (TileEntityCrop) world.getTileEntity(pos);
        if(crop.crossCrop) {
            return ConfigurationHandler.bonemealMutation;
        }
        if(crop.hasPlant()) {
            if(SeedHelper.getSeedTier((ItemSeeds) crop.seed, crop.seedMeta)<4) {
                return !this.isMature(world, pos);
            }
        }
        return false;
    }

    @Override
    public void grow(World world, Random rand, BlockPos pos, IBlockState state) {
        TileEntityCrop crop = (TileEntityCrop) world.getTileEntity(pos);
        if(crop.hasPlant() && this.isFertile(world, pos)) {
            super.grow(world, rand, pos, state);
        }
        else if(crop.crossCrop && ConfigurationHandler.bonemealMutation) {
            crop.crossOver();
        }
    }

    @Override
    public void onNeighborBlockChange(World world, BlockPos pos, IBlockState state, Block neighborBlock) {
        //check if crops can stay
        if (!this.canBlockStay(world, pos, state)) {
            //the crop will be destroyed
            dropBlockAsItem(world, pos, state, 0);
            world.setBlockToAir(pos);
            world.removeTileEntity(pos);
        }
    }

    @Override
    public boolean canBlockStay(World world, BlockPos pos, IBlockState state) {
        Block soil = world.getBlockState(pos.down()).getBlock();
        int moisture = (Integer) world.getBlockState(pos.down()).getValue(BlockFarmland.MOISTURE);
        return ItemCrop.isSoilValid(soil, moisture);
    }

    //see if the block can grow
    @Override
    public boolean isFertile(World world, BlockPos pos) {
        TileEntity te = world.getTileEntity(pos);
        return te != null && te instanceof TileEntityCrop && ((TileEntityCrop) te).isFertile();
    }

    //get a list with items dropped by the the crop
    @Override
    public List<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
        List<ItemStack> items = new ArrayList<ItemStack>();
        if (world.getTileEntity(pos) != null && world.getTileEntity(pos) instanceof TileEntityCrop) {
            TileEntityCrop crop = (TileEntityCrop) world.getTileEntity(pos);
            if (crop.crossCrop) {
                items.add(new ItemStack(Items.crops, 2));
            } else {
                items.add(new ItemStack(Items.crops, 1));
            }
            if (crop.hasPlant()) {
                ItemStack seedStack = crop.getSeedStack().copy();
                items.add(seedStack);
                if(crop.isMature()) {
                    BlockPos cropPos = crop.getPos();
                    items.addAll(SeedHelper.getPlantFruits((ItemSeeds) crop.seed, crop.getWorld(),
                            cropPos.getX(), cropPos.getY(), cropPos.getZ(), crop.gain, crop.seedMeta));
                }
            }
        }
        return items;
    }

    //return the crops item if this block is called
    @Override
    @SideOnly(Side.CLIENT)
    public Item getItem(World worldIn, BlockPos pos) {
        return Items.crops;
    }

    //rendering stuff
    @Override public int getRenderType() {
        return AgriCraft.proxy.getRenderId(Constants.cropId);
    }

    @Override
    public boolean isOpaqueCube() {
        return false; //tells minecraft that this is not a block (no levers can be placed on it, it's transparent, ...)
    }

    @Override
    public boolean shouldSideBeRendered(IBlockAccess worldIn, BlockPos pos, EnumFacing side) {
        return true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean addHitEffects(World worldObj, MovingObjectPosition target, EffectRenderer effectRenderer) {
        return false; //no particles when this block gets hit
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean addDestroyEffects(World world, BlockPos pos, EffectRenderer effectRenderer) {
        return false; //no particles when destroyed
    }

    // TODO: textures in 1.8?
    /*
    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister reg) {
        this.blockIcon = reg.registerIcon(this.getUnlocalizedName().substring(this.getUnlocalizedName().indexOf('.') + 1));
        this.weedIcons = new IIcon[4];
        for(int i=0;i<weedIcons.length;i++) {
            this.weedIcons[i] = reg.registerIcon(this.getUnlocalizedName().substring(this.getUnlocalizedName().indexOf('.') + 1) + "WeedTexture" + (i + 1));
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int meta) {
        return this.blockIcon;
    }

    @SideOnly(Side.CLIENT)
    public IIcon getWeedIcon(int meta) {
        int index = 0;
        switch(meta) {
            case 0:index = 0;break;
            case 1:index = 0;break;
            case 2:index = 1;break;
            case 3:index = 1;break;
            case 4:index = 1;break;
            case 5:index = 2;break;
            case 6:index = 2;break;
            case 7:index = 3;break;
        }
        return this.weedIcons[index];
    }
    */

    @Override
    public boolean onBlockEventReceived(World world, BlockPos pos, IBlockState state, int eventID, int eventParam) {
        super.onBlockEventReceived(world, pos, state, eventID, eventParam);
        TileEntity tileEntity = world.getTileEntity(pos);
        return tileEntity != null && tileEntity.receiveClientEvent(eventID, eventParam);
    }

    /** Returns the AGE property of the BlockState at the given position */
    private int getAge(World world, BlockPos pos) {
        return getMetaFromState(world.getBlockState(pos));
    }
}
