package com.InfinityRaider.AgriCraft.blocks;

import com.InfinityRaider.AgriCraft.AgriCraft;
import com.InfinityRaider.AgriCraft.container.ContainerSeedAnalyzer;
import com.InfinityRaider.AgriCraft.creativetab.AgriCraftTab;
import com.InfinityRaider.AgriCraft.handler.GuiHandler;
import com.InfinityRaider.AgriCraft.init.Blocks;
import com.InfinityRaider.AgriCraft.reference.Constants;
import com.InfinityRaider.AgriCraft.tileentity.TileEntitySeedAnalyzer;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class BlockSeedAnalyzer extends Block implements ITileEntityProvider {
    public BlockSeedAnalyzer() {
        super(Material.ground);
        this.setCreativeTab(AgriCraftTab.agriCraftTab);
        this.isBlockContainer = true;
        this.setTickRandomly(false);
        //set mining statistics
        this.setHardness(1);
        this.setResistance(1);
        //set the bounding box dimensions
        this.maxX = 15* Constants.unit;
        this.minX = 1*Constants.unit;
        this.maxZ = this.maxX;
        this.minZ = this.minX;
        this.maxY = 4*Constants.unit;
        this.minY = 0;
    }

    //creates a new tile entity every time a block of this type is placed
    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileEntitySeedAnalyzer();
    }

    //this sets the block's orientation based upon the direction the player is looking when the block is placed
    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        if(world.getTileEntity(pos) instanceof TileEntitySeedAnalyzer) {
            TileEntitySeedAnalyzer analyzer = (TileEntitySeedAnalyzer) world.getTileEntity(pos);
            int direction = MathHelper.floor_double(placer.rotationYaw * 4.0F / 360.0F + 0.5D) & 3;
            switch(direction) {
                case 0: analyzer.setDirection(EnumFacing.NORTH.ordinal()); break;
                case 1: analyzer.setDirection(EnumFacing.EAST.ordinal()); break;
                case 2: analyzer.setDirection(EnumFacing.SOUTH.ordinal()); break;
                case 3: analyzer.setDirection(EnumFacing.WEST.ordinal()); break;
            }
        }
    }

    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state) {
        if (!world.isRemote) {
            world.removeTileEntity(pos);
            world.setBlockToAir(pos);
        }
    }

    //override this to delay the removal of the tile entity until after harvestBlock() has been called
    @Override
    public boolean removedByPlayer(World world, BlockPos pos, EntityPlayer player, boolean willHarvest) {
        return !player.capabilities.isCreativeMode || super.removedByPlayer(world, pos, player, willHarvest);
    }

    //this gets called when the block is mined
    @Override
    public void harvestBlock(World world, EntityPlayer player, BlockPos pos, IBlockState state, TileEntity te) {
        if (!world.isRemote) {
            if (!player.capabilities.isCreativeMode) {
                dropBlockAsItem(world, pos, state, 0);
            }
            breakBlock(world, pos, state);
        }
    }

    //get a list with items dropped by the the crop
    @Override
    public List<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
        List<ItemStack> items = new ArrayList<ItemStack>();
        items.add(new ItemStack(Item.getItemFromBlock(Blocks.seedAnalyzer), 1, 0));
        if (world.getTileEntity(pos) != null && world.getTileEntity(pos) instanceof TileEntitySeedAnalyzer) {
            TileEntitySeedAnalyzer analyzer = (TileEntitySeedAnalyzer) world.getTileEntity(pos);
            if(analyzer.getStackInSlot(ContainerSeedAnalyzer.seedSlotId)!=null) {
                items.add(analyzer.getStackInSlot(ContainerSeedAnalyzer.seedSlotId));
            }
            if(analyzer.getStackInSlot(ContainerSeedAnalyzer.journalSlotId)!=null) {
                items.add(analyzer.getStackInSlot(ContainerSeedAnalyzer.journalSlotId));
            }
        }
        return items;
    }

    //open the gui when the block is activated
    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumFacing side, float hitX, float hitY, float hitZ) {
        if (player.isSneaking()) {
            return false;
        }
        if (!world.isRemote) {
            player.openGui(AgriCraft.instance, GuiHandler.seedAnalyzerID, world, pos.getX(), pos.getY(), pos.getZ());
        }
        return true;
    }

    //rendering stuff
    @Override
    public int getRenderType() {return -1;}                 //get default render type: net.minecraft.client.renderer
    @Override
    public boolean isOpaqueCube() {return false;}           //tells minecraft that this is not a block (no levers can be placed on it, it's transparent, ...)

    @Override
    public boolean shouldSideBeRendered(IBlockAccess worldIn, BlockPos pos, EnumFacing side) {
        return false;
    }

    // TODO: textures in 1.8?
    /*
    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister reg) {
        this.blockIcon = reg.registerIcon(this.getUnlocalizedName().substring(this.getUnlocalizedName().indexOf('.') + 1));
    }
    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int meta) {
        return net.minecraft.init.Blocks.planks.getIcon(0, 0);
    }
    */

    @Override
    public boolean onBlockEventReceived(World world, BlockPos pos, IBlockState state, int eventID, int eventParam) {
        super.onBlockEventReceived(world, pos, state, eventID, eventParam);
        TileEntity tileEntity = world.getTileEntity(pos);
        return tileEntity != null && tileEntity.receiveClientEvent(eventID, eventParam);
    }
}
