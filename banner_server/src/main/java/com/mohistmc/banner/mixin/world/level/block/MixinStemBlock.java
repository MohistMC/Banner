package com.mohistmc.banner.mixin.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.StemBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(StemBlock.class)
public class MixinStemBlock {

    @Shadow @Final private ResourceKey<Block> fruit;

    @Redirect(method = "randomTick", at = @At (value = "INVOKE",
            target = "Lnet/minecraft/server/level/ServerLevel;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z"))
    private boolean banner$growEvent(ServerLevel level, BlockPos pos, BlockState state, int flags) {
        return CraftEventFactory.handleBlockGrowEvent(level, pos, state, flags);
    }

    @Inject(method = "randomTick", at = @At (value = "INVOKE",
            target = "Lnet/minecraft/server/level/ServerLevel;setBlockAndUpdate(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)Z",
            ordinal = 0),
            cancellable = true,
            locals = LocalCapture.CAPTURE_FAILHARD)
    private void banner$growEvent0(BlockState state, ServerLevel level, BlockPos pos, RandomSource random, CallbackInfo ci, float f, int i, Direction direction, BlockPos blockPos) {
        if (!CraftEventFactory.handleBlockGrowEvent(level, blockPos, state)) { ci.cancel(); }
    }

    @Redirect(method = "randomTick", at = @At (value = "INVOKE",
            target = "Lnet/minecraft/server/level/ServerLevel;setBlockAndUpdate(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)Z",
            ordinal = 0))
    private boolean banner$growEvent(ServerLevel instance, BlockPos pos, BlockState state) {
        return false;
    }

    @Redirect(method = "performBonemeal", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z"))
    public boolean banner$cropGrow(ServerLevel world, BlockPos pos, BlockState newState, int flags) {
        return CraftEventFactory.handleBlockGrowEvent(world, pos, newState, flags);
    }
}
