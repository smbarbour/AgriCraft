package com.InfinityRaider.AgriCraft.tileentity;

import com.InfinityRaider.AgriCraft.handler.ConfigurationHandler;
import com.InfinityRaider.AgriCraft.reference.Constants;
import com.InfinityRaider.AgriCraft.reference.Names;
import com.InfinityRaider.AgriCraft.utility.interfaces.IDebuggable;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.fluids.*;

import java.util.List;

public class TileEntityTank extends TileEntityCustomWood implements IFluidHandler, IDebuggable {

    protected static final int DISCRETE_MAX = 32;

    protected int connectedTanks=1;
    protected int fluidLevel=0;
    protected int lastDiscreteFluidLevel = 0;
    private int timer = 0;
    
    //OVERRIDES
    //---------
    //this saves the data on the tile entity
    @Override
    public void writeToNBT(NBTTagCompound tag) {
        tag.setInteger(Names.NBT.connected, this.connectedTanks);
        if(this.fluidLevel>0) {
            tag.setInteger(Names.NBT.level, this.fluidLevel);
        }
        super.writeToNBT(tag);
    }

    //this loads the saved data for the tile entity
    @Override
    public void readFromNBT(NBTTagCompound tag) {
        this.connectedTanks = tag.getInteger(Names.NBT.connected);
        if(tag.hasKey(Names.NBT.level)) {
            this.fluidLevel = tag.getInteger(Names.NBT.level);
        }
        else {
            this.fluidLevel=0;
        }
        super.readFromNBT(tag);
    }

    //updates the tile entity every tick
    @Override
    public void updateContainingBlockInfo() {
        if(!this.worldObj.isRemote) {
            boolean change = this.updateMultiBlock();
            if (this.worldObj.canBlockSeeSky(pos) && this.worldObj.isRaining()) {
                if(this.getYPosition()+1==this.getYSize()) {
                    BiomeGenBase biome = this.worldObj.getBiomeGenForCoords(pos);
                    if(biome!=BiomeGenBase.desert && biome!=BiomeGenBase.desertHills) {
                       this.setFluidLevel(this.fluidLevel + 1);
                       change = true;
                    }
                }
            }
            Block blockAbove = worldObj.getBlockState(pos.up()).getBlock();
            if(ConfigurationHandler.fillFromFlowingWater && (blockAbove==Blocks.water || blockAbove==Blocks.flowing_water)) {
                this.setFluidLevel(this.fluidLevel+5);
                change = true;
            }
            //Only send update to the client every 10 ticks to reduce network stress (thanks, Marcin212)
            if(change && timer>10) {
                timer = 0;

                int discreteFluidLevel = getDiscreteFluidLevel();
                if (lastDiscreteFluidLevel != discreteFluidLevel) {
                    lastDiscreteFluidLevel = discreteFluidLevel;
                    markDirtyAndMarkForUpdate();
                }
            }
            timer++;
        }
    }

    //called on client event
    @Override
    public boolean receiveClientEvent(int id, int value) {
        if(worldObj.isRemote) {
            this.worldObj.markBlockForUpdate(pos);
            this.worldObj.notifyLightSet(pos);
            Minecraft.getMinecraft().renderGlobal.markBlockForUpdate(pos);
            super.receiveClientEvent(id, value);
        }
        return true;
    }


    //MULTIBLOCK METHODS
    //------------------
    //checks if this is made of wood
    public boolean isWood() {return this.getBlockMetadata()==0;}

    //checks if this is part of a multiblock
    public boolean isMultiBlock() {return this.connectedTanks>1;}

    //returns the number of blocks in the multiblock
    public int getConnectedTanks() {return this.connectedTanks;}

    //check if a tile entity is instance of TileEntityTank and is the same material
    public boolean isSameTank(TileEntity tileEntity) {
        if(tileEntity!=null && tileEntity instanceof TileEntityTank) {
            TileEntityTank tank = (TileEntityTank) tileEntity;
            return this.isSameMaterial(tank);
        }
        return false;
    }
    //check if a tile entity is part of this multiblock
    public boolean isMultiBlockPartner(TileEntity tileEntity) {
        return this.connectedTanks>1 && (this.isSameTank(tileEntity)) && (this.connectedTanks == ((TileEntityTank) tileEntity).connectedTanks);
    }

    //updates the multiblock, returns true if something has changed
    public boolean updateMultiBlock() {
        boolean change = false;
        if(!this.isMultiBlock()) {
            change = this.checkForMultiBlock();
        }
        return change;
    }

    //multiblockify
    public boolean checkForMultiBlock() {
        if(!this.worldObj.isRemote) {
            //find dimensions
            int xPos = this.findArrayXPosition();
            int yPos = this.findArrayYPosition();
            int zPos = this.findArrayZPosition();
            int xSize = this.findArrayXSize();
            int ySize = this.findArrayYSize();
            int zSize = this.findArrayZSize();

            if (xSize == 1 && ySize == 1 && zSize == 1)
                return false;

            //iterate trough the x-, y- and z-directions if all blocks are tanks
            for (int x = pos.getX() - xPos; x < pos.getX() - xPos + xSize; x++) {
                for (int y = pos.getY() - yPos; y < pos.getY() - yPos + ySize; y++) {
                    for (int z = pos.getZ() - zPos; z < pos.getZ() - zPos + zSize; z++) {
                        if (this.isSameTank(this.worldObj.getTileEntity(pos))) {
                            TileEntityTank tank = (TileEntityTank) this.worldObj.getTileEntity(new BlockPos(x, y, z));
                            int[] tankSize = tank.findArrayDimensions();
                            if(!(xSize==tankSize[0] && ySize==tankSize[1] && zSize==tankSize[2])) {
                                return false;
                            }
                        }
                        else {
                            return false;
                        }
                    }
                }
            }
            //turn all blocks that are in a multiblock in single blocks again to calculate the total fluid level
            for (int x = pos.getX() - xPos; x < pos.getX() - xPos + xSize; x++) {
                for (int y = pos.getY() - yPos; y < pos.getY() - yPos + ySize; y++) {
                    for (int z = pos.getZ() - zPos; z < pos.getZ() - zPos + zSize; z++) {
                        TileEntityTank tank = ((TileEntityTank) this.worldObj.getTileEntity(new BlockPos(x, y, z)));
                        if(tank.isMultiBlock()) {
                            tank.breakMultiBlock();
                        }
                    }
                }
            }
            //calculate the total fluid level
            int lvl = 0;
            for (int x = pos.getX() - xPos; x < pos.getX() - xPos + xSize; x++) {
                for (int y = pos.getY() - yPos; y < pos.getY() - yPos + ySize; y++) {
                    for (int z = pos.getZ() - zPos; z < pos.getZ() - zPos + zSize; z++) {
                        TileEntityTank tank = ((TileEntityTank) this.worldObj.getTileEntity(new BlockPos(x, y, z)));
                        lvl = tank.fluidLevel+lvl;
                    }
                }
            }
            //turn all the blocks into one multiblock
            this.connectedTanks = xSize * ySize * zSize;
            for (int x = pos.getX() - xPos; x < pos.getX() - xPos + xSize; x++) {
                for (int y = pos.getY() - yPos; y < pos.getY() - yPos + ySize; y++) {
                    for (int z = pos.getZ() - zPos; z < pos.getZ() - zPos + zSize; z++) {
                        TileEntityTank tank = ((TileEntityTank) this.worldObj.getTileEntity(new BlockPos(x, y, z)));
                        tank.connectedTanks = xSize * ySize * zSize;
                        tank.setFluidLevel(lvl);
                        tank.markDirtyAndMarkForUpdate();
                    }
                }
            }
            return true;
        }
        return false;
    }

    //breaks up the multiblock and divides the fluid among the tanks
    public void breakMultiBlock() {
        int lvl = this.fluidLevel;
        //get dimensions of the multiblock
        int xPos = this.getXPosition();
        int yPos = this.getYPosition();
        int zPos = this.getZPosition();
        int xSize = this.getXSize();
        int ySize = this.getYSize();
        int zSize = this.getZSize();
        int[] levels = new int[ySize];
        int area = xSize*zSize;
        for(int i=0;i<levels.length;i++) {
            levels[i] = (lvl/area>=this.getSingleCapacity())?this.getSingleCapacity():lvl/area;
            lvl = (lvl - levels[i]*area)<0?0:(lvl - levels[i]*area);
        }
        for(int x=0;x<xSize;x++) {
            for(int y=0;y<ySize;y++) {
                for(int z=0;z<zSize;z++) {
                    TileEntityTank tank = (TileEntityTank) this.worldObj.getTileEntity(
                            new BlockPos(pos.getX()-xPos+x, pos.getY()-yPos+y, pos.getZ()-zPos+z));
                    tank.connectedTanks = 1;
                    tank.fluidLevel = levels[y];
                    tank.markDirtyAndMarkForUpdate();
                }
            }
        }
    }

    //syncs this fluid level to all tanks in the multiblock
    public void syncFluidLevels(boolean forceUpdate) {
        int lvl = this.fluidLevel;
        //get dimensions of the multiblock
        int xPos = this.getXPosition();
        int yPos = this.getYPosition();
        int zPos = this.getZPosition();
        int xSize = this.getXSize();
        int ySize = this.getYSize();
        int zSize = this.getZSize();
        boolean change = timer>20 || forceUpdate;
        if(change) timer = 0;
        //iterate trough the x-, y- and z-directions if all blocks are tanks
        for (int x = pos.getX() - xPos; x < pos.getX() - xPos + xSize; x++) {
            for (int y = pos.getY() - yPos; y < pos.getY() - yPos + ySize; y++) {
                for (int z = pos.getZ() - zPos; z < pos.getZ() - zPos + zSize; z++) {
                    if(this.worldObj.getTileEntity(new BlockPos(x, y, z))!=null && this.worldObj.getTileEntity(new BlockPos(x, y, z)) instanceof TileEntityTank) {
                        TileEntityTank tank =(TileEntityTank) this.worldObj.getTileEntity(new BlockPos(x, y, z));
                        tank.fluidLevel = lvl;
                        if (change && tank.getYPosition() == 0) {
                            tank.markDirtyAndMarkForUpdate();
                        }
                    }
                }
            }
        }

    }

    //returns the xPosition of this block in along a row of these blocks along the X-axis
    private int findArrayXPosition() {
        if(this.isSameTank(this.worldObj.getTileEntity(pos.west()))) {
            return (((TileEntityTank) this.worldObj.getTileEntity(pos.west())).findArrayXPosition() + 1);
        }
        else {
            return 0;
        }
    }

    //returns the yPosition of this block in along a row of these blocks along the Y-axis
    private int findArrayYPosition() {
        if(this.isSameTank(this.worldObj.getTileEntity(pos.down()))) {
            return (((TileEntityTank) this.worldObj.getTileEntity(pos.down())).findArrayYPosition() + 1);
        }
        else {
            return 0;
        }
    }

    //returns the zPosition of this block in along a row of these blocks along the Z-axis
    private int findArrayZPosition() {
        if(this.isSameTank(this.worldObj.getTileEntity(pos.north()))) {
            return (((TileEntityTank) this.worldObj.getTileEntity(pos.north())).findArrayZPosition() + 1);
        }
        else {
            return 0;
        }
    }

    //returns the x size of an array of these blocks this block is in along the X-axis
    private int findArrayXSize() {
        if(this.isSameTank(this.worldObj.getTileEntity(pos.east()))) {
            return ((TileEntityTank) this.worldObj.getTileEntity(pos.east())).findArrayXSize();
        }
        else {
            return this.findArrayXPosition()+1;
        }
    }

    //returns the y size of an array of these blocks this block is in along the Y-axis
    private int findArrayYSize() {
        if(this.isSameTank(this.worldObj.getTileEntity(pos.up()))) {
            return ((TileEntityTank) this.worldObj.getTileEntity(pos.up())).findArrayYSize();
        }
        else {
            return this.findArrayYPosition()+1;
        }
    }

    //returns the z size of an array of these blocks this block is in along the Z-axis
    private int findArrayZSize() {
        if(this.isSameTank(this.worldObj.getTileEntity(pos.south()))) {
            return ((TileEntityTank) this.worldObj.getTileEntity(pos.south())).findArrayZSize();
        }
        else {
            return this.findArrayZPosition()+1;
        }
    }

    //returns the x, y and z sizes of 3 arrays of these blocks along the cardinal directions
    private int[] findArrayDimensions() {
        int[] size = new int[3];
        size[0] = this.findArrayXSize();
        size[1] = this.findArrayYSize();
        size[2] = this.findArrayZSize();
        return size;
    }

    //returns the xPosition of this block in the multiblock
    public int getXPosition() {
        if(this.isMultiBlockPartner(this.worldObj.getTileEntity(pos.west()))) {
            return (((TileEntityTank) this.worldObj.getTileEntity(pos.west())).getXPosition() + 1);
        }
        else {
            return 0;
        }
    }

    //returns the yPosition of this block in the multiblock
    public int getYPosition() {
        if(this.isMultiBlockPartner(this.worldObj.getTileEntity(pos.down()))) {
            return (((TileEntityTank) this.worldObj.getTileEntity(pos.down())).getYPosition() + 1);
        }
        else {
            return 0;
        }
    }

    //returns the zPosition of this block in the multiblock
    public int getZPosition() {
        if(this.isMultiBlockPartner(this.worldObj.getTileEntity(pos.north()))) {
            return (((TileEntityTank) this.worldObj.getTileEntity(pos.north())).getZPosition() + 1);
        }
        else {
            return 0;
        }
    }

    //returns the x size of the multiblock
    public int getXSize() {
        if(this.isMultiBlockPartner(this.worldObj.getTileEntity(pos.east()))) {
            return ((TileEntityTank) this.worldObj.getTileEntity(pos.east())).getXSize();
        }
        else {
            return this.getXPosition()+1;
        }
    }

    //returns the y size of an array of these blocks this block is in along the Y-axis
    public int getYSize() {
        if(this.isMultiBlockPartner(this.worldObj.getTileEntity(pos.up()))) {
            return ((TileEntityTank) this.worldObj.getTileEntity(pos.up())).getYSize();
        }
        else {
            return this.getYPosition()+1;
        }
    }

    //returns the z size of an array of these blocks this block is in along the Z-axis
    public int getZSize() {
        if(this.isMultiBlockPartner(this.worldObj.getTileEntity(pos.south()))) {
            return ((TileEntityTank) this.worldObj.getTileEntity(pos.south())).getZSize();
        }
        else {
            return this.getZPosition()+1;
        }
    }

    //returns the x, y and z sizes of the multiblock
    public int[] getDimensions() {
        int[] size = new int[3];
        size[0] = this.getXSize();
        size[1] = this.getYSize();
        size[2] = this.getZSize();
        return size;
    }

    //TANK METHODS
    //------------
    public FluidStack getContents() {
        return new FluidStack(FluidRegistry.WATER, this.fluidLevel);
    }

    public int getFluidLevel() {
        return this.fluidLevel;
    }

    /** Maps the current fluid level into the interval [0, 32] */
    public int getDiscreteFluidLevel() {
        float discreteFactor = (float) DISCRETE_MAX / (float) getSingleCapacity();
        int discreteFluidLevel = Math.round(discreteFactor * fluidLevel);
        if (discreteFluidLevel == 0 && fluidLevel > 0)
            discreteFluidLevel = 1;
        return discreteFluidLevel;
    }

    public int getScaledDiscreteFluidLevel() {
        float scaleFactor = (float) getSingleCapacity() / (float) DISCRETE_MAX;
        int discreteFluidLevel = getDiscreteFluidLevel();
        return Math.round(scaleFactor * discreteFluidLevel);
    }

    public float getScaledDiscreteFluidY() {
        return getFluidY(getScaledDiscreteFluidLevel());
    }

    public float getFluidY() {
        return this.getFluidY(this.fluidLevel);
    }

    public float getFluidY(int volume) {
        int totalHeight = 16*this.getYSize()-2;     //total height in 1/16th's of a block
        return totalHeight*((float) volume)/((float) this.getTotalCapacity())+2;
    }

    public void setFluidLevel(int lvl) {
        if(lvl!=this.fluidLevel) {
            this.fluidLevel = lvl > this.getTotalCapacity() ? this.getTotalCapacity() : lvl;
            this.syncFluidLevels(false);
        }
    }

    public int getSingleCapacity() {
        return (this.getBlockMetadata()+1)*8*Constants.mB;
    }

    public int getTotalCapacity() {
        return this.getSingleCapacity()*this.connectedTanks;
    }

    public boolean isFull() {
        return this.fluidLevel==this.getTotalCapacity();
    }

    public boolean isEmpty() {
        return this.fluidLevel==0;
    }

    //try to fill the tank
    public int fill(EnumFacing from, int amount, boolean doFill) {
        return this.fill(from, new FluidStack(FluidRegistry.WATER, amount), doFill);
    }

    //try to fill the tank
    @Override
    public int fill(EnumFacing from, FluidStack resource, boolean doFill) {
        if(resource==null || !this.canFill(from, resource.getFluid())) {
            return 0;
        }
        int filled = Math.min(resource.amount, this.getTotalCapacity() - this.getFluidLevel());
        if(doFill) {
            this.setFluidLevel(this.getFluidLevel()+filled);
            syncFluidLevels(true);
        }
        return filled;
    }

    //try to drain from the tank
    @Override
    public FluidStack drain(EnumFacing from, FluidStack resource, boolean doDrain) {
        if(resource==null || !this.canDrain(from, resource.getFluid())) {
           return null;
        }
        int drained = Math.min(resource.amount, this.getFluidLevel());
        if(doDrain) {
            this.setFluidLevel(this.fluidLevel-drained);
            syncFluidLevels(true);
        }
        return new FluidStack(FluidRegistry.WATER, drained);
    }

    //try to drain from the tank
    @Override
    public FluidStack drain(EnumFacing from, int maxDrain, boolean doDrain) {
        return this.drain(from, new FluidStack(FluidRegistry.WATER, maxDrain), doDrain);
    }

    //check if the tank can be filled
    @Override
    public boolean canFill(EnumFacing from, Fluid fluid) {
        return fluid==FluidRegistry.WATER && !this.isFull();
    }

    //check if the tank can be drained
    @Override
    public boolean canDrain(EnumFacing from, Fluid fluid) {
        return fluid==FluidRegistry.WATER && !this.isEmpty();
    }

    @Override
    public FluidTankInfo[] getTankInfo(EnumFacing from) {
        FluidTankInfo[] info = new FluidTankInfo[1];
        info[0] = new FluidTankInfo(this.getContents(), this.getTotalCapacity());
        return info;
    }

    //debug info
    @Override
    public void addDebugInfo(List<String> list) {
        list.add("TANK:");
        list.add("Tank: " + (this.isWood() ? "wood" : "iron") + " (single capacity: " + this.getSingleCapacity() + ")");
        list.add("  - MultiBlock: " + this.isMultiBlock());
        list.add("  - Connected tanks: " + this.getConnectedTanks());
        int[] size = this.getDimensions();
        list.add("  - MultiBlock Size: " + size[0] + "x" + size[1] + "x" + size[2]);
        list.add("  - FluidLevel: " + this.getFluidLevel() + "/" + this.getTotalCapacity());
        list.add("  - FluidHeight: " + this.getFluidY());
        boolean left = this.isMultiBlockPartner(this.worldObj.getTileEntity(pos.west()));
        boolean right = this.isMultiBlockPartner(this.worldObj.getTileEntity(pos.east()));
        boolean back = this.isMultiBlockPartner(this.worldObj.getTileEntity(pos.north()));
        boolean front = this.isMultiBlockPartner(this.worldObj.getTileEntity(pos.south()));
        boolean top = this.isMultiBlockPartner(this.worldObj.getTileEntity(pos.up()));
        boolean below = this.isMultiBlockPartner(this.worldObj.getTileEntity(pos.down()));
        list.add("  - Found multiblock partners on: " + (left ? "left, " : "") + (right ? "right, " : "") + (back ? "back, " : "") + (front ? "front, " : "") + (top ? "top, " : "") + (below ? "below" : ""));
        list.add("Water level is on layer " + (int) Math.floor(((float) this.getFluidLevel() - 0.1F) / ((float) (this.getSingleCapacity() * this.getXSize() * this.getZSize()))) + ".");
        list.add("this clicked is on  layer " + this.getYPosition() + ".");
        list.add("Water height is " + this.getFluidY());
    }
}
