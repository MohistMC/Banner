package com.mohistmc.banner.mixin.world.entity.moster;

import io.izzel.arclight.mixin.Eject;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.ZombieVillager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Zombie;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityCombustByEntityEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.EntityTransformEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(net.minecraft.world.entity.monster.Zombie.class)
public abstract class MixinZombie extends Monster {

    protected MixinZombie(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "convertToZombieType", at = @At("HEAD"))
    private void banner$transformReason(EntityType<? extends net.minecraft.world.entity.monster.Zombie> entityType, CallbackInfo ci) {
        this.bridge$pushTransformReason(EntityTransformEvent.TransformReason.DROWNED);
        this.level().pushAddEntityReason(CreatureSpawnEvent.SpawnReason.DROWNED);
    }

    @Inject(method = "convertToZombieType", locals = LocalCapture.CAPTURE_FAILHARD, at = @At("RETURN"))
    private void banner$stopConversion(EntityType<? extends net.minecraft.world.entity.monster.Zombie> entityType, CallbackInfo ci) {
        if (entityType == null) {
            ((Zombie) this.getBukkitEntity()).setConversionTime(-1);
        }
    }

    @Inject(method = "hurt", locals = LocalCapture.CAPTURE_FAILHARD, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/monster/Zombie;setTarget(Lnet/minecraft/world/entity/LivingEntity;)V"))
    private void banner$spawnWithReason(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir, ServerLevel serverLevel, LivingEntity livingEntity, int i, int j, int k, net.minecraft.world.entity.monster.Zombie zombie, int l, int m, int n, int o, BlockPos blockPos, EntityType entityType, SpawnPlacements.Type type) {
        serverLevel.pushAddEntityReason(CreatureSpawnEvent.SpawnReason.REINFORCEMENTS);
        if (livingEntity != null) {
            zombie.bridge$pushGoalTargetReason(EntityTargetEvent.TargetReason.REINFORCEMENT_TARGET, true);
        }
    }

    @Redirect(method = "doHurtTarget", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;setSecondsOnFire(I)V"))
    private void banner$entityCombust(Entity entity, int seconds) {
        EntityCombustByEntityEvent event = new EntityCombustByEntityEvent(this.getBukkitEntity(),  entity.getBukkitEntity(), seconds);
        Bukkit.getPluginManager().callEvent(event);
        if (!event.isCancelled()) {
             entity.setSecondsOnFire(event.getDuration(), false);
        }
    }

    @Eject(method = "killedEntity(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/LivingEntity;)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/npc/Villager;convertTo(Lnet/minecraft/world/entity/EntityType;Z)Lnet/minecraft/world/entity/Mob;"))
    private <T extends Mob> T banner$transform(Villager villagerEntity, EntityType<T> entityType, boolean flag, CallbackInfoReturnable<Boolean> cir) {
         villagerEntity.level().pushAddEntityReason(CreatureSpawnEvent.SpawnReason.INFECTION);
         villagerEntity.bridge$pushTransformReason(EntityTransformEvent.TransformReason.INFECTION);
        T t = villagerEntity.convertTo(entityType, flag);
        if (t == null) {
            cir.setReturnValue(false);
        }
        return t;
    }

    @Inject(method = "finalizeSpawn", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/ServerLevelAccessor;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"))
    private void banner$mount(ServerLevelAccessor worldIn, DifficultyInstance difficultyIn, MobSpawnType reason, SpawnGroupData spawnDataIn, CompoundTag dataTag, CallbackInfoReturnable<SpawnGroupData> cir) {
         worldIn.getLevel().pushAddEntityReason(CreatureSpawnEvent.SpawnReason.MOUNT);
    }

    private static ZombieVillager zombifyVillager(ServerLevel level, Villager villager, BlockPos blockPosition, boolean silent, CreatureSpawnEvent.SpawnReason spawnReason) {
        villager.level().pushAddEntityReason(spawnReason);
        villager.bridge$pushTransformReason(EntityTransformEvent.TransformReason.INFECTION);
        ZombieVillager zombieVillager = villager.convertTo(EntityType.ZOMBIE_VILLAGER, false);
        if (zombieVillager != null) {
            zombieVillager.finalizeSpawn(level, level.getCurrentDifficultyAt(zombieVillager.blockPosition()), MobSpawnType.CONVERSION, new net.minecraft.world.entity.monster.Zombie.ZombieGroupData(false, true), null);
            zombieVillager.setVillagerData(villager.getVillagerData());
            zombieVillager.setGossips(villager.getGossips().store(NbtOps.INSTANCE));
            zombieVillager.setTradeOffers(villager.getOffers().createTag());
            zombieVillager.setVillagerXp(villager.getVillagerXp());
            if (!silent) {
                level.levelEvent(null, 1026, blockPosition, 0);
            }
        }
        return zombieVillager;
    }
}
