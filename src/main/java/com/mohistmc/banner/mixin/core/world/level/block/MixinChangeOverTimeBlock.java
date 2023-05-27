package com.mohistmc.banner.mixin.core.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChangeOverTimeBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Optional;

@Mixin(ChangeOverTimeBlock.class)
public interface MixinChangeOverTimeBlock<T extends Enum<T>> {

    @Shadow T getAge();

    @Shadow float getChanceModifier();

    @Shadow Optional<BlockState> getNext(BlockState state);

    /**
     * @author wdog5
     * @reason bukkit event
     */
    @Overwrite
    default void applyChangeOverTime(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        int i = this.getAge().ordinal();
        int j = 0;
        int k = 0;

        for (BlockPos blockPos : BlockPos.withinManhattan(pos, 4, 4, 4)) {
            int l = blockPos.distManhattan(pos);
            if (l > 4) {
                break;
            }

            if (!blockPos.equals(pos)) {
                BlockState blockState = level.getBlockState(blockPos);
                Block block = blockState.getBlock();
                if (block instanceof ChangeOverTimeBlock) {
                    Enum<?> enum_ = ((ChangeOverTimeBlock) block).getAge();
                    if (this.getAge().getClass() == enum_.getClass()) {
                        int m = enum_.ordinal();
                        if (m < i) {
                            return;
                        }

                        if (m > i) {
                            ++k;
                        } else {
                            ++j;
                        }
                    }
                }
            }
        }

        float f = (float)(k + 1) / (float)(k + j + 1);
        float g = f * f * this.getChanceModifier();
        if (random.nextFloat() < g) {
            this.getNext(state).ifPresent((blockStatex) -> {
                org.bukkit.craftbukkit.v1_19_R3.event.CraftEventFactory.handleBlockFormEvent(level, pos, blockStatex); // CraftBukkit
            });
        }

    }
}
