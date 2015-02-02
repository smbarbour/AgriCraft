package com.InfinityRaider.AgriCraft.blocks;


import com.InfinityRaider.AgriCraft.farming.CropProduce;
import com.InfinityRaider.AgriCraft.utility.OreDictHelper;
import com.InfinityRaider.AgriCraft.utility.SeedHelper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.IGrowable;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemSeeds;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BlockModPlant extends BlockCrops implements IGrowable {

    private static final Random random = new Random();

    public Block soil;
    public Block base;
    public int baseMeta;
    public CropProduce products = new CropProduce();
    public ArrayList<ItemStack> fruits;
    private ItemSeeds seed;
    public int tier;

    // TODO: textures in 1.8?
    // @SideOnly(Side.CLIENT)
    // private IIcon[] icons;

    private int renderType;
    private boolean isEditable;

    public BlockModPlant(Item fruit) {
        this(null, null, 0, fruit, 0, 1, 6);
    }

    public BlockModPlant(Block soil, Item fruit) {
        this(soil, null, 0, fruit, 0, 1, 6);
    }

    public BlockModPlant(Item fruit, int fruitMeta) {
        this(null, null, 0, fruit, fruitMeta, 1, 6);
    }

    public BlockModPlant(Item fruit, int fruitMeta, int tier) {
        this(null, null, 0, fruit, fruitMeta, tier, 6);
    }

    public BlockModPlant(Item fruit, int fruitMeta, int tier, int renderType) {
        this(null, null, 0, fruit, fruitMeta, tier, renderType);
    }

    public BlockModPlant(Block soil, Block base, Item fruit, int tier, int renderType) {
        this(soil, base, 0, fruit, 0, tier, renderType);
    }

    public BlockModPlant(Block soil, Item fruit, int fruitMeta) {
        this(soil, null, 0, fruit, fruitMeta, 1, 6);
    }

    public BlockModPlant(Block base, int baseMeta, Item fruit, int fruitMeta, int tier, int renderType) {
        this(Blocks.farmland, base, baseMeta, fruit, fruitMeta, tier, renderType);
    }

    public BlockModPlant(Block soil, Block base, Item fruit, int fruitMeta, int tier, int renderType) {
        this(soil, base, 0, fruit, fruitMeta, tier, renderType);
    }

    public BlockModPlant(Block soil, Block base, int baseMeta, Item fruit, int tier, int renderType) {
        this(soil, base, baseMeta, fruit, 0, tier, renderType);
    }

    public BlockModPlant(Block soil, Block base, int baseMeta, Item fruit, int fruitMeta, int tier, int renderType) {
        this(soil, base, baseMeta, fruit, fruitMeta, tier, renderType, false);
    }

    public BlockModPlant(Block soil, Block base, int baseMeta, Item fruit, int fruitMeta, int tier, int renderType, boolean isCustom) {
        super();
        this.soil = soil;
        this.base = base;
        this.baseMeta = baseMeta;
        this.products.addProduce(new ItemStack(fruit, 1, fruitMeta));
        this.tier = tier;
        this.setTickRandomly(true);
        this.useNeighborBrightness = true;
        this.renderType = renderType==1?renderType:6;
        this.isEditable = isCustom;
    }

    //set seed
    public void initializeSeed(ItemSeeds seed) {
        if(this.seed==null) {
            this.seed = seed;
        }
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return super.getMetaFromState(state);
    }

    public ArrayList<ItemStack> getFruits() {return this.products.getAllProducts();}

    public ArrayList<ItemStack> getFruit(Random rand) {return this.getFruit(1, rand);}

    public ArrayList<ItemStack> getFruit(int nr, Random rand) {return this.products.getProduce(nr, rand);}

    public boolean canEdit() {
        return this.isEditable;
    }

    // TODO: textures in 1.8?
    /*
    //register icons
    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister reg) {
        LogHelper.debug("registering icon for: " + this.getUnlocalizedName());
        this.icons = new IIcon[4];
        for(int i=1;i<this.icons.length+1;i++) {
            this.icons[i-1] = reg.registerIcon(this.getUnlocalizedName().substring(this.getUnlocalizedName().indexOf('.') + 1)+i);
        }
    }
    */

    //growing
    @Override
    public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
        int meta = getMetaFromState(state);
        if (meta < 7 && isFertile(world, pos)) {
            double rate = 1.0 + (1 + 0.00) / 10;
            float growthRate = (float) SeedHelper.getBaseGrowth(this.tier);
            meta = (rand.nextDouble() > (growthRate * rate)/100) ? meta : meta + 1;
            world.setBlockState(pos, getStateFromMeta(meta), 2);
        }
    }

    //check if the plant is mature
    public boolean isMature(World world, BlockPos pos) {
        int age = getMetaFromState(world.getBlockState(pos));
        return age == 7;
    }

    // TODO: textures in 1.8?
    /*
    //render different stages
    @Override
    public IIcon getIcon(int side, int meta) {
        switch(meta) {
            case 0: return this.icons[0];
            case 1: return this.icons[0];
            case 2: return this.icons[1];
            case 3: return this.icons[1];
            case 4: return this.icons[1];
            case 5: return this.icons[2];
            case 6: return this.icons[2];
            case 7: return this.icons[3];
        }
        return this.icons[(int)Math.floor(meta/5)];
    }
    */

    //item drops
    @Override
    public void dropBlockAsItemWithChance(World world, BlockPos pos, IBlockState state, float chance, int fortune) {
        super.dropBlockAsItemWithChance(world, pos, state, chance, 0);
    }

    @Override
    public List<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
        List<ItemStack> drops = new ArrayList<ItemStack>();
        drops.add(new ItemStack(this.seed, 1, 0));
        if (getMetaFromState(state) == 7) {
            drops.addAll(getFruit(random));
        }
        return drops;
    }

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return getMetaFromState(state) == 7 ? getCrop() : getSeed();
    }

    //fruit gain
    @Override
    public int quantityDropped(Random rand) {
        return 1;
    }

    @Override
    public void onNeighborBlockChange(World world, BlockPos pos, IBlockState state, Block neighborBlock) {
        if (!canBlockStay(world, pos, state)) {
            //the crop will be destroyed
            dropBlockAsItem(world, pos, state, 0);
            world.setBlockToAir(pos);
        }
    }

    @Override
    public boolean canBlockStay(World world, BlockPos pos, IBlockState state) {
        Block soil = world.getBlockState(pos.down()).getBlock();
        return (soil instanceof net.minecraft.block.BlockFarmland);
    }

    /** @return true if plant can grow */
    @Override
    public boolean isFertile(World world, BlockPos pos) {
        int lightLevel = world.getLight(pos.up());
        if (this.soil == world.getBlockState(pos.down()).getBlock() && lightLevel > 8) {
            // TODO: check if a hard coded meta of 0 works or if we need some kind of BlockState to tag along
            if (base == null || OreDictHelper.isSameOre(base, baseMeta, world.getBlockState(pos.down(2)).getBlock(), 0)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected Item getSeed() {
        return this.seed;
    }

    @Override
    protected Item getCrop() {
        Item crop = null;
        List<ItemStack> items = getFruit(random);
        if (items != null && !items.isEmpty() && items.get(0) != null) {
            crop = items.get(0).getItem();
        }
        return crop;
    }

    @Override
    public int getRenderType() {
        return this.renderType;
    }
}
