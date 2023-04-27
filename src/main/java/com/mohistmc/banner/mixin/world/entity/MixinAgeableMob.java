package com.mohistmc.banner.mixin.world.entity;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mohistmc.banner.injection.world.entity.InjectionAgeableMob;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AgeableMob.class)
public class MixinAgeableMob implements InjectionAgeableMob {

    public boolean ageLocked; // CraftBukkit

    @Inject(method = "addAdditionalSaveData", at = @At("RETURN"))
    private void banner$writeAgeLocked(CompoundTag compound, CallbackInfo ci) {
        compound.putBoolean("AgeLocked", ageLocked);
    }

    @Inject(method = "readAdditionalSaveData", at = @At("RETURN"))
    private void banner$readAgeLocked(CompoundTag compound, CallbackInfo ci) {
        ageLocked = compound.getBoolean("AgeLocked");
    }

    @ModifyExpressionValue(method = "aiStep", at = @At(value = "FIELD", target = "Lnet/minecraft/world/level/Level;isClientSide:Z"))
    private boolean banner$tickIfNotLocked(Level world) {
        return world.isClientSide || ageLocked;
    }

    @Override
    public boolean bridge$ageLocked() {
        return ageLocked;
    }

    @Override
    public void banner$setAgeLocked(boolean ageLocked) {
        this.ageLocked = ageLocked;
    }
}
