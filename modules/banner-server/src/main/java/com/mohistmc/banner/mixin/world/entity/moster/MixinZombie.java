package com.mohistmc.banner.mixin.world.entity.moster;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Local;
import com.mohistmc.banner.asm.annotation.TransformAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.ConversionParams;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.ZombieVillager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Zombie;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityCombustByEntityEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.EntityTransformEvent;
import org.objectweb.asm.Opcodes;
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

    @Inject(method = "hurtServer", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/monster/Zombie;setTarget(Lnet/minecraft/world/entity/LivingEntity;)V"))
    private void banner$spawnWithReason(ServerLevel serverLevel, DamageSource damageSource, float f, CallbackInfoReturnable<Boolean> cir, @Local LivingEntity livingEntity, @Local(ordinal = 1) net.minecraft.world.entity.monster.Zombie zombie) {
        serverLevel.pushAddEntityReason(CreatureSpawnEvent.SpawnReason.REINFORCEMENTS);
        if (livingEntity != null) {
            zombie.bridge$pushGoalTargetReason(EntityTargetEvent.TargetReason.REINFORCEMENT_TARGET, true);
        }
    }

    @Redirect(method = "doHurtTarget", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;igniteForSeconds(F)V"))
    private void banner$entityCombust(Entity entity, float seconds) {
        EntityCombustByEntityEvent event = new EntityCombustByEntityEvent(this.getBukkitEntity(),  entity.getBukkitEntity(), seconds);
        Bukkit.getPluginManager().callEvent(event);
        if (!event.isCancelled()) {
             entity.banner$setSecondsOnFire(event.getDuration(), false);
        }
    }

    @Redirect(method = "killedEntity(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/LivingEntity;)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/monster/Zombie;convertVillagerToZombieVillager(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/npc/Villager;)Z"))
    private <T extends Mob> boolean banner$transform(net.minecraft.world.entity.monster.Zombie instance, ServerLevel serverLevel, Villager villager) {
        villager.level().pushAddEntityReason(CreatureSpawnEvent.SpawnReason.INFECTION);
        villager.bridge$pushTransformReason(EntityTransformEvent.TransformReason.INFECTION);
        T t = villager.convertTo(entityType, flag);
        if (t == null) {
            cir.setReturnValue(false);
        }
        return t;
    }

    @Inject(method = "finalizeSpawn", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/ServerLevelAccessor;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"))
    private void banner$mount(ServerLevelAccessor serverLevelAccessor, DifficultyInstance difficultyInstance, EntitySpawnReason mobSpawnType, SpawnGroupData spawnGroupData, CallbackInfoReturnable<SpawnGroupData> cir) {
        serverLevelAccessor.getLevel().pushAddEntityReason(CreatureSpawnEvent.SpawnReason.MOUNT);
    }

    @TransformAccess(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC)
    private static ZombieVillager convertVillagerToZombieVillager(ServerLevel level, Villager villager, BlockPos blockPosition, boolean silent, CreatureSpawnEvent.SpawnReason spawnReason) {
        villager.level().pushAddEntityReason(spawnReason);
        villager.bridge$pushTransformReason(EntityTransformEvent.TransformReason.INFECTION);
        ZombieVillager zombieVillager = villager.convertTo(EntityType.ZOMBIE_VILLAGER, ConversionParams.single(villager, true, true) , (entityzombievillager1) -> {
            entityzombievillager1.finalizeSpawn(level, level.getCurrentDifficultyAt(entityzombievillager1.blockPosition()), EntitySpawnReason.CONVERSION, new Zombie.ZombieGroupData(false, true));
            entityzombievillager1.setVillagerData(villager.getVillagerData());
            entityzombievillager1.setGossips(villager.getGossips().store(NbtOps.INSTANCE));
            entityzombievillager1.setTradeOffers(villager.getOffers().copy());
            entityzombievillager1.setVillagerXp(villager.getVillagerXp());
            // CraftBukkit start
            if (!silent) {
                level.levelEvent((Player) null, 1026, blockPosition, 0);
            }
        });
        return zombieVillager;
    }

    @WrapWithCondition(method = "registerGoals",
            at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/ai/goal/GoalSelector;addGoal(ILnet/minecraft/world/entity/ai/goal/Goal;)V",
                    ordinal = 0))
    private boolean banner$configTurtleEgg(GoalSelector instance, int priority, Goal goal) {
        return this.level().bridge$bannerConfig().zombiesTargetTurtleEggs;
    }
}
