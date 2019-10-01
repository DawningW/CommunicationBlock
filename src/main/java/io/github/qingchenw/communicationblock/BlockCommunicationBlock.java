package io.github.qingchenw.communicationblock;

import java.util.Random;

import io.github.qingchenw.communicationblock.client.GuiCommunicationBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockCommunicationBlock extends BlockContainer
{
    public BlockCommunicationBlock(MapColor color)
    {
        super(Material.IRON, color);
    }
    
    @Override
	public EnumBlockRenderType getRenderType(IBlockState state)
    {
        return EnumBlockRenderType.MODEL;
    }
    
    @Override
	protected BlockStateContainer createBlockState()
    {
        return new BlockStateContainer(this);
    }
    
    @Override
	public TileEntity createNewTileEntity(World world, int meta)
    {
        return new TileEntityCommunicationBlock();
    }
    
    @Override
	public IBlockState getStateFromMeta(int meta)
    {
        return super.getStateFromMeta(meta);
    }

    @Override
	public int getMetaFromState(IBlockState state)
    {
        return super.getMetaFromState(state);
    }
    
    @Override
	public int quantityDropped(Random random)
    {
        return 0;
    }
    
    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, EnumHand hand)
    {
        return super.getStateForPlacement(world, pos, facing, hitX, hitY, hitZ, meta, placer, hand);
    }
    
    @Override
	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack)
    {
        TileEntity tileentity = world.getTileEntity(pos);

        if (tileentity instanceof TileEntityCommunicationBlock)
        {
        	TileEntityCommunicationBlock tileEntityCommunicationBlock = (TileEntityCommunicationBlock) tileentity;

            if (stack.hasDisplayName())
            {
            	tileEntityCommunicationBlock.setName(stack.getDisplayName());
            }
        }
    }
    
    @Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
    {
        TileEntity tileentity = world.getTileEntity(pos);
        if (tileentity instanceof TileEntityCommunicationBlock && player.canUseCommandBlock())
        {
        	TileEntityCommunicationBlock tileEntityCommunicationBlock = (TileEntityCommunicationBlock) tileentity;
        	if (!player.getEntityWorld().isRemote)
            {
                SPacketUpdateTileEntity packet = tileEntityCommunicationBlock.getUpdatePacket();
                ((EntityPlayerMP) player).connection.sendPacket(packet);
            }
        	else
        	{
        		Minecraft.getMinecraft().displayGuiScreen(new GuiCommunicationBlock(tileEntityCommunicationBlock));
        	}
            return true;
        }
        return false;
    }
    
    @Override
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block, BlockPos fromPos)
    {
        if (!world.isRemote)
        {
            TileEntity tileentity = world.getTileEntity(pos);
            if (tileentity instanceof TileEntityCommunicationBlock)
            {
            	TileEntityCommunicationBlock tileentityCommunicationBlock = (TileEntityCommunicationBlock) tileentity;
                boolean flag = world.isBlockPowered(pos);
                boolean flag1 = tileentityCommunicationBlock.isPowered();
                tileentityCommunicationBlock.setPowered(flag);

                if (!flag1 && flag)
                {
                    world.scheduleUpdate(pos, this, this.tickRate(world));
                }
            }
        }
    }
    
    @Override
	public boolean hasComparatorInputOverride(IBlockState state)
    {
        return true;
    }

    @Override
	public int getComparatorInputOverride(IBlockState blockState, World world, BlockPos pos)
    {
        TileEntity tileentity = world.getTileEntity(pos);
        return tileentity instanceof TileEntityCommunicationBlock ? ((TileEntityCommunicationBlock) tileentity).getSuccessCount() : 0;
    }
    
    @Override
	public int tickRate(World world)
    {
        return 1;
    }
    
    @Override
	public void updateTick(World world, BlockPos pos, IBlockState state, Random rand)
    {
        TileEntity tileentity = world.getTileEntity(pos);
        if (tileentity instanceof TileEntityCommunicationBlock)
        {
            ((TileEntityCommunicationBlock) tileentity).trigger(world);
            world.updateComparatorOutputLevel(pos, this);
        }
    }
}
