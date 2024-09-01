package com.mohistmc.banner.mixin.world.entity.projectile;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.entity.projectile.ThrownEnderpearl;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ThrownEnderpearl.class)
public abstract class MixinThrownEnderpearl extends ThrowableItemProjectile {

    public MixinThrownEnderpearl(EntityType<? extends ThrowableItemProjectile> entityType, Level level) {
        super(entityType, level);
    }

    // Banner TODO fixme
    /*
    @Inject(method = "onHit", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"))
    private void banner$spawnEndermite(HitResult result, CallbackInfo ci) {
        this.level().pushAddEntityReason(CreatureSpawnEvent.SpawnReason.ENDER_PEARL);
    }*/
}
