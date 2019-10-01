package io.github.qingchenw.communicationblock;

import io.netty.buffer.ByteBuf;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class MessageUpdateCommBlock implements IMessage
{
	private int x;
	private int y;
	private int z;
	private String port;
	private String data;
	
	public MessageUpdateCommBlock() {}

	public MessageUpdateCommBlock(int x, int y, int z, String port, String data)
	{
		this.x = x;
		this.y = y;
		this.z = z;
		this.port = port;
		this.data = data;
	}

	@Override
	public void fromBytes(ByteBuf buf)
	{
		PacketBuffer pb = new PacketBuffer(buf);
		this.x = pb.readInt();
		this.y = pb.readInt();
		this.z = pb.readInt();
		int length = pb.readShort();
		this.port = pb.readString(length);
		this.data = pb.readString(pb.readableBytes());
		
	}

	@Override
	public void toBytes(ByteBuf buf)
	{
		PacketBuffer pb = new PacketBuffer(buf);
		pb.writeInt(this.x);
		pb.writeInt(this.y);
		pb.writeInt(this.z);
		pb.writeShort(this.port.length());
		pb.writeString(this.port);
		pb.writeString(this.data);
	}
	
    public static class Handler implements IMessageHandler<MessageUpdateCommBlock, IMessage>
    {
        @Override
        public IMessage onMessage(final MessageUpdateCommBlock message, MessageContext ctx)
        {
            if (ctx.side == Side.SERVER)
            {
                final EntityPlayerMP player = ctx.getServerHandler().player;
                final MinecraftServer server = player.getServer();
                server.addScheduledTask(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        if (!server.isCommandBlockEnabled())
                        {
                            player.sendMessage(new TextComponentTranslation("commMod.notEnabled"));
                            return;
                        }

                        if (!player.canUseCommandBlock())
                        {
                            player.sendMessage(new TextComponentTranslation("commMod.notAllowed"));
                            return;
                        }
                        
                        try
                        {
                            TileEntity tileentity = player.world.getTileEntity(new BlockPos(message.x, message.y, message.z));
                            if (tileentity != null && tileentity instanceof TileEntityCommunicationBlock)
                            {
                            	TileEntityCommunicationBlock tileentityCommunicationBlock = (TileEntityCommunicationBlock) tileentity;
                            	tileentityCommunicationBlock.setPort(message.port);
                            	tileentityCommunicationBlock.setData(message.data);
                            	IBlockState blockState = tileentity.getWorld().getBlockState(tileentity.getPos());
                            	tileentity.getWorld().notifyBlockUpdate(tileentity.getPos(), blockState, blockState, 3);
                                player.sendMessage(new TextComponentTranslation("commMod.setCommand.success", message.data));
                            }
                        }
                        catch (Exception exception)
                        {
                            SerialPortMod.logger.error("Couldn't set communication block", exception);
                        }
                    }
                });
            }
			return null;
        }
    }
}
