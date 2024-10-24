package com.mohistmc.banner.mixin.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SpreadingSnowyDirtBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SpreadingSnowyDirtBlock.class)
public class MixinSpreadingSnowyDirtBlock {

    @Inject(method = "randomTick", cancellable = true, at = @At(value = "INVOKE", ordinal = 0, target = "Lnet/minecraft/server/level/ServerLevel;setBlockAndUpdate(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)Z"))
    public void banner$blockFade(BlockState state, ServerLevel worldIn, BlockPos pos, RandomSource random, CallbackInfo ci) {
        if (CraftEventFactory.callBlockFadeEvent(worldIn, pos, Blocks.DIRT.defaultBlockState()).isCancelled()) {
            ci.cancel();
        }
    }

    @Redirect(method = "randomTick", at = @At(value = "INVOKE", ordinal = 1, target = "Lnet/minecraft/server/level/ServerLevel;setBlockAndUpdate(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)Z"))
    public boolean banner$blockSpread(ServerLevel world, BlockPos to, BlockState state, BlockState state1, ServerLevel worldIn, BlockPos from) {
        return CraftEventFactory.handleBlockSpreadEvent(world, from, to, state);
    }
}
