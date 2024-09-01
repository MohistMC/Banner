package com.mohistmc.banner.mixin.world.entity.npc;

import java.util.concurrent.atomic.AtomicReference;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.Level;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.entity.Villager;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.entity.EntityTransformEvent;
import org.bukkit.event.entity.VillagerReplenishTradeEvent;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(net.minecraft.world.entity.npc.Villager.class)
public abstract class MixinVillager extends AbstractVillager {

    @Shadow @Final private static Logger LOGGER;

    private AtomicReference<DamageSource> banner$damageSource = new AtomicReference<>();
    public MixinVillager(EntityType<? extends AbstractVillager> entityType, Level level) {
        super(entityType, level);
    }

    @Redirect(method = "thunderHit", at = @At(value = "INVOKE",
            target = "Lorg/slf4j/Logger;info(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)V",
            remap = false))
    private void banner$moveDown(Logger instance, String s, Object o1, Object o2) { }

    @Inject(method = "customServerAiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/npc/Villager;addEffect(Lnet/minecraft/world/effect/MobEffectInstance;)Z"))
    private void banner$reason(CallbackInfo ci) {
        pushEffectCause(EntityPotionEffectEvent.Cause.VILLAGER_TRADE);
    }

    @Redirect(method = "restock", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/trading/MerchantOffer;resetUses()V"))
    private void banner$restock(MerchantOffer instance) {
        VillagerReplenishTradeEvent event = new VillagerReplenishTradeEvent((Villager) this.getBukkitEntity(), instance.asBukkit());
        Bukkit.getPluginManager().callEvent(event);
        if (!event.isCancelled()) {
            instance.resetUses();
        }
    }

    @Redirect(method = "catchUpDemand", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/trading/MerchantOffer;resetUses()V"))
    private void banner$replenish(MerchantOffer instance) {
        VillagerReplenishTradeEvent event = new VillagerReplenishTradeEvent((Villager) this.getBukkitEntity(), instance.asBukkit());
        Bukkit.getPluginManager().callEvent(event);
        if (!event.isCancelled()) {
            instance.resetUses();
        }
    }

    @Inject(method = "thunderHit", cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD, at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;addFreshEntityWithPassengers(Lnet/minecraft/world/entity/Entity;)V"))
    private void banner$transformWitch(ServerLevel serverWorld, LightningBolt lightningBolt, CallbackInfo ci, Witch witchEntity) {
        if (CraftEventFactory.callEntityTransformEvent((net.minecraft.world.entity.npc.Villager) (Object) this, witchEntity, EntityTransformEvent.TransformReason.LIGHTNING).isCancelled()) {
            ci.cancel();
        } else {
            serverWorld.pushAddEntityReason(CreatureSpawnEvent.SpawnReason.LIGHTNING);
        }
    }

    @Inject(method = "spawnGolemIfNeeded", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/SpawnUtil;trySpawnMob(Lnet/minecraft/world/entity/EntityType;Lnet/minecraft/world/entity/MobSpawnType;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;IIILnet/minecraft/util/SpawnUtil$Strategy;)Ljava/util/Optional;"))
    private void banner$ironGolemReason(ServerLevel world, long p_35399_, int p_35400_, CallbackInfo ci) {
        world.pushAddEntityReason(CreatureSpawnEvent.SpawnReason.VILLAGE_DEFENSE);
    }

    @Inject(method = "spawnGolemIfNeeded", at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/util/SpawnUtil;trySpawnMob(Lnet/minecraft/world/entity/EntityType;Lnet/minecraft/world/entity/MobSpawnType;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;IIILnet/minecraft/util/SpawnUtil$Strategy;)Ljava/util/Optional;"))
    private void banner$ironGolemReasonReset(ServerLevel world, long p_35399_, int p_35400_, CallbackInfo ci) {
        world.pushAddEntityReason(null);
    }

    @Inject(method = "die", at = @At("HEAD"))
    private void banner$getSource(DamageSource damageSource, CallbackInfo ci) {
        banner$damageSource.set(damageSource);
    }

    @Redirect(method = "die", remap = false, at = @At(value = "INVOKE", target = "Lorg/slf4j/Logger;info(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)V"))
    private void banner$logVillagerDeaths(Logger instance, String s, Object o1, Object o2) {
        if (org.spigotmc.SpigotConfig.logVillagerDeaths)
            LOGGER.info("Villager {} died, message: '{}'", ((net.minecraft.world.entity.npc.Villager) (Object) this), banner$damageSource.get().getLocalizedDeathMessage(this).getString()); // Spigot
    }
}
