package com.mohistmc.banner.mixin.world.level.chunk;

import com.mohistmc.banner.bukkit.BukkitCaptures;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net/minecraft/world/level/chunk/LevelChunk$BoundTickingBlockEntity")
public class MixinLevelChunk_BoundTickingBlockEntity<T extends BlockEntity>  {

    @Shadow @Final private T blockEntity;

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/entity/BlockEntityTicker;tick(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/entity/BlockEntity;)V"))
    private void banner$captureBlockEntity(CallbackInfo ci) {
        BukkitCaptures.captureTickingBlockEntity(this.blockEntity);
    }

    @Inject(method = "tick", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/chunk/LevelChunk;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;"))
    private void banner$startTimings(CallbackInfo ci) {
        this.blockEntity.bridge$tickTimer().startTiming(); // Spigot
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/world/level/block/entity/BlockEntityTicker;tick(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/entity/BlockEntity;)V"))
    private void banner$resetBlockEntity(CallbackInfo ci) {
        BukkitCaptures.resetTickingBlockEntity();
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void banner$stopTimings(CallbackInfo ci) {
        this.blockEntity.bridge$tickTimer().stopTiming();
    }
}
