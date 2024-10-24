package com.mohistmc.banner.mixin.world.level.material;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
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
    public void randomTick(ServerLevel serverLevel, BlockPos pos, FluidState state, RandomSource random) {
        if (serverLevel.getGameRules().getBoolean(GameRules.RULE_DOFIRETICK)) {
            int i = random.nextInt(3);
            if (i > 0) {
                BlockPos blockPos = pos;

                for(int j = 0; j < i; ++j) {
                    blockPos = blockPos.offset(random.nextInt(3) - 1, 1, random.nextInt(3) - 1);
                    if (!serverLevel.isLoaded(blockPos)) {
                        return;
                    }

                    BlockState blockState = serverLevel.getBlockState(blockPos);
                    if (blockState.isAir()) {
                        if (this.hasFlammableNeighbours(serverLevel, blockPos)) {
                            // CraftBukkit start - Prevent lava putting something on fire
                            if (serverLevel.getBlockState(blockPos).getBlock() != Blocks.FIRE) {
                                if (CraftEventFactory.callBlockIgniteEvent(serverLevel, blockPos, pos).isCancelled()) {
                                    continue;
                                }
                            }
                            // CraftBukkit end
                            serverLevel.setBlockAndUpdate(blockPos, BaseFireBlock.getState(serverLevel, blockPos));
                            return;
                        }
                    } else if (blockState.blocksMotion()) {
                        return;
                    }
                }
            } else {
                for(int k = 0; k < 3; ++k) {
                    BlockPos blockPos2 = pos.offset(random.nextInt(3) - 1, 0, random.nextInt(3) - 1);
                    if (!serverLevel.isLoaded(blockPos2)) {
                        return;
                    }

                    if (serverLevel.isEmptyBlock(blockPos2.above()) && this.isFlammable(serverLevel, blockPos2)) {
                        serverLevel.setBlockAndUpdate(blockPos2.above(), BaseFireBlock.getState(serverLevel, blockPos2));
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
