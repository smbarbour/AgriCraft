package com.InfinityRaider.AgriCraft.items;

import com.InfinityRaider.AgriCraft.blocks.BlockCrop;
import com.InfinityRaider.AgriCraft.creativetab.AgriCraftTab;
import com.InfinityRaider.AgriCraft.tileentity.TileEntityCrop;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemSeeds;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;

public class ItemMagnifyingGlass extends ModItem {
    public ItemMagnifyingGlass() {
        super();
        this.setCreativeTab(AgriCraftTab.agriCraftTab);
        this.maxStackSize=1;
    }

    //I'm overriding this just to be sure
    @Override
    public boolean canItemEditBlocks() {return true;}

    //this is called when you right click with this item in hand
    @Override
    public boolean onItemUseFirst(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ) {
        if(world.isRemote) {
            ArrayList<String> list = new ArrayList<String>();
            Block block = world.getBlockState(pos).getBlock();
            if(block != null && block instanceof BlockCrop && world.getTileEntity(pos)!=null && world.getTileEntity(pos) instanceof TileEntityCrop) {
                TileEntityCrop crop = (TileEntityCrop) world.getTileEntity(pos);
                if(crop.hasPlant()) {
                    int growth = crop.growth;
                    int gain = crop.gain;
                    int strength = crop.strength;
                    boolean analyzed = crop.analyzed;
                    String seedName = ((ItemSeeds) crop.seed).getItemStackDisplayName(new ItemStack((ItemSeeds) crop.seed, 1, crop.seedMeta));
                    int meta = block.getMetaFromState(world.getBlockState(pos));
                    float growthPercentage = ((float) meta)/((float) 7)*100.0F;
                    list.add(StatCollector.translateToLocal("agricraft_tooltip.cropWithPlant"));
                    list.add(StatCollector.translateToLocal("agricraft_tooltip.seed") + ": " + seedName);
                    if(analyzed) {
                        list.add(" - " + StatCollector.translateToLocal("agricraft_tooltip.growth") + ": " + growth);
                        list.add(" - " + StatCollector.translateToLocal("agricraft_tooltip.gain") + ": " + gain);
                        list.add(" - " + StatCollector.translateToLocal("agricraft_tooltip.strength") + ": " + strength);
                    }
                    else {
                        list.add(StatCollector.translateToLocal("agricraft_tooltip.analyzed"));
                    }
                    list.add(StatCollector.translateToLocal(crop.isFertile()?"agricraft_tooltip.fertile":"agricraft_tooltip.notFertile"));
                    if (growthPercentage < 100.0) {
                        list.add(String.format("Growth : %.0f %%", growthPercentage));
                    } else {
                        list.add("Growth : Mature");
                    }
                }
                else if(crop.crossCrop) {
                    list.add(StatCollector.translateToLocal("agricraft_tooltip.crossCrop"));
                }
                else if(crop.weed) {
                    list.add(StatCollector.translateToLocal("agricraft_tooltip.weeds"));
                }
                else {
                    list.add(StatCollector.translateToLocal("agricraft_tooltip.cropWithoutPlant"));
                }
            }
            else {
                list.add(StatCollector.translateToLocal("agricraft_tooltip.notCrop"));
            }
            for(String msg:list) {
                player.addChatComponentMessage(new ChatComponentText(msg));
            }
        }
        return true;   //return true so nothing else happens
    }

    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean flag) {
        list.add(StatCollector.translateToLocal("agricraft_tooltip.magnifyingGlass"));
    }

    // TODO: textures in 1.8?
    /*
    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister reg) {
        LogHelper.debug("registering icon for: " + this.getUnlocalizedName());
        this.itemIcon = reg.registerIcon(this.getUnlocalizedName().substring(this.getUnlocalizedName().indexOf('.')+1));
    }
    */
}