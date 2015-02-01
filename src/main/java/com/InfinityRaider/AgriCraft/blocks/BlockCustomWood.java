package com.InfinityRaider.AgriCraft.blocks;

import com.InfinityRaider.AgriCraft.creativetab.AgriCraftTab;
import com.InfinityRaider.AgriCraft.tileentity.TileEntityCustomWood;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

public abstract class BlockCustomWood extends BlockContainer {
    public BlockCustomWood() {
        super(Material.wood);
        this.setHardness(2.0F);
        this.setResistance(5.0F);
        setHarvestLevel("axe", 0);
        this.setCreativeTab(AgriCraftTab.agriCraftTab);
    }

    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state) {
        super.breakBlock(world, pos, state);
        world.removeTileEntity(pos);
    }

    //override this to delay the removal of the tile entity until after harvestBlock() has been called
    @Override
    public boolean removedByPlayer(World world, BlockPos pos, EntityPlayer player, boolean willHarvest) {
        return !player.capabilities.isCreativeMode || super.removedByPlayer(world, pos, player, willHarvest);
    }

    //when the block is harvested
    @Override
    public void harvestBlock(World world, EntityPlayer player, BlockPos pos, IBlockState state, TileEntity te) {
        if((!world.isRemote) && (!player.isSneaking())) {
            if(!player.capabilities.isCreativeMode) {       //drop items if the player is not in creative
                dropBlockAsItem(world, pos, state, 0); // TODO: check if fortune 0 is the correct value here
            }
            world.setBlockToAir(pos);
        }
    }

    protected void setTag(World world, BlockPos pos, ItemStack stack) {
        if(world.getTileEntity(pos)!=null && world.getTileEntity(pos) instanceof TileEntityCustomWood) {
            TileEntityCustomWood te = (TileEntityCustomWood) world.getTileEntity(pos);
            stack.setTagCompound(te.getMaterialTag());
        }
    }
}
