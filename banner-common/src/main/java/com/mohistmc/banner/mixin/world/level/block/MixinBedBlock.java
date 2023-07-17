package com.mohistmc.banner.mixin.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BedBlock.class)
public class MixinBedBlock {

    @Inject(method = "kickVillagerOutOfBed", at = @At(value = "HEAD"))
    private void banner$addCapture(Level level, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (level.bridge$captureBlockStates()) {
            cir.cancel();
        }
    }
}
