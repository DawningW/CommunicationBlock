package io.github.qingchenw.communicationblock;

import java.io.DataOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;

import javax.annotation.Nullable;

import com.google.common.base.Strings;

import gnu.io.NRSerialPort;
import io.github.qingchenw.communicationblock.client.GuiCommunicationBlock;
import io.github.qingchenw.communicationblock.utils.SerialPortManager;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IWorldNameable;
import net.minecraft.world.World;

public class TileEntityCommunicationBlock extends TileEntity implements IWorldNameable
{
	private static final SimpleDateFormat TIMESTAMP_FORMAT = new SimpleDateFormat("HH:mm:ss");
	private String customName = "@";
	private String port = "";
    private String dataStored = "";
    private int successCount;
    private boolean powered;
    
	@Override
	public boolean hasCustomName()
	{
		return !Strings.isNullOrEmpty(customName);
	}
    
    @Override
	public String getName()
    {
        return this.customName;
    }

    public void setName(String name)
    {
        this.customName = name;
    }
    
    public void setPort(String port)
    {
        this.port = port;
    }

    public String getPort()
    {
        return this.port;
    }
    
    public void setData(String data)
    {
        this.dataStored = data;
        this.successCount = 0;
    }

    public String getData()
    {
        return this.dataStored;
    }
    
    public int getSuccessCount()
    {
        return this.successCount;
    }

    public void setSuccessCount(int count)
    {
        this.successCount = count;
    }
    
    public void setPowered(boolean powered)
    {
        this.powered = powered;
    }

    public boolean isPowered()
    {
        return this.powered;
    }
    
    public boolean trigger(World world)
    {
        if (!world.isRemote)
        {
        	if (!Strings.isNullOrEmpty(getPort()))
        	{
				NRSerialPort serial = SerialPortManager.getPort(getPort());
				if (serial != null)
				{
					try
					{
						DataOutputStream outs = new DataOutputStream(serial.getOutputStream());
						outs.writeChar(Integer.valueOf(this.dataStored));
						outs.close();
						SerialPortMod.logger.info("Data have been sent to port: " + getPort() + ", with data: " + getData());
					}
					catch (NumberFormatException | IOException e)
					{
						e.printStackTrace();
						return false;
					}
		        	++this.successCount;
		        	return true;
				}
        	}
        	return false;
        }
        else
        {
            return false;
        }
    }

    @Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound)
    {
    	super.writeToNBT(compound);
    	compound.setString("CustomName", this.customName);
    	compound.setString("Port", this.port);
    	compound.setString("Data", this.dataStored);
    	compound.setInteger("SuccessCount", this.successCount);
        return compound;
    }

    @Override
	public void readFromNBT(NBTTagCompound compound)
    {
        super.readFromNBT(compound);
        if (compound.hasKey("CustomName", 8))
        {
            this.customName = compound.getString("CustomName");
        }
        this.port = compound.getString("Port");
        this.dataStored = compound.getString("Data");
        this.successCount = compound.getInteger("SuccessCount");
    }

    @Override
	@Nullable
    public SPacketUpdateTileEntity getUpdatePacket()
    {
        NBTTagCompound tagcompound = this.writeToNBT(new NBTTagCompound());
        return new SPacketUpdateTileEntity(this.pos, 2, tagcompound);
    }
    
    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt)
    {
    	this.port = pkt.getNbtCompound().getString("Port");
    	this.dataStored = pkt.getNbtCompound().getString("Data");
    	
        if (this.getWorld().isRemote && Minecraft.getMinecraft().currentScreen instanceof GuiCommunicationBlock)
        {
            ((GuiCommunicationBlock) Minecraft.getMinecraft().currentScreen).updateGui();
        }
    }
}
