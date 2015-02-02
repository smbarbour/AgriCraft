package com.InfinityRaider.AgriCraft.items;

import com.InfinityRaider.AgriCraft.blocks.BlockModPlant;
import com.InfinityRaider.AgriCraft.creativetab.AgriCraftTab;
import com.InfinityRaider.AgriCraft.init.Blocks;
import com.InfinityRaider.AgriCraft.utility.LogHelper;
import com.InfinityRaider.AgriCraft.utility.SeedHelper;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemSeeds;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemModSeed extends ItemSeeds implements IPlantable{
    private String displayName;

    @SideOnly(Side.CLIENT)
    private String information;

    public ItemModSeed(BlockModPlant plant, String information) {
        this(plant, net.minecraft.init.Blocks.farmland, information);
    }

    public ItemModSeed(BlockModPlant plant, String name, String information) {
        this(plant, net.minecraft.init.Blocks.farmland, information);
        this.displayName = name;
    }

    public ItemModSeed(BlockModPlant plant, Block soil, String information) {
        super(plant, soil);
        if(FMLCommonHandler.instance().getEffectiveSide()==Side.CLIENT) {
            this.information = information;
        }
        this.setCreativeTab(AgriCraftTab.agriCraftTab);
    }

    public ItemModSeed(BlockModPlant plant, Block soil, String name, String information) {
        this(plant, soil, information);
        this.displayName = name;
    }

    public BlockModPlant getPlant() {
        return (BlockModPlant) this.getPlant(null, BlockPos.ORIGIN).getBlock();
    }

    @SideOnly(Side.CLIENT)
    public String getInformation() {
        return this.information;
    }

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer playerIn, World worldIn, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ) {
        Block block = worldIn.getBlockState(pos).getBlock();
        if (block == Blocks.blockCrop) {
            LogHelper.debug("Trying to plant seed "+stack.getItem().getUnlocalizedName()+" on crops");
            return true;
        }
        if(SeedHelper.isCorrectSoil(block, block.getMetaFromState(worldIn.getBlockState(pos)), (ItemSeeds) stack.getItem(), stack.getItemDamage())) {
            super.onItemUse(stack, playerIn, worldIn, pos, side, hitX, hitY, hitZ);
        }
        return false;
    }

    // TODO: textures in 1.8?
    /*
    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister reg) {
        LogHelper.debug("registering icon for: " + this.getUnlocalizedName());
        itemIcon = reg.registerIcon(this.getUnlocalizedName().substring(this.getUnlocalizedName().indexOf('.')+1));
    }
    */

    @Override
    public String getItemStackDisplayName(ItemStack stack) {
        return (this.displayName==null || this.displayName.equals(""))?super.getItemStackDisplayName(stack):this.displayName;
    }
}
