package com.InfinityRaider.AgriCraft.compatibility.applecore;

import net.minecraft.block.Block;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.eventhandler.Event;

import java.util.Random;

public class AppleCoreHelper {
    public static final String MODID = "AppleCore";
    public static final boolean isAppleCoreLoaded = Loader.isModLoaded(AppleCoreHelper.MODID);
    public static boolean hasDispatcher;
    static {
        try {
            hasDispatcher = isAppleCoreLoaded && Class.forName("squeek.applecore.api.IAppleCoreDispatcher") != null;
        } catch(ClassNotFoundException e) {
            hasDispatcher = false;
        }
    }

    @Optional.Method(modid = AppleCoreHelper.MODID)
    private static Event.Result validateAppleCoreGrowthTick(Block block, World world, BlockPos pos, Random random) {
        Event.Result result = Event.Result.DEFAULT;
        //if(AppleCoreAPI.dispatcher!=null) {
        //    result = AppleCoreAPI.dispatcher.validatePlantGrowth(block, world, x, y, z, random);
        //}
        return result;
    }

    @Optional.Method(modid = AppleCoreHelper.MODID)
    private static void announceAppleCoreGrowthTick(Block block, World world, BlockPos pos) {
        //if(AppleCoreAPI.dispatcher!=null) {
        // AppleCoreAPI.dispatcher.announcePlantGrowth(block, world, x, y, z);
        // }
    }

    public static Event.Result validateGrowthTick(Block block, World world, BlockPos pos, Random random) {
        if (hasDispatcher)
            return validateAppleCoreGrowthTick(block, world, pos, random);
        else
            return Event.Result.DEFAULT;
    }

    public static void announceGrowthTick(Block block, World world, BlockPos pos) {
        if (hasDispatcher) {
            announceAppleCoreGrowthTick(block, world, pos);
        }
    }
}
