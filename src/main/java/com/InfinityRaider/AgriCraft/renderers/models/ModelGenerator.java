package com.InfinityRaider.AgriCraft.renderers.models;


import com.InfinityRaider.AgriCraft.utility.LogHelper;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Thanks to RWTema and denseores
 */
public class ModelGenerator {

    private static final ModelGenerator instance = new ModelGenerator();

    public static void register() {
        MinecraftForge.EVENT_BUS.register(instance);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void bakeModels(ModelBakeEvent event) {
        LogHelper.info("bakeModels event.");
    }
}
