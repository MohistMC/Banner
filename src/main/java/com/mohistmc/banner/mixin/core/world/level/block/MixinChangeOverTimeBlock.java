package com.mohistmc.banner.mixin.core.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.ChangeOverTimeBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Optional;

@Mixin(ChangeOverTimeBlock.class)
public interface MixinChangeOverTimeBlock<T extends Enum<T>> {

    @Shadow Optional<BlockState> getNextState(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource);

    default void changeOverTime(BlockState state, ServerLevel level, BlockPos pos, RandomSource randomSource) {
        float f = 0.05688889F;
        if (randomSource.nextFloat() < 0.05688889F) {
            this.getNextState(state, level, pos, randomSource).ifPresent((p_153039_) -> {
                CraftEventFactory.handleBlockFormEvent(level, pos, p_153039_);
            });
        }
    }
}
