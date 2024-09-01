package com.mohistmc.banner.mixin.world.entity.projectile;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Fireball;
import net.minecraft.world.entity.projectile.LargeFireball;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Bukkit;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LargeFireball.class)
public abstract class MixinLargeFireball extends Fireball {

    public MixinLargeFireball(EntityType<? extends Fireball> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "<init>(Lnet/minecraft/world/entity/EntityType;Lnet/minecraft/world/level/Level;)V", at = @At("RETURN"))
    private void banner$init(EntityType entityType, Level level, CallbackInfo ci) {
        this.banner$setIsIncendiary(level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING));
    }

    @Inject(method = "<init>(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/phys/Vec3;I)V", at = @At("RETURN"))
    private void banner$init(Level level, LivingEntity livingEntity, Vec3 vec3, int i, CallbackInfo ci) {
        this.banner$setIsIncendiary(level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING));
    }

    @Redirect(method = "onHit", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;explode(Lnet/minecraft/world/entity/Entity;DDDFZLnet/minecraft/world/level/Level$ExplosionInteraction;)Lnet/minecraft/world/level/Explosion;"))
    private Explosion banner$explodePrime(Level world, Entity entityIn, double xIn, double yIn, double zIn, float explosionRadius, boolean causesFire, Level.ExplosionInteraction interaction) {
        ExplosionPrimeEvent event = new ExplosionPrimeEvent((org.bukkit.entity.Explosive) this.getBukkitEntity());
        event.setRadius(explosionRadius);
        event.setFire(causesFire);
        Bukkit.getPluginManager().callEvent(event);

        if (!event.isCancelled()) {
            return this.level().explode((LargeFireball) (Object) this, xIn, yIn, zIn, event.getRadius(), event.getFire(), interaction);
        } else {
            return null;
        }
    }

    @Inject(method = "readAdditionalSaveData", at = @At(value = "INVOKE", target = "Lnet/minecraft/nbt/CompoundTag;getByte(Ljava/lang/String;)B"))
    private void banner$setYield(CompoundTag compound, CallbackInfo ci) {
        this.banner$setBukkitYield(compound.getInt("ExplosionPower"));
    }
}
