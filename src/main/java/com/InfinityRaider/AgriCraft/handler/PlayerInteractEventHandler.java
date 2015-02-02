package com.InfinityRaider.AgriCraft.handler;

import com.InfinityRaider.AgriCraft.items.ItemCrop;
import com.InfinityRaider.AgriCraft.reference.Names;
import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class PlayerInteractEventHandler {

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPlayerUseItemEvent(PlayerInteractEvent event) {
        if (event.action == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK) {
            Block block = event.world.getBlockState(event.pos).getBlock();
            int meta = block.getMetaFromState(event.world.getBlockState(event.pos));
            if (ItemCrop.isSoilValid(block, meta)) {
                if (event.entityPlayer.getCurrentEquippedItem() != null && event.entityPlayer.getCurrentEquippedItem().stackSize > 0 && event.entityPlayer.getCurrentEquippedItem().getItem() != null && event.entityPlayer.getCurrentEquippedItem().getItem() instanceof IPlantable) {
                    if (ConfigurationHandler.disableVanillaFarming) {
                        //for now, disable vanilla farming for every IPlantable, if people start to need exceptions I'll add in exceptions
                        this.denyEvent(event, false);
                    } else if(event.entityPlayer.getCurrentEquippedItem().hasTagCompound()){
                        NBTTagCompound tag = (NBTTagCompound) event.entityPlayer.getCurrentEquippedItem().getTagCompound().copy();
                        if (tag.hasKey(Names.NBT.growth) && tag.hasKey(Names.NBT.gain) && tag.hasKey(Names.NBT.strength)) {
                            //WIP: place a tile entity storing the seeds data
                            this.denyEvent(event, false);
                        }
                    }
                }
            }
        }
    }

    private void denyEvent(PlayerInteractEvent event, boolean sendToServer) {
        //cancel event to prevent the Hunger Overhaul event handler from being called
        event.setResult(Event.Result.DENY);
        event.useItem = Event.Result.DENY;
        event.useBlock = Event.Result.DENY;
        if (sendToServer) {
            //send the right click to the server manually (cancelling the event will prevent the client from telling the server a right click happened, and nothing will happen, but we still want stuff to happen)
            FMLClientHandler.instance().getClientPlayerEntity().sendQueue.addToSendQueue(new C08PacketPlayerBlockPlacement(event.pos, event.face.getIndex(), event.entityPlayer.inventory.getCurrentItem(), 0f, 0f, 0f));
        }
        event.setCanceled(true);
    }
}
