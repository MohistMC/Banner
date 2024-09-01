package com.mohistmc.banner.mixin.world.level.material;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.LavaFluid;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LavaFluid.class)
public abstract class MixinLavaFluid {

    @Shadow protected abstract boolean hasFlammableNeighbours(LevelReader level, BlockPos pos);

    @Shadow protected abstract boolean isFlammable(LevelReader level, BlockPos pos);

    /**
     * @author wdog5
     * @reason
     */
    @Overwrite
    public void randomTick(Level level, BlockPos pos, FluidState state, RandomSource random) {
        if (level.getGameRules().getBoolean(GameRules.RULE_DOFIRETICK)) {
            int i = random.nextInt(3);
            if (i > 0) {
                BlockPos blockPos = pos;

                for(int j = 0; j < i; ++j) {
                    blockPos = blockPos.offset(random.nextInt(3) - 1, 1, random.nextInt(3) - 1);
                    if (!level.isLoaded(blockPos)) {
                        return;
                    }

                    BlockState blockState = level.getBlockState(blockPos);
                    if (blockState.isAir()) {
                        if (this.hasFlammableNeighbours(level, blockPos)) {
                            // CraftBukkit start - Prevent lava putting something on fire
                            if (level.getBlockState(blockPos).getBlock() != Blocks.FIRE) {
                                if (CraftEventFactory.callBlockIgniteEvent(level, blockPos, pos).isCancelled()) {
                                    continue;
                                }
                            }
                            // CraftBukkit end
                            level.setBlockAndUpdate(blockPos, BaseFireBlock.getState(level, blockPos));
                            return;
                        }
                    } else if (blockState.blocksMotion()) {
                        return;
                    }
                }
            } else {
                for(int k = 0; k < 3; ++k) {
                    BlockPos blockPos2 = pos.offset(random.nextInt(3) - 1, 0, random.nextInt(3) - 1);
                    if (!level.isLoaded(blockPos2)) {
                        return;
                    }

                    if (level.isEmptyBlock(blockPos2.above()) && this.isFlammable(level, blockPos2)) {
                        level.setBlockAndUpdate(blockPos2.above(), BaseFireBlock.getState(level, blockPos2));
                    }
                }
            }

        }
    }

    @Redirect(method = "spreadTo", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/LevelAccessor;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z"))
    private boolean banner$spreadTo(LevelAccessor instance, BlockPos pos, BlockState state, int i) {
        return CraftEventFactory.handleBlockFormEvent(instance.getMinecraftWorld(), pos, Blocks.STONE.defaultBlockState(), 3);
    }

}
