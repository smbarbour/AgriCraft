package com.InfinityRaider.AgriCraft.blocks;

import com.InfinityRaider.AgriCraft.AgriCraft;
import com.InfinityRaider.AgriCraft.handler.ConfigurationHandler;
import com.InfinityRaider.AgriCraft.reference.Constants;
import com.InfinityRaider.AgriCraft.tileentity.TileEntityTank;
import com.InfinityRaider.AgriCraft.utility.LogHelper;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

public class BlockWaterTank extends BlockCustomWood{

    public BlockWaterTank() {
        super();
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileEntityTank();
    }

    @Override
    public void dropBlockAsItemWithChance(World world, BlockPos pos, IBlockState state, float chance, int fortune) {
        if (!world.isRemote) {
            // FIXME: will end up in an infinity loop
            ItemStack drop = new ItemStack(com.InfinityRaider.AgriCraft.init.Blocks.blockWaterTank, 1);
            setTag(world, pos, drop);
            dropBlockAsItem(world, pos, state, 0);
        }
    }

    //creative item picking
    @Override
    public ItemStack getPickBlock(MovingObjectPosition target, World world, BlockPos pos) {
        // TODO: check if a BlockState / meta value is needed here
        ItemStack stack = new ItemStack(com.InfinityRaider.AgriCraft.init.Blocks.blockWaterTank, 1);
        this.setTag(world, pos, stack);
        return stack;
    }

    //This gets called when the block is right clicked
    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumFacing side, float hitX, float hitY, float hitZ) {
        boolean update=false;
        if (!world.isRemote) {
            TileEntityTank tank = (TileEntityTank) world.getTileEntity(pos);
            ItemStack stack = player.getCurrentEquippedItem();
            if(stack!=null && stack.getItem()!=null) {
                FluidStack liquid = FluidContainerRegistry.getFluidForFilledItem(stack);
                //put water from liquid container in tank
                if(liquid!=null && liquid.getFluid()==FluidRegistry.WATER) {
                    int quantity = tank.fill(null, liquid, false);
                    if(quantity==liquid.amount) {
                        tank.fill(null, liquid, true);
                        update = true;
                        //change the inventory if player is not in creative mode
                        if(!player.capabilities.isCreativeMode) {
                            if(stack.stackSize==1) {
                                if (stack.getItem().hasContainerItem(stack)) {
                                    player.inventory.setInventorySlotContents(player.inventory.currentItem, stack.getItem().getContainerItem(stack));
                                }
                                else {
                                    player.inventory.setInventorySlotContents(player.inventory.currentItem, null);
                                }
                            }
                            else {
                                stack.splitStack(1);
                                player.inventory.setInventorySlotContents(player.inventory.currentItem, stack);
                            }
                        }
                    }
                }
                //put water from tank in empty liquid container
                else {
                    FluidStack tankContents = tank.getTankInfo(null)[0].fluid;
                    if(tankContents!=null) {
                        ItemStack filledContainer = FluidContainerRegistry.fillFluidContainer(tankContents, stack);
                        FluidStack filledLiquid = FluidContainerRegistry.getFluidForFilledItem(filledContainer);
                        if(filledLiquid!=null) {
                            //change the inventory if the player is not in creative mode
                            if(!player.capabilities.isCreativeMode) {
                                if (stack.stackSize == 1) {
                                    if (stack.getItem().hasContainerItem(stack)) {
                                        player.inventory.setInventorySlotContents(player.inventory.currentItem, stack.getItem().getContainerItem(stack));
                                    } else {
                                        player.inventory.setInventorySlotContents(player.inventory.currentItem, null);
                                    }
                                    player.inventory.setInventorySlotContents(player.inventory.currentItem, filledContainer);
                                } else {
                                    if (!player.inventory.addItemStackToInventory(filledContainer)) {
                                        return false;
                                    } else {
                                        stack.splitStack(1);
                                        player.inventory.setInventorySlotContents(player.inventory.currentItem, stack);
                                        player.inventory.addItemStackToInventory(filledContainer);
                                        player.inventory.markDirty();
                                    }
                                }
                            }
                            tank.drain(null, filledLiquid.amount, true);
                            update = true;
                        }
                    }
                }
            }
            if(update) {
                tank.markDirtyAndMarkForUpdate();
                world.markBlockForUpdate(pos);
                return true;
            }
            else {
                return false;
            }
        }
        return true;
    }

    //when the block is broken
    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state) {
        if(!world.isRemote) {
            LogHelper.debug("breaking tank");
            boolean placeWater = false;
            LogHelper.debug("TileEntity found: " + (world.getTileEntity(pos) != null));
            if (world.getTileEntity(pos) != null && world.getTileEntity(pos) instanceof TileEntityTank) {
                TileEntityTank tank = (TileEntityTank) world.getTileEntity(pos);
                tank.breakMultiBlock();
                placeWater = tank.getFluidLevel() >= Constants.mB;
            }
            world.removeTileEntity(pos);
            if (ConfigurationHandler.placeWater && placeWater) {
                world.setBlockState(pos, new BlockState(Blocks.water).getBaseState(), 3);
                Blocks.water.onNeighborBlockChange(world, pos, state, null);
            } else {
                world.setBlockToAir(pos);
            }
        }
    }

    @Override
    public int damageDropped(IBlockState state) {
        // TODO: implement it as it was in 1.7
        return super.damageDropped(state);
    }


    //render methods
    //--------------
    @Override
    public int getRenderType() {return AgriCraft.proxy.getRenderId(Constants.tankId);}                 //get the correct renderId
    @Override
    public boolean isOpaqueCube() {return false;}           //tells minecraft that this is not a block (no levers can be placed on it, it's transparent, ...)

    @Override
    public boolean shouldSideBeRendered(IBlockAccess worldIn, BlockPos pos, EnumFacing side) {
        return true;
    }

    // TODO: textures in 1.8?
    /*
    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int meta) {
        if(meta==0) {
            return Blocks.planks.getIcon(0, 0);
        }
        else if(meta==1) {
            return Blocks.iron_block.getIcon(0, 0);
        }
        return null;
    }
    */

    @Override
    public boolean onBlockEventReceived(World world, BlockPos pos, IBlockState state, int eventID, int eventParam) {
        super.onBlockEventReceived(world, pos, state, eventID, eventParam);
        return world.getTileEntity(pos) != null && world.getTileEntity(pos).receiveClientEvent(eventID, eventParam);
    }
}
