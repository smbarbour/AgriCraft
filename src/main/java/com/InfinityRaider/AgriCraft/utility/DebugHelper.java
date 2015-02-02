package com.InfinityRaider.AgriCraft.utility;

import com.InfinityRaider.AgriCraft.utility.interfaces.IDebuggable;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public abstract class DebugHelper {

    public static void debug(EntityPlayer player, World world, BlockPos pos) {
        debug(player, world, pos.getX(), pos.getY(), pos.getZ());
    }

    public static void debug(EntityPlayer player, World world, int x, int y, int z) {
        ArrayList<String> list = new ArrayList<String>();
        getDebugData(world, new BlockPos(x, y,z), list);
        for(String data:list) {
            LogHelper.debug(data);
            player.addChatComponentMessage(new ChatComponentText(data));
        }
    }

    public static void debug(World world, int x, int y, int z) {
        ArrayList<String> list = new ArrayList<String>();
        getDebugData(world, new BlockPos(x, y, z), list);
        for(String data:list) {
            LogHelper.debug(data);
        }
    }

    private static void getDebugData(World world, BlockPos pos, List<String> list) {
        if (!world.isRemote) {
            list.add("Server debug info:");
            list.add("------------------");
        } else {
            list.add("Client debug info:");
            list.add("------------------");
        }
        TileEntity tile = world.getTileEntity(pos);
        if(tile!=null && tile instanceof IDebuggable) {
            ((IDebuggable) tile).addDebugInfo(list);
        }
        else {
            IBlockState state = world.getBlockState(pos);
            list.add("Block: "+ Block.blockRegistry.getNameForObject(state.getBlock()));
            list.add("Meta: "+ state.getBlock().getMetaFromState(state));
        }
    }
}
