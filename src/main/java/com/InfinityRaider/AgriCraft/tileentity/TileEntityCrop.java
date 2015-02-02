package com.InfinityRaider.AgriCraft.tileentity;

import com.InfinityRaider.AgriCraft.blocks.BlockCrop;
import com.InfinityRaider.AgriCraft.blocks.BlockModPlant;
import com.InfinityRaider.AgriCraft.farming.mutation.Mutation;
import com.InfinityRaider.AgriCraft.farming.mutation.MutationHandler;
import com.InfinityRaider.AgriCraft.handler.ConfigurationHandler;
import com.InfinityRaider.AgriCraft.reference.Names;
import com.InfinityRaider.AgriCraft.utility.OreDictHelper;
import com.InfinityRaider.AgriCraft.utility.RenderHelper;
import com.InfinityRaider.AgriCraft.utility.SeedHelper;
import com.InfinityRaider.AgriCraft.utility.interfaces.IDebuggable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBush;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.item.Item;
import net.minecraft.item.ItemSeeds;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public class TileEntityCrop extends TileEntityAgricraft implements IDebuggable{
    public int growth=0;
    public int gain=0;
    public int strength=0;
    public boolean analyzed=false;
    public boolean crossCrop=false;
    public boolean weed=false;
    public IPlantable seed = null;
    public int seedMeta = 0;

    //this saves the data on the tile entity
    @Override
    public void writeToNBT(NBTTagCompound tag) {
        tag.setShort(Names.NBT.growth, (short) growth);
        tag.setShort(Names.NBT.gain, (short) gain);
        tag.setShort(Names.NBT.strength, (short) strength);
        tag.setBoolean(Names.NBT.analyzed, analyzed);
        tag.setBoolean(Names.NBT.crossCrop,crossCrop);
        tag.setBoolean(Names.NBT.weed, weed);
        if(this.seed!=null) {
            tag.setString(Names.Objects.seed, this.getSeedString());
            tag.setShort(Names.NBT.meta, (short) seedMeta);
        }
        super.writeToNBT(tag);
    }

    //this loads the saved data for the tile entity
    @Override
    public void readFromNBT(NBTTagCompound tag) {
        this.growth=tag.getInteger(Names.NBT.growth);
        this.gain=tag.getInteger(Names.NBT.gain);
        this.strength=tag.getInteger(Names.NBT.strength);
        this.analyzed=tag.hasKey(Names.NBT.analyzed) && tag.getBoolean(Names.NBT.analyzed);
        this.crossCrop=tag.getBoolean(Names.NBT.crossCrop);
        this.weed=tag.getBoolean(Names.NBT.weed);
        if(tag.hasKey(Names.Objects.seed) && tag.hasKey(Names.NBT.meta)) {
            this.setSeed(tag.getString(Names.Objects.seed));
            this.seedMeta = tag.getInteger(Names.NBT.meta);
        }
        else {
            this.seed=null;
            this.seedMeta=0;
        }
        super.readFromNBT(tag);
    }

    //the code that makes the crop cross with neighboring crops
    public void crossOver() {
            //flag to check if the crop needs to update
            boolean change = false;
            //possible new plant
            ItemSeeds result=null;
            int resultMeta=0;
            int mutationId=0;
            Block req=null;
            int reqMeta = 0;
            double chance=0;
            //find neighbours
            TileEntityCrop[] neighbours = this.findNeighbours();
            //find out the new plant
            if (Math.random() > ConfigurationHandler.mutationChance) {
                int index = (int) Math.floor(Math.random() * neighbours.length);
                if (neighbours[index]!=null && neighbours[index].seed!=null && neighbours[index].isMature()) {
                    result = (ItemSeeds) neighbours[index].seed;
                    resultMeta = neighbours[index].seedMeta;
                    chance = SeedHelper.getSpreadChance(result, resultMeta);
                }
            } else {
                Mutation[] crossOvers = MutationHandler.getCrossOvers(neighbours);
                if (crossOvers!=null && crossOvers.length>0) {
                    int index = (int) Math.floor(Math.random()*crossOvers.length);
                    if(crossOvers[index].result.getItem()!=null) {
                        result = (ItemSeeds) crossOvers[index].result.getItem();
                        resultMeta = crossOvers[index].result.getItemDamage();
                        mutationId = crossOvers[index].id;
                        req = crossOvers[index].requirement;
                        reqMeta = crossOvers[index].requirementMeta;
                        chance = crossOvers[index].chance;
                    }
                }
            }
            //try to set the new plant
            if(result!=null && SeedHelper.isValidSeed(result, resultMeta) && this.canMutate(result, resultMeta, mutationId, req, reqMeta)) {
                if(Math.random()<chance) {
                    this.crossCrop = false;
                    int[] stats = MutationHandler.getStats(neighbours);
                    this.setPlant(stats[0], stats[1], stats[2], false, result, resultMeta);
                    change = true;
                }
            }
            //update the tile entity on a change
            if (change) {
                markDirtyAndMarkForUpdate();
            }
        
    }

    //finds neighbouring crops
    private TileEntityCrop[] findNeighbours() {
        TileEntityCrop[] neighbours = new TileEntityCrop[4];
        neighbours[0] = (this.worldObj.getTileEntity(pos.west()) instanceof TileEntityCrop) ? (TileEntityCrop) this.worldObj.getTileEntity(pos.west()) : null;
        neighbours[1] = (this.worldObj.getTileEntity(pos.east()) instanceof TileEntityCrop) ? (TileEntityCrop) this.worldObj.getTileEntity(pos.east()) : null;
        neighbours[2] = (this.worldObj.getTileEntity(pos.north()) instanceof TileEntityCrop) ? (TileEntityCrop) this.worldObj.getTileEntity(pos.north()) : null;
        neighbours[3] = (this.worldObj.getTileEntity(pos.south()) instanceof TileEntityCrop) ? (TileEntityCrop) this.worldObj.getTileEntity(pos.south()) : null;
        return neighbours;
    }

    //checks if a plant can mutate
    private boolean canMutate(ItemSeeds seed, int seedMeta, int id, Block req, int reqMeta) {
        if(this.canGrow(seed, seedMeta)) {
            //id = 0: no requirement
            //id = 1: block below farmland has to be the req block
            //id = 2: block near has to be the req block
            switch(id) {
                case 0: return true;
                case 1:
                    IBlockState state = worldObj.getBlockState(pos.down(2));
                    return state.getBlock() == req && state.getBlock().getMetaFromState(state) == reqMeta;
                case 2: return isBlockNear(req, reqMeta);
            }
        }
        return false;
    }

    //checks if a given block is near
    private boolean isBlockNear(Block block, int meta) {
        for(int x=-3;x<=3;x++) {
            for(int y=0;y<=3;y++) {
                for(int z=-3;z<=3;z++) {
                    IBlockState state = worldObj.getBlockState(pos.add(x, y, z));
                    if (state.getBlock() == block && state.getBlock().getMetaFromState(state) == meta) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    //spawns weed in the crop
    public void spawnWeed() {
        this.crossCrop=false;
        this.clearPlant();
        this.weed=true;
        this.markDirtyAndMarkForUpdate();
    }

    //spread the weed
    public void spreadWeed() {
        TileEntityCrop[] neighbours = this.findNeighbours();
        for(TileEntityCrop crop:neighbours) {
            if(crop!=null && (!crop.weed) && Math.random()<crop.getWeedSpawnChance()) {
                crop.spawnWeed();
            }
        }
    }

    //clear the weed
    public void clearWeed() {
        weed = false;
        worldObj.setBlockState(pos, worldObj.getBlockState(pos).getBlock().getDefaultState(), 2);
        markDirtyAndMarkForUpdate();
    }

    //weed spawn chance
    private double getWeedSpawnChance() {
        if(this.hasPlant()) {
            return ConfigurationHandler.weedsWipePlants?((double) (10 - this.strength))/10:0;
        }
        else {
            return this.weed ? 0 : 1;
        }
    }

    //sets the plant in the crop
    public void setPlant(int growth, int gain, int strength, boolean analyzed, IPlantable seed, int seedMeta) {
        if( (!this.crossCrop) && (!this.hasPlant())) {
            this.growth = growth;
            this.gain = gain;
            this.strength = strength;
            this.seed = seed;
            this.analyzed = analyzed;
            this.seedMeta = seedMeta;
            worldObj.setBlockState(pos, worldObj.getBlockState(pos).getBlock().getDefaultState(), 3);
            this.markDirtyAndMarkForUpdate();
        }
    }

    //clears the plant in the crop
    public void clearPlant() {
        if(!this.crossCrop) {
            this.growth = 0;
            this.gain = 0;
            this.strength = 0;
            this.seed = null;
            this.seedMeta = 0;
            this.analyzed = false;
            this.weed = false;
            worldObj.setBlockState(pos, worldObj.getBlockState(pos).getBlock().getDefaultState(), 3);
            this.markDirtyAndMarkForUpdate();
        }
    }

    @Override
    public boolean receiveClientEvent(int id, int value) {
        if (worldObj.isRemote && id == 1) {
            worldObj.markBlockForUpdate(pos);
            worldObj.notifyLightSet(pos);
            Minecraft.getMinecraft().renderGlobal.markBlockForUpdate(pos);
        }
        return true;
    }

    //check to see if there is a plant here
    public boolean hasPlant() {
        return seed != null && seed.getPlant(this.worldObj, pos) != null;
    }

    //check if the crop is fertile
    public boolean isFertile() {
        return this.canGrow((ItemSeeds) this.seed, this.seedMeta);
    }

    //check the block if the plant is mature
    public boolean isMature() {
        Block block = worldObj.getBlockState(pos).getBlock();
        boolean success = !worldObj.isRemote && block != null && block instanceof BlockCrop;
        success = success && ((BlockCrop) block).isMature(worldObj, pos);
        return success;
    }

    //check if the seed can grow
    private boolean canGrow(ItemSeeds seed, int seedMeta) {
        BlockBush plant = SeedHelper.getPlant(seed);
        IBlockState soilState =  worldObj.getBlockState(pos.down());
        Block soil = soilState.getBlock();
        int soilMeta = soil.getMetaFromState(soilState);
        if(SeedHelper.isCorrectSoil(soil, soilMeta, seed, seedMeta) && worldObj.getLight(pos.up()) > 8) {
            if(plant instanceof BlockModPlant) {
                BlockModPlant blockModPlant = (BlockModPlant) plant;
                IBlockState soilBaseState = worldObj.getBlockState(pos.down(2));
                return blockModPlant.base == null || OreDictHelper.isSameOre(blockModPlant.base, blockModPlant.baseMeta,
                        soilBaseState.getBlock(), soilBaseState.getBlock().getMetaFromState(soilBaseState));
            }
            return true;
        }
        return false;
    }

    public ItemStack getSeedStack() {
        ItemStack seed = new ItemStack((ItemSeeds) this.seed, 1, this.seedMeta);
        NBTTagCompound tag = new NBTTagCompound();
        SeedHelper.setNBT(tag, (short) this.growth, (short) this.gain, (short) this.strength, this.analyzed);
        seed.setTagCompound(tag);
        return seed;
    }

    //a helper method for ItemSeed <-> String conversion for storing seed as a string in NBT
    public String getSeedString() {
        return seed == null ? "none" : Item.itemRegistry.getNameForObject(seed).toString();
    }

    //a helper method for ItemSeed <-> String conversion for storing seed as a string in NBT
    public void setSeed(String input) {
        this.seed = input.equalsIgnoreCase("none")?null:(ItemSeeds) Item.itemRegistry.getObject(input);
    }

    // TODO: textures in 1.8?
    /*
    //get the plant icon
    @SideOnly(Side.CLIENT)
    public IIcon getPlantIcon() {
        IIcon icon = null;
        if(this.hasPlant()) {
            int meta = RenderHelper.plantIconIndex((ItemSeeds) this.seed, this.seedMeta, this.getBlockMetadata());
            icon = SeedHelper.getPlant((ItemSeeds) this.seed).getIcon(0, meta);
        }
        else if(this.weed) {
            icon = ((BlockCrop) this.worldObj.getBlock(this.xCoord, this.yCoord, this.zCoord)).getWeedIcon(this.getBlockMetadata());
        }
        return icon;
    }
    */

    //get the rendertype
    @SideOnly(Side.CLIENT)
    public int getRenderType() {
        int type = -1;
        if(this.hasPlant()) {
            type = RenderHelper.getRenderType((ItemSeeds) this.seed, this.seedMeta);
        }
        else if(this.weed) {
            type = 6;
        }
        return type;
    }

    @Override
    public void addDebugInfo(List<String> list) {
        list.add("CROP:");
        if(this.crossCrop) {
            list.add(" - This is a crosscrop");
        }
        else if(this.hasPlant()) {
            list.add(" - This crop has a plant");
            list.add(" - Seed: " + ((ItemSeeds) this.seed).getUnlocalizedName());
            list.add(" - RegisterName: " + Item.itemRegistry.getNameForObject(this.seed) + ':' + this.seedMeta);
            list.add(" - Plant: " + SeedHelper.getPlant((ItemSeeds) this.seed).getUnlocalizedName());
            list.add(" - Meta: " + this.getBlockMetadata());
            list.add(" - Growth: " + this.growth);
            list.add(" - Gain: " + this.gain);
            list.add(" - Strength: " + this.strength);
            list.add(" - Fertile: " + this.isFertile());
            list.add(" - Mature: " + this.isMature());
        }
        else if(this.weed) {
            list.add(" - This crop has weeds");
            list.add(" - Meta: " + this.getBlockMetadata());
        }
        else {
            list.add(" - This crop has no plant");
        }
    }
}
