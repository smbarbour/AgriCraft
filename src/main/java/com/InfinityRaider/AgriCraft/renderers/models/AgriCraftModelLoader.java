package com.InfinityRaider.AgriCraft.renderers.models;

import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ICustomModelLoader;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoaderRegistry;


public class AgriCraftModelLoader implements ICustomModelLoader {

    private static final AgriCraftModelLoader instance = new AgriCraftModelLoader();

    /** Will register this custom model loader with forge. Best called in the init phase */
    public static void register() {
        ModelLoaderRegistry.registerLoader(instance);
    }

    @Override
    public boolean accepts(ResourceLocation modelLocation) {
        if (modelLocation.toString().equals("agricraft:models/crops"))
            return true;

        return false;
    }

    @Override
    public IModel loadModel(ResourceLocation modelLocation) {
        if (modelLocation.toString().equals("agricraft:models/crops"))
            return new ModelCrop();
        return null;
    }

    @Override
    public void onResourceManagerReload(IResourceManager resourceManager) {
        // do nothing at the moment
    }
}
