package com.mohistmc.banner.mixin.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.RedstoneTorchBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RedstoneTorchBlock.class)
public class MixinRedstoneTorchBlock {

    @Inject(method = "tick", cancellable = true, at = @At(value = "INVOKE", ordinal = 0, target = "Lnet/minecraft/server/level/ServerLevel;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z"))
    private void banner$blockRedstone1(BlockState state, ServerLevel worldIn, BlockPos pos, RandomSource rand, CallbackInfo ci) {
        int oldCurrent = state.getValue(RedstoneTorchBlock.LIT) ? 15 : 0;
        if (oldCurrent != 0) {
            CraftBlock block = CraftBlock.at(worldIn, pos);
            BlockRedstoneEvent event = new BlockRedstoneEvent(block, oldCurrent, 0);
            Bukkit.getPluginManager().callEvent(event);
            if (event.getNewCurrent() != 0) {
                ci.cancel();
            }
        }
    }

    @Inject(method = "tick", cancellable = true, at = @At(value = "INVOKE", ordinal = 1, target = "Lnet/minecraft/server/level/ServerLevel;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z"))
    private void banner$blockRedstone2(BlockState state, ServerLevel worldIn, BlockPos pos, RandomSource rand, CallbackInfo ci) {
        int oldCurrent = state.getValue(RedstoneTorchBlock.LIT) ? 15 : 0;
        if (oldCurrent != 15) {
            CraftBlock block = CraftBlock.at(worldIn, pos);
            BlockRedstoneEvent event = new BlockRedstoneEvent(block, oldCurrent, 15);
            Bukkit.getPluginManager().callEvent(event);
            if (event.getNewCurrent() != 15) {
                ci.cancel();
            }
        }
    }
}
