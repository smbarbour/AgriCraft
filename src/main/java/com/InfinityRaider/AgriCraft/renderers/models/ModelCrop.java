package com.InfinityRaider.AgriCraft.renderers.models;

import com.google.common.base.Function;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IFlexibleBakedModel;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.IModelState;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;


public class ModelCrop implements IModel {

    @Override
    public Collection<ResourceLocation> getDependencies() {
        return null;
    }

    @Override
    public Collection<ResourceLocation> getTextures() {
        return null;
    }

    @Override
    public IFlexibleBakedModel bake(IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {
        return new ModelCropBaked(null, format);
    }

    @Override
    public IModelState getDefaultState() {
        return null;
    }

    private static class ModelCropBaked extends IFlexibleBakedModel.Wrapper {

        public ModelCropBaked(IBakedModel parent, VertexFormat format) {
            super(parent, format);
        }

        @Override
        public List<BakedQuad> getFaceQuads(EnumFacing side) {
            return Arrays.asList(new BakedQuad(new int[]{0, 0, 1, 1}, 0, side));
        }
    }
}
