package com.InfinityRaider.AgriCraft.tileentity;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;

public class TileEntityAgricraft extends TileEntity {

    @Override
    public Packet getDescriptionPacket(){
        NBTTagCompound nbtTag = new NBTTagCompound();
        writeToNBT(nbtTag);
        return new S35PacketUpdateTileEntity(pos, getBlockMetadata(), nbtTag);
    }

    //read data from packet
    @Override
    public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt) {
        readFromNBT(pkt.getNbtCompound());
    }

    public void markDirtyAndMarkForUpdate() {
        worldObj.notifyLightSet(pos);
        markDirty();
        worldObj.markBlockForUpdate(pos);
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        if (worldObj != null) {
            worldObj.markBlockForUpdate(pos);
        }
        super.readFromNBT(tag);
    }
}
