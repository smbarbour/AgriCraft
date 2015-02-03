package com.InfinityRaider.AgriCraft.compatibility.waila;

import com.InfinityRaider.AgriCraft.blocks.BlockCrop;
import com.InfinityRaider.AgriCraft.init.Items;
import com.InfinityRaider.AgriCraft.tileentity.TileEntityCrop;
import mcp.mobius.waila.api.*;
import net.minecraft.block.Block;
import net.minecraft.item.ItemSeeds;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.StatCollector;

public class AgriCraftCropDataProvider implements IWailaDataProvider {

    @Override
    public ItemStack getWailaStack(IWailaDataAccessor dataAccessor, IWailaConfigHandler configHandler) {
        Block block = dataAccessor.getBlock();
        if(block instanceof BlockCrop) {
            return new ItemStack(Items.crops, 1, 0);
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
        if(block!=null && block instanceof BlockCrop && te!=null && te instanceof TileEntityCrop) {
            TileEntityCrop crop = (TileEntityCrop) te;
            if(crop.hasPlant()) {
                int growth = crop.growth;
                int gain = crop.gain;
                int strength = crop.strength;
                boolean analyzed = crop.analyzed;
                String seedName = ((ItemSeeds) crop.seed).getItemStackDisplayName(new ItemStack((ItemSeeds) crop.seed, 1, crop.seedMeta));
                currentTip.add(StatCollector.translateToLocal("agricraft_tooltip.seed") + ": " + seedName);
                if(analyzed) {
                    currentTip.add(" - " + StatCollector.translateToLocal("agricraft_tooltip.growth") + ": " + growth);
                    currentTip.add(" - " + StatCollector.translateToLocal("agricraft_tooltip.gain") + ": " + gain);
                    currentTip.add(" - " + StatCollector.translateToLocal("agricraft_tooltip.strength") + ": " + strength);
                }
                else {
                    currentTip.add(StatCollector.translateToLocal("agricraft_tooltip.analyzed"));
                }
                currentTip.add(StatCollector.translateToLocal(crop.isFertile() ? "agricraft_tooltip.fertile" : "agricraft_tooltip.notFertile"));
            }
            else if(crop.weed) {
                currentTip.add(StatCollector.translateToLocal("agricraft_tooltip.weeds"));
            }
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
