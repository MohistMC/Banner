package com.mohistmc.banner.mixin.world.entity.animal;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Turtle;
import net.minecraft.world.level.Level;
import org.bukkit.craftbukkit.v1_19_R3.event.CraftEventFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
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

    @Inject(method = "thunderHit", at = @At("HEAD"))
    private void banner$lightning(ServerLevel world, LightningBolt lightningBolt, CallbackInfo ci) {
        CraftEventFactory.entityDamage = lightningBolt;
    }

    @Inject(method = "thunderHit", at = @At("RETURN"))
    private void banner$lightningReset(ServerLevel world, LightningBolt lightningBolt, CallbackInfo ci) {
        CraftEventFactory.entityDamage = null;
    }
}
