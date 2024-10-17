package com.mohistmc.banner.mixin.world.entity.boss.wither;

import io.izzel.arclight.mixin.Decorate;
import io.izzel.arclight.mixin.DecorationOps;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WitherBoss.class)
public abstract class MixinWitherBoss extends Monster {

    protected MixinWitherBoss(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
    }

    @Decorate(method = "customServerAiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;explode(Lnet/minecraft/world/entity/Entity;DDDFZLnet/minecraft/world/level/Level$ExplosionInteraction;)Lnet/minecraft/world/level/Explosion;"))
    private Explosion banner$fireExplosionPrimeEvent(Level instance, Entity source, double x, double y, double z, float radius, boolean fire, Level.ExplosionInteraction explosionInteraction) throws Throwable {
        ExplosionPrimeEvent event = new ExplosionPrimeEvent(this.getBukkitEntity(), 7.0F, false);
        instance.getCraftServer().getPluginManager().callEvent(event);

        if (!event.isCancelled()) {
            return (Explosion) DecorationOps.callsite().invoke(instance, source, x, y, z, event.getRadius(), event.getFire(), explosionInteraction);
        }
        // CraftBukkit end
        return null;
    }

    @Decorate(method = "customServerAiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/boss/wither/WitherBoss;setAlternativeTarget(II)V"))
    private void banner$targetLivingEvent(WitherBoss instance, int i, int entityId) throws Throwable {
        if (i > 0 && entityId != 0) {
            if (CraftEventFactory.callEntityTargetLivingEvent((WitherBoss) (Object) this, (LivingEntity) this.level().getEntity(entityId), EntityTargetEvent.TargetReason.CLOSEST_ENTITY).isCancelled()) {
                return;
            }
        }
        DecorationOps.callsite().invoke(instance, i, entityId);
    }

    @Decorate(method = "customServerAiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;destroyBlock(Lnet/minecraft/core/BlockPos;ZLnet/minecraft/world/entity/Entity;)Z"))
    private boolean banner$damageBlock(Level instance, BlockPos blockPos, boolean b, Entity entity) throws Throwable {
        if (!CraftEventFactory.callEntityChangeBlockEvent((WitherBoss) (Object) this, blockPos, Blocks.AIR.defaultBlockState())) {
            return false;
        }
        return (boolean) DecorationOps.callsite().invoke(instance, blockPos, b, entity);
    }

    @Inject(method = "customServerAiStep", at = @At(value = "INVOKE", ordinal = 0, target = "Lnet/minecraft/world/entity/boss/wither/WitherBoss;heal(F)V"))
    private void banner$healReason(CallbackInfo ci) {
        pushHealReason(EntityRegainHealthEvent.RegainReason.WITHER_SPAWN);
    }

    @Inject(method = "customServerAiStep", at = @At(value = "INVOKE", ordinal = 1, target = "Lnet/minecraft/world/entity/boss/wither/WitherBoss;heal(F)V"))
    private void banner$healReason0(CallbackInfo ci) {
        pushHealReason(EntityRegainHealthEvent.RegainReason.REGEN);
    }
}
