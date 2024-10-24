package com.mohistmc.banner.mixin.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.TripWireHookBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TripWireHookBlock.class)
public class MixinTripWireHookBlock {

    @Inject(method = "calculateState", cancellable = true, at = @At(value = "INVOKE", ordinal = 1, target = "Lnet/minecraft/world/level/block/TripWireHookBlock;emitState(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;ZZZZ)V"))
    private static void banner$blockRedstone(Level worldIn, BlockPos pos, BlockState hookState, boolean attaching, boolean shouldNotifyNeighbours, int searchRange, BlockState state, CallbackInfo ci) {
        BlockRedstoneEvent event = new BlockRedstoneEvent(CraftBlock.at(worldIn, pos), 15, 0);
        Bukkit.getPluginManager().callEvent(event);
        if (event.getNewCurrent() > 0) {
            ci.cancel();
        }
    }
}
