package com.InfinityRaider.AgriCraft.items;

import com.InfinityRaider.AgriCraft.creativetab.AgriCraftTab;
import com.InfinityRaider.AgriCraft.farming.SoilWhitelist;
import com.InfinityRaider.AgriCraft.init.Blocks;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

import java.util.Arrays;

public class ItemCrop extends ModItem {
    private static Block[] soils = {net.minecraft.init.Blocks.sand, net.minecraft.init.Blocks.soul_sand, net.minecraft.init.Blocks.mycelium};
    public ItemCrop() {
        super();
        this.setCreativeTab(AgriCraftTab.agriCraftTab);
    }

    //I'm overriding this just to be sure
    @Override
    public boolean canItemEditBlocks() {return true;}

    //this is called when you right click with this item in hand
    @Override
    public boolean onItemUseFirst(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ) {
        if (!world.isRemote) {
            IBlockState state = world.getBlockState(pos);
            // TODO: check if side.getIndex() does mean the same here
            if (isSoilValid(state.getBlock(), state.getBlock().getMetaFromState(state))
                    && world.getBlockState(pos.up()).getBlock().getMaterial()== Material.air && side.getIndex() == 1) {
                world.setBlockState(pos.up(), Blocks.blockCrop.getDefaultState());
                stack.stackSize = player.capabilities.isCreativeMode ? stack.stackSize : stack.stackSize - 1;
                return false;
            }
        }
        return false;   //return false or else no other use methods will be called (for instance "onBlockActivated" on the crops block)
    }

    public static boolean isSoilValid(Block soil, int soilMeta) {
        return Arrays.asList(soils).contains(soil) || SoilWhitelist.isSoilFertile(soil, soilMeta);

    }
}
