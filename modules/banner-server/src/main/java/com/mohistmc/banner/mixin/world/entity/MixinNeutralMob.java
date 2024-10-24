package com.mohistmc.banner.mixin.world.entity;

import com.mohistmc.banner.injection.world.entity.InjectionNeutralMob;
import io.izzel.arclight.mixin.Decorate;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.NeutralMob;
import org.bukkit.event.entity.EntityTargetEvent;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(NeutralMob.class)
public interface MixinNeutralMob extends InjectionNeutralMob {

    @Shadow void setTarget(@Nullable LivingEntity livingEntity);

    @Decorate(method = "readPersistentAngerSaveData", inject = true, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/NeutralMob;setTarget(Lnet/minecraft/world/entity/LivingEntity;)V"))
    private void banner$targetReason() {
        if (this instanceof Mob b) {
            b.bridge$pushGoalTargetReason(EntityTargetEvent.TargetReason.UNKNOWN, false);
        }
    }

    @Override
    boolean setTarget(@Nullable LivingEntity entityliving, EntityTargetEvent.TargetReason reason, boolean fireEvent);

}
