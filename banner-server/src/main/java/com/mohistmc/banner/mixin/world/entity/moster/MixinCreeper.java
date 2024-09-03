package com.mohistmc.banner.mixin.world.entity.moster;

import com.mohistmc.banner.injection.world.entity.monster.InjectionCreeper;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.PowerableMob;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreeperPowerEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Collection;

@Mixin(Creeper.class)
public abstract class MixinCreeper extends Monster implements PowerableMob, InjectionCreeper {

    // @formatter:off
    @Shadow @Final private static EntityDataAccessor<Boolean> DATA_IS_POWERED;
    @Shadow public int explosionRadius;
    @Shadow protected abstract void spawnLingeringCloud();
    @Shadow
    public int swell;
    @Shadow public abstract boolean isPowered();
    // @formatter:on

    protected MixinCreeper(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "thunderHit", cancellable = true, at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/monster/Creeper;entityData:Lnet/minecraft/network/syncher/SynchedEntityData;"))
    private void banner$lightningBolt(ServerLevel world, LightningBolt lightningBolt, CallbackInfo ci) {
        if (CraftEventFactory.callCreeperPowerEvent((Creeper) (Object) this, lightningBolt, CreeperPowerEvent.PowerCause.LIGHTNING).isCancelled()) {
            ci.cancel();
        }
    }

    @Inject(method = "explodeCreeper", locals = LocalCapture.CAPTURE_FAILHARD, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/monster/Creeper;isPowered()Z"), cancellable = true)
    public final void explodeCreeper(CallbackInfo ci) {
        ExplosionPrimeEvent event = new ExplosionPrimeEvent(this.getBukkitEntity(), this.explosionRadius * (this.isPowered() ? 2.0f : 1.0f), false);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            this.swell = 0;
            ci.cancel();
        }
    }

    @Inject(method = "spawnLingeringCloud", locals = LocalCapture.CAPTURE_FAILHARD, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"))
    private void banner$creeperCloud(CallbackInfo ci, Collection<MobEffectInstance> collection, AreaEffectCloud areaeffectcloudentity) {
        areaeffectcloudentity.setOwner((Creeper) (Object) this);
         this.level().pushAddEntityReason(CreatureSpawnEvent.SpawnReason.EXPLOSION);
    }

    @Override
    public void setPowered(boolean power) {
        this.entityData.set(DATA_IS_POWERED, power);
    }

}
