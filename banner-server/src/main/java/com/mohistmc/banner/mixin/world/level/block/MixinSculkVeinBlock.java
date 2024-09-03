package com.mohistmc.banner.mixin.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SculkSpreader;
import net.minecraft.world.level.block.SculkVeinBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Iterator;
import java.util.concurrent.atomic.AtomicReference;

@Mixin(SculkVeinBlock.class)
public abstract class MixinSculkVeinBlock {


    @Shadow protected abstract boolean attemptPlaceSculk(SculkSpreader spreader, LevelAccessor level, BlockPos pos, RandomSource random);

    private AtomicReference<BlockPos> banner$source = new AtomicReference<>();

    @Inject(method = "attemptUseCharge", at = @At("HEAD"))
    private void banner$getSource(SculkSpreader.ChargeCursor cursor,
                                  LevelAccessor level,
                                  BlockPos pos, RandomSource random, SculkSpreader spreader,
                                  boolean bl, CallbackInfoReturnable<Integer> cir) {
        banner$source.set(pos);
    }

    @Redirect(method = "attemptUseCharge", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/SculkVeinBlock;attemptPlaceSculk(Lnet/minecraft/world/level/block/SculkSpreader;Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/core/BlockPos;Lnet/minecraft/util/RandomSource;)Z"))
    private boolean banner$attemptPlace(SculkVeinBlock instance, SculkSpreader spreader, LevelAccessor level, BlockPos pos, RandomSource random) {
        return attemptPlaceSculk(spreader, level, pos, random, banner$source.get());
    }

    private AtomicReference<BlockPos> banner$pos = new AtomicReference<>();

    private boolean attemptPlaceSculk(SculkSpreader spreader, LevelAccessor level, BlockPos pos, RandomSource random, BlockPos sourceBlock) {
        banner$pos.set(sourceBlock);
        return attemptPlaceSculk(spreader, level, pos, random);
    }

    @Redirect(method = "attemptPlaceSculk", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/LevelAccessor;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z"))
    private boolean banner$cancelSetBlock(LevelAccessor instance, BlockPos pos, BlockState state, int i) {
        return false;
    }

    @Inject(method = "attemptPlaceSculk", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/LevelAccessor;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z"),
            locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
    private void banner$setBlock(SculkSpreader spreader, LevelAccessor level, BlockPos pos,
                                 RandomSource random, CallbackInfoReturnable<Boolean> cir,
                                 BlockState blockState, TagKey<Block> tagKey, Iterator var7, Direction direction,
                                 BlockPos blockPos, BlockState blockState2, BlockState blockState3) {
        // CraftBukkit start - Call BlockSpreadEvent
        if (!CraftEventFactory.handleBlockSpreadEvent(level, banner$pos.get(), blockPos, blockState2, 3)) {
            cir.setReturnValue(false);
        }
        // CraftBukkit end
    }
}
