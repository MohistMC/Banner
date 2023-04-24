package com.mohistmc.banner.mixin.world.entity.projectile;

import com.mohistmc.banner.injection.world.entity.projectile.InjectionProjectile;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Projectile.class)
public abstract class MixinProjectile implements InjectionProjectile {

    @Shadow protected abstract void onHit(HitResult result);

    private boolean hitCancelled = false;

    @Override
    public boolean hitCancelled() {
        return hitCancelled;
    }

    @Override
    public void banner$setHitCancelled(boolean cancelled) {
        hitCancelled = cancelled;
    }

    @Override
    public void preOnHit(HitResult movingobjectposition) {
        org.bukkit.event.entity.ProjectileHitEvent event =
                org.bukkit.craftbukkit.v1_19_R3.event.CraftEventFactory.callProjectileHitEvent(((Projectile) (Object) this),
                        movingobjectposition);
        this.hitCancelled = event != null && event.isCancelled();
        if (movingobjectposition.getType() == HitResult.Type.BLOCK || !this.hitCancelled) {
            this.onHit(movingobjectposition);
        }
    }
}
