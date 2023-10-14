package com.mohistmc.banner.mixin.world.entity.moster;

import io.izzel.arclight.mixin.Eject;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.Level;
import org.bukkit.entity.ZombieVillager;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.entity.EntityTransformEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(net.minecraft.world.entity.monster.ZombieVillager.class)
public abstract class MixinZombieVillager extends Zombie {

    public MixinZombieVillager(EntityType<? extends Zombie> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "startConverting", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/monster/ZombieVillager;removeEffect(Lnet/minecraft/world/effect/MobEffect;)Z"))
    private void banner$convert1(UUID conversionStarterIn, int conversionTimeIn, CallbackInfo ci) {
        this.banner$setPersist(true);
        pushEffectCause(EntityPotionEffectEvent.Cause.CONVERSION);
    }

    @Inject(method = "startConverting", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/monster/ZombieVillager;addEffect(Lnet/minecraft/world/effect/MobEffectInstance;)Z"))
    private void banner$convert2(UUID conversionStarterIn, int conversionTimeIn, CallbackInfo ci) {
        pushEffectCause(EntityPotionEffectEvent.Cause.CONVERSION);
    }

    @Eject(method = "finishConversion", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/monster/ZombieVillager;convertTo(Lnet/minecraft/world/entity/EntityType;Z)Lnet/minecraft/world/entity/Mob;"))
    private <T extends Mob> T banner$cure(net.minecraft.world.entity.monster.ZombieVillager zombieVillagerEntity, EntityType<T> entityType, boolean flag, CallbackInfo ci) {
        T t = this.convertTo(entityType, flag, EntityTransformEvent.TransformReason.CURED, CreatureSpawnEvent.SpawnReason.CURED);
        if (t == null) {
            ((ZombieVillager) this.getBukkitEntity()).setConversionTime(-1);
            ci.cancel();
        } else {
             t.pushEffectCause(EntityPotionEffectEvent.Cause.CONVERSION);
        }
        return t;
    }

    @Inject(method = "finishConversion", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/monster/ZombieVillager;spawnAtLocation(Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/entity/item/ItemEntity;"))
    private void banner$dropPre(ServerLevel world, CallbackInfo ci) {
        this.banner$setForceDrops(true);
    }

    @Inject(method = "finishConversion", at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/world/entity/monster/ZombieVillager;spawnAtLocation(Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/entity/item/ItemEntity;"))
    private void banner$dropPost(ServerLevel world, CallbackInfo ci) {
        this.banner$setForceDrops(false);
    }
}
