package io.github.qingchenw.commblock.block;

import io.github.qingchenw.commblock.blockentity.CommunicationBlockEntity;
import io.github.qingchenw.commblock.entity.PlayerExtension;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.GameMasterBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class CommunicationBlock extends BaseEntityBlock implements GameMasterBlock {
    public CommunicationBlock() {
        super(Properties.of()
                .mapColor(MapColor.COLOR_BROWN)
                .requiresCorrectToolForDrops()
                .strength(-1.0F, 3600000.0F)
                .noLootTable());
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CommunicationBlockEntity(pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        return blockEntity instanceof CommunicationBlockEntity ? ((CommunicationBlockEntity)blockEntity).getSuccessCount() : 0;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        if (level.getBlockEntity(pos) instanceof CommunicationBlockEntity communicationBlockEntity) {
            if (stack.hasCustomHoverName()) {
                communicationBlockEntity.setName(stack.getHoverName());
            }
        }
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.getBlockEntity(pos) instanceof CommunicationBlockEntity communicationBlockEntity
                && player.canUseGameMasterBlocks()) {
            ((PlayerExtension) player).openCommunicationBlock(communicationBlockEntity);
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.PASS;
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean movedByPiston) {
        if (!level.isClientSide) {
            if (level.getBlockEntity(pos) instanceof CommunicationBlockEntity communicationBlockEntity) {
                boolean newState = level.hasNeighborSignal(pos);
                boolean oldState = communicationBlockEntity.isPowered();
                communicationBlockEntity.setPowered(newState);
                if (!oldState && newState) {
                    level.scheduleTick(pos, this, 1);
                }
            }
        }
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (level.getBlockEntity(pos) instanceof CommunicationBlockEntity communicationBlockEntity) {
            communicationBlockEntity.sendData(level);
            level.updateNeighbourForOutputSignal(pos, this);
        }
    }
}
