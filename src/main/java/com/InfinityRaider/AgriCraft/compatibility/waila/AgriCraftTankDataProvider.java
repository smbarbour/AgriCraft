package com.InfinityRaider.AgriCraft.compatibility.waila;

import com.InfinityRaider.AgriCraft.blocks.BlockWaterTank;
import com.InfinityRaider.AgriCraft.init.Blocks;
import com.InfinityRaider.AgriCraft.tileentity.TileEntityCustomWood;
import com.InfinityRaider.AgriCraft.tileentity.TileEntityTank;
import mcp.mobius.waila.api.*;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.StatCollector;

public class AgriCraftTankDataProvider implements IWailaDataProvider {

    @Override
    public ItemStack getWailaStack(IWailaDataAccessor dataAccessor, IWailaConfigHandler configHandler) {
        Block block = dataAccessor.getBlock();
        TileEntity te = dataAccessor.getTileEntity();
        if(block instanceof BlockWaterTank && te instanceof TileEntityCustomWood) {
            ItemStack stack = new ItemStack(Blocks.blockWaterTank, 1, 0);
            stack.setTagCompound(((TileEntityCustomWood) te).getMaterialTag());
            return stack;
        }
        return null;
    }

    @Override
    public ITaggedList.ITipList getWailaHead(ItemStack itemStack, ITaggedList.ITipList currentTip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
        return currentTip;
    }

    @Override
    public ITaggedList.ITipList getWailaBody(ItemStack itemStack, ITaggedList.ITipList currentTip, IWailaDataAccessor dataAccessor, IWailaConfigHandler config) {
        Block block = dataAccessor.getBlock();
        TileEntity te = dataAccessor.getTileEntity();
        if(block!=null && block instanceof BlockWaterTank && te!=null && te instanceof TileEntityTank) {
            TileEntityTank tank = (TileEntityTank) te;
            //define material
            ItemStack materialStack =tank.getMaterial();
            String material = materialStack.getItem().getItemStackDisplayName(materialStack);
            currentTip.add(StatCollector.translateToLocal("agricraft_tooltip.material")+": "+material);
            //show contents
            TileEntityTank bottomTank = (TileEntityTank) tank.getWorld().getTileEntity(tank.getPos().add(0, -tank.getYPosition(), 0));
            int contents = bottomTank.getFluidLevel();
            int capacity = tank.getTotalCapacity();
            currentTip.add(StatCollector.translateToLocal("agricraft_tooltip.waterLevel")+": "+contents+"/"+capacity);
        }
        return currentTip;
    }

    @Override
    public ITaggedList.ITipList getWailaTail(ItemStack itemStack, ITaggedList.ITipList currentTip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
        return currentTip;
    }

    @Override
    public NBTTagCompound getNBTData(TileEntity te, NBTTagCompound tag, IWailaDataAccessorServer accessor) {
        return tag;
    }
}
