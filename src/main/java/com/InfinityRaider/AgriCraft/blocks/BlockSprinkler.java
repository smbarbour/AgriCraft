package com.InfinityRaider.AgriCraft.blocks;

import com.InfinityRaider.AgriCraft.init.Items;
import com.InfinityRaider.AgriCraft.reference.Constants;
import com.InfinityRaider.AgriCraft.tileentity.TileEntitySprinkler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockSprinkler extends BlockContainer {

    public BlockSprinkler() {
        super(Material.iron);
        this.setHardness(2.0F);
        this.setResistance(5.0F);
        setHarvestLevel("axe", 0);
        this.maxX = Constants.unit*12;
        this.minX = Constants.unit*4;
        this.maxZ = this.maxX;
        this.minZ = this.minX;
        this.maxY = Constants.unit*20;
        this.minY = Constants.unit*12;
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileEntitySprinkler();
    }

    @Override
    public boolean isReplaceable(World worldIn, BlockPos pos) {
        return false;
    }

    @Override
    public void harvestBlock(World world, EntityPlayer player, BlockPos pos, IBlockState state, TileEntity te) {
        if (!world.isRemote && !player.isSneaking()) {
            if (!player.capabilities.isCreativeMode) {       //drop items if the player is not in creative
                dropBlockAsItem(world, pos, state, 0);
            }
            world.setBlockToAir(pos);
            world.removeTileEntity(pos);
        }
    }

    @Override
    public void dropBlockAsItemWithChance(World world, BlockPos pos, IBlockState state, float chance, int fortune) {
        if (!world.isRemote) {
            // TODO: override getDrops so that the sprinkler is really dropped
            // ItemStack drop = new ItemStack(Items.sprinkler, 1);
            dropBlockAsItem(world, pos, state, 0);
        }
    }

    @Override
    public void onNeighborBlockChange(World world, BlockPos pos, IBlockState state, Block neighborBlock) {
        if (!canBlockStay(world, pos)) {
            dropBlockAsItem(world, pos, state, 0);
            world.setBlockToAir(pos);
            world.removeTileEntity(pos);
        }
    }

    // TODO: check what happened to the 'canBlockStay' method
    //see if the block can stay
    // @Override
    public boolean canBlockStay(World world, BlockPos pos) {
        Block channel = world.getBlockState(pos.up()).getBlock();
        return channel == com.InfinityRaider.AgriCraft.init.Blocks.blockWaterChannel;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public Item getItem(World worldIn, BlockPos pos) {
        return Items.sprinkler;
    }

    //rendering stuff
    @Override
    public int getRenderType() {return -1;}                 //get default render type: net.minecraft.client.renderer
    @Override
    public boolean isOpaqueCube() {return false;}           //tells minecraft that this is not a block (no levers can be placed on it, it's transparent, ...)

    @Override
    @SideOnly(Side.CLIENT)
    public boolean shouldSideBeRendered(IBlockAccess worldIn, BlockPos pos, EnumFacing side) {
        return false;
    }

    // TODO: textures in 1.8?
    /*
    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int meta) {
        return Blocks.planks.getIcon(0, 0);
    }
    */
}
