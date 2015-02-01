package com.InfinityRaider.AgriCraft.blocks;

import com.InfinityRaider.AgriCraft.AgriCraft;
import com.InfinityRaider.AgriCraft.reference.Constants;
import com.InfinityRaider.AgriCraft.tileentity.TileEntityValve;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLever;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;


public class BlockChannelValve extends BlockCustomWood {

    public BlockChannelValve() {
        super();
        this.setBlockBounds(4*Constants.unit, 0, 4*Constants.unit, 12*Constants.unit, 1, 12*Constants.unit);
    }

    @Override
    public void onNeighborBlockChange(World world, BlockPos pos, IBlockState state, Block neighborBlock) {
        if (!world.isRemote) {
            updatePowerStatus(world, pos);
            if (neighborBlock instanceof BlockLever) {
                world.markBlockForUpdate(pos);
            }
        }
    }

    //creative item picking
    @Override
    public ItemStack getPickBlock(MovingObjectPosition target, World world, BlockPos pos) {
        // TODO: replace meta with blockstate
        // world.getBlockState(pos)
        ItemStack stack = new ItemStack(com.InfinityRaider.AgriCraft.init.Blocks.blockChannelValve, 1, 0);
        this.setTag(world, pos, stack);
        return stack;
    }


    // TODO: Figure out where onPostBlockPlaced went
    /* @Override
    public void onPostBlockPlaced(World world, BlockPos pos, int metadata) {
        if (!world.isRemote) {
            updatePowerStatus(world, pos);
        }
    } */

    private void updatePowerStatus(World world, BlockPos pos) {
        TileEntity te = world.getTileEntity(pos);
        if (te !=null && te instanceof TileEntityValve) {
            TileEntityValve valve = (TileEntityValve) te;
            valve.updatePowerStatus();
        }
    }

    //allows levers to be attached to the block
    @Override
    public boolean isSideSolid(IBlockAccess world, BlockPos pos, EnumFacing side) {
        return side!= EnumFacing.UP;
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileEntityValve();
    }

    @Override
    public int getRenderType() {
        return AgriCraft.proxy.getRenderId(Constants.valveId);
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    public boolean shouldSideBeRendered(IBlockAccess worldIn, BlockPos pos, EnumFacing side) {
        return true;
    }

    // TODO: texture handling in 1.8?
    /*@Override
    public IIcon getIcon(int side, int meta) {
        return Blocks.planks.getIcon(0, 0);
    }*/
}
