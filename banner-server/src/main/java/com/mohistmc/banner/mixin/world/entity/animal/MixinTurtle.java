package com.mohistmc.banner.mixin.world.entity.animal;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Turtle;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Turtle.class)
public abstract class MixinTurtle extends Animal {

    protected MixinTurtle(EntityType<? extends Animal> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "ageBoundaryReached", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/animal/Turtle;spawnAtLocation(Lnet/minecraft/world/level/ItemLike;I)Lnet/minecraft/world/entity/item/ItemEntity;"))
    private void banner$forceDrop(CallbackInfo ci) {
        this.banner$setForceDrops(true);
    }

    @Inject(method = "ageBoundaryReached", at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/world/entity/animal/Turtle;spawnAtLocation(Lnet/minecraft/world/level/ItemLike;I)Lnet/minecraft/world/entity/item/ItemEntity;"))
    private void banner$forceDropReset(CallbackInfo ci) {
        this.banner$setForceDrops(false);
    }

    @Redirect(method = "thunderHit", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/damagesource/DamageSources;lightningBolt()Lnet/minecraft/world/damagesource/DamageSource;"))
    private DamageSource banner$lightning(DamageSources instance, ServerLevel serverLevel, LightningBolt lightningBolt) {
        return instance.lightningBolt().bridge$customCausingEntity(lightningBolt);
    }
}
