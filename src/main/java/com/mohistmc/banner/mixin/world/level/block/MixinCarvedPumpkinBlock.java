package com.mohistmc.banner.mixin.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CarvedPumpkinBlock;
import net.minecraft.world.level.block.state.pattern.BlockPattern;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CarvedPumpkinBlock.class)
public abstract class MixinCarvedPumpkinBlock {

    @Redirect(method = "spawnGolemInWorld", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/CarvedPumpkinBlock;clearPatternBlocks(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/level/block/state/pattern/BlockPattern$BlockPatternMatch;)V"))
    private static void banner$cancelClear(Level level, BlockPattern.BlockPatternMatch patternMatch) {
        // Banner - do nothing
    }

    @Inject(method = "spawnGolemInWorld", cancellable = true, at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"))
    private static void banner$clearPattern(Level level, BlockPattern.BlockPatternMatch patternMatch, Entity golem, BlockPos pos, CallbackInfo ci) {
        if (!level.addFreshEntity(golem, CreatureSpawnEvent.SpawnReason.BUILD_IRONGOLEM)) {
            ci.cancel();
        }
        CarvedPumpkinBlock.clearPatternBlocks(level, patternMatch);
    }
}
