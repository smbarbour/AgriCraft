package com.InfinityRaider.AgriCraft.tileentity;

import com.InfinityRaider.AgriCraft.blocks.BlockWaterChannel;
import com.InfinityRaider.AgriCraft.handler.ConfigurationHandler;
import com.InfinityRaider.AgriCraft.reference.Constants;
import com.InfinityRaider.AgriCraft.reference.Names;
import com.InfinityRaider.AgriCraft.renderers.particles.LiquidSprayFX;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBush;
import net.minecraft.block.BlockFarmland;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TileEntitySprinkler extends TileEntityAgricraft {

    private int counter = 0;
    public float angle = 0.0F;
    private boolean isSprinkled = false;

    //this saves the data on the tile entity
    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        if(this.counter>0) {
            tag.setInteger(Names.NBT.level, this.counter);
        }
        tag.setBoolean(Names.NBT.isSprinkled, isSprinkled);
    }

    //this loads the saved data for the tile entity
    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        if(tag.hasKey(Names.NBT.level)) {
            this.counter = tag.getInteger(Names.NBT.level);
        }
        else {
            this.counter=0;
        }
        
        if(tag.hasKey(Names.NBT.isSprinkled)) {
             this.isSprinkled = tag.getBoolean(Names.NBT.isSprinkled);
         }
         else {
             this.isSprinkled = false;
         }
    }

    //checks if the sprinkler is connected to an irrigation channel
    public boolean isConnected() {
        return worldObj.getBlockState(pos.up()).getBlock() instanceof BlockWaterChannel;
    }

    // TODO: textures in 1.8?
    /*
    public IIcon getChannelIcon() {
        if(this.isConnected()) {
            TileEntityChannel channel = (TileEntityChannel) this.worldObj.getTileEntity(pos.up());
            return channel.getIcon();
        }
        return Blocks.planks.getIcon(0, 0);
    }
    */

    @Override
    public void updateContainingBlockInfo() {
        if (!worldObj.isRemote) {
            if (this.sprinkle()) {
                counter = ++counter % ConfigurationHandler.sprinklerGrowthIntervalTicks;
                drainWaterFromChannel();

                for (int yOffset = 1; yOffset < 5; yOffset++) {
                    for (int xOffset = -3; xOffset <= 3; xOffset++) {
                        for (int zOffset = -3; zOffset <= 3; zOffset++) {
                            irrigate(pos.add(xOffset, yOffset, zOffset));
                        }
                    }
                }
            }
        }
        else {
            if(this.isSprinkled) {
            	this.renderLiquidSpray();
            }
        }
    }

    public boolean canSprinkle() {
        return this.isConnected() && ((TileEntityChannel) this.worldObj.getTileEntity(pos.up())).getFluidLevel() > 0;
    }

    private boolean sprinkle() {
    	boolean newState  = this.canSprinkle();
        if(newState != this.isSprinkled) {
        	this.isSprinkled = newState;
        	this.markDirtyAndMarkForUpdate();
        }
        return this.isSprinkled;
    }

    /** Depending on the block type either irrigates farmland or forces plant growth (based on chance) */
    private void irrigate(BlockPos pos) {
        Block block = worldObj.getBlockState(pos).getBlock();
        if (block != null) {
            if (block instanceof BlockFarmland && block.getMetaFromState(worldObj.getBlockState(pos)) < 7) {
                // irrigate farmland
                worldObj.setBlockState(pos, block.getStateFromMeta(7), 2);
            } else if (block instanceof BlockBush) {
                // x chance to force growth tick on plant every y ticks
                if (counter == 0 && Constants.rand.nextDouble() <= ConfigurationHandler.sprinklerGrowthChancePercent) {
                    block.updateTick(worldObj, pos, worldObj.getBlockState(pos), Constants.rand);
                }
            }
        }
    }

    /** Called once per tick, drains water out of the WaterChannel one y-level above */
    private void drainWaterFromChannel() {
        if (counter % 10 == 0) {
            TileEntityChannel channel = (TileEntityChannel) this.worldObj.getTileEntity(pos.up());
            channel.drainFluid(ConfigurationHandler.sprinklerRatePerHalfSecond);
        }
    }

    @SideOnly(Side.CLIENT)
    private void renderLiquidSpray() {
        this.angle = (this.angle+5F)%360;
        for(int i=0;i<4;i++) {
            float alpha = (this.angle+90*i)*((float)Math.PI)/180;
            double xOffset = (4*Constants.unit)*Math.cos(alpha);
            double zOffset = (4*Constants.unit)*Math.sin(alpha);
            float radius = 0.3F;
            for(int j=0;j<=4;j++) {
                float beta = -j*((float)Math.PI)/(8.0F);
                Vec3 vector = new Vec3(radius*Math.cos(alpha), radius*Math.sin(beta), radius*Math.sin(alpha));
                this.spawnLiquidSpray(xOffset*(4-j)/4, zOffset*(4-j)/4, vector);
            }
        }
    }

    @SideOnly(Side.CLIENT)
    private void spawnLiquidSpray(double xOffset, double zOffset, Vec3 vector) {
        LiquidSprayFX liquidSpray = new LiquidSprayFX(this.worldObj, pos.getX()+0.5F+xOffset, pos.getY()+5* Constants.unit, pos.getZ()+0.5F+zOffset, 0.3F, 0.7F, vector);
        Minecraft.getMinecraft().effectRenderer.addEffect(liquidSpray);
    }

}
