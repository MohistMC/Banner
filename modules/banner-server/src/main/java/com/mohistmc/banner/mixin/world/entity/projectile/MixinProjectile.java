package com.mohistmc.banner.mixin.world.entity.projectile;

import com.mohistmc.banner.injection.world.entity.projectile.InjectionProjectile;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileDeflection;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.bukkit.projectiles.ProjectileSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Projectile.class)
public abstract class MixinProjectile extends Entity implements InjectionProjectile {

    public MixinProjectile(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Shadow protected abstract void onHit(HitResult result);

    @Shadow protected abstract ProjectileDeflection hitTargetOrDeflectSelf(HitResult hitResult);

    private boolean hitCancelled = false;

    @Inject(method = "setOwner", at = @At("RETURN"))
    private void banner$updateSource(Entity entityIn, CallbackInfo ci) {
        this.banner$setProjectileSource((entityIn != null && entityIn.getBukkitEntity() instanceof ProjectileSource) ? (ProjectileSource) entityIn.getBukkitEntity() : null);
    }

    @Inject(method = "onHitBlock", cancellable = true, at = @At("HEAD"))
    private void banner$cancelBlockHit(BlockHitResult result, CallbackInfo ci) {
        if (hitCancelled) {
            ci.cancel();
        }
    }

    @Override
    public boolean hitCancelled() {
        return hitCancelled;
    }

    @Override
    public void banner$setHitCancelled(boolean cancelled) {
        hitCancelled = cancelled;
    }

    @Override
    public ProjectileDeflection preHitTargetOrDeflectSelf(HitResult movingobjectposition) {
        org.bukkit.event.entity.ProjectileHitEvent event = org.bukkit.craftbukkit.event.CraftEventFactory.callProjectileHitEvent(this, movingobjectposition);
        this.hitCancelled = event != null && event.isCancelled();
        if (movingobjectposition.getType() == HitResult.Type.BLOCK || !this.hitCancelled) {
            return this.hitTargetOrDeflectSelf(movingobjectposition);
        }
        return ProjectileDeflection.NONE;
    }
}
