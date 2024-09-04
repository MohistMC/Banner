package com.mohistmc.banner.mixin.world.entity.moster;

import com.mohistmc.banner.asm.annotation.TransformAccess;
import io.izzel.arclight.mixin.Eject;
import java.util.UUID;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.level.Level;
import org.bukkit.entity.ZombieVillager;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.entity.EntityTransformEvent;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(net.minecraft.world.entity.monster.ZombieVillager.class)
public abstract class MixinZombieVillager extends Zombie {

    public MixinZombieVillager(EntityType<? extends Zombie> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "startConverting", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/monster/ZombieVillager;removeEffect(Lnet/minecraft/core/Holder;)Z"))
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

    // Banner TODO fixme
    /*
    @Inject(method = "finishConversion", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/monster/ZombieVillager;spawnAtLocation(Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/entity/item/ItemEntity;"))
    private void banner$dropPre(ServerLevel world, CallbackInfo ci) {
        this.banner$setForceDrops(true);
    }

    @Inject(method = "finishConversion", at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/world/entity/monster/ZombieVillager;spawnAtLocation(Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/entity/item/ItemEntity;"))
    private void banner$dropPost(ServerLevel world, CallbackInfo ci) {
        this.banner$setForceDrops(false);
    }*/

    @TransformAccess(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC)
    private static net.minecraft.world.entity.monster.ZombieVillager zombifyVillager(ServerLevel level, Villager villager, BlockPos blockPosition, boolean silent, CreatureSpawnEvent.SpawnReason spawnReason) {
        villager.level().pushAddEntityReason(spawnReason);
        villager.bridge$pushTransformReason(EntityTransformEvent.TransformReason.INFECTION);
        net.minecraft.world.entity.monster.ZombieVillager zombieVillager = villager.convertTo(EntityType.ZOMBIE_VILLAGER, false);
        if (zombieVillager != null) {
            zombieVillager.finalizeSpawn(level, level.getCurrentDifficultyAt(zombieVillager.blockPosition()), MobSpawnType.CONVERSION, new net.minecraft.world.entity.monster.Zombie.ZombieGroupData(false, true));
            zombieVillager.setVillagerData(villager.getVillagerData());
            zombieVillager.setGossips(villager.getGossips().store(NbtOps.INSTANCE));
            zombieVillager.setTradeOffers(villager.getOffers().copy());
            zombieVillager.setVillagerXp(villager.getVillagerXp());
            if (!silent) {
                level.levelEvent(null, 1026, blockPosition, 0);
            }
        }
        return zombieVillager;
    }
}
