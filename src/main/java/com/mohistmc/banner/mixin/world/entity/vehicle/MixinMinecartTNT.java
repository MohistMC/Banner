package com.mohistmc.banner.mixin.world.entity.vehicle;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.MinecartTNT;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import org.bukkit.Bukkit;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.atomic.AtomicReference;

@Mixin(MinecartTNT.class)
public abstract class MixinMinecartTNT extends AbstractMinecart {

    @Shadow
    public int fuse;

    protected MixinMinecartTNT(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    private AtomicReference<ExplosionPrimeEvent> banner$event = new AtomicReference<>();

    @Redirect(method = "explode(Lnet/minecraft/world/damagesource/DamageSource;D)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;explode(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/damagesource/DamageSource;Lnet/minecraft/world/level/ExplosionDamageCalculator;DDDFZLnet/minecraft/world/level/Level$ExplosionInteraction;)Lnet/minecraft/world/level/Explosion;"))
    private Explosion banner$explode(Level level, Entity source, DamageSource damageSource,
                                     ExplosionDamageCalculator damageCalculator, double x, double y, double z,
                                     float radius, boolean fire, Level.ExplosionInteraction interaction) {
        var event = new ExplosionPrimeEvent(this.getBukkitEntity(), radius, fire);
        banner$event.set(event);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            this.fuse = -1;
            return null;
        }
        return level.explode((MinecartTNT) (Object) this, x, y, z, event.getRadius(), event.getFire(), interaction);
    }

    @Inject(method = "explode(Lnet/minecraft/world/damagesource/DamageSource;D)V",
            at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/vehicle/MinecartTNT;discard()V"),
            cancellable = true)
    private void banner$cancelIfNotExplode(DamageSource damageSource, double d, CallbackInfo ci) {
        if (banner$event.get().isCancelled()) {
            ci.cancel();
        }
    }
}
