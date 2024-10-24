package com.mohistmc.banner.api.mixin;

import com.mohistmc.banner.api.event.block.SetBlockEvent;
import com.mohistmc.banner.injection.world.level.InjectionLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Level.class)
public abstract class MixinLevel implements LevelAccessor, AutoCloseable, InjectionLevel {

    @Inject(method = "setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;II)Z", at = @At("HEAD"), cancellable = true)
    private void banner$setBlockEvent(BlockPos blockPos, BlockState blockState, int i, int j, CallbackInfoReturnable<Boolean> cir) {
        SetBlockEvent event = new SetBlockEvent(new Location(this.getWorld(), blockPos.getX(), blockPos.getY(), blockPos.getZ()));
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            org.bukkit.block.Block block = event.getLocation().getBlock();
            block.getState().update();
            cir.setReturnValue(false);
        }
    }
}
