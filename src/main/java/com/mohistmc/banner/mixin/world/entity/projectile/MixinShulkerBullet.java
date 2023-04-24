package com.mohistmc.banner.mixin.world.entity.projectile;

import com.mohistmc.banner.injection.world.entity.projectile.InjectionShulkerBullet;
import net.minecraft.core.Direction;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ShulkerBullet;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ShulkerBullet.class)
public abstract class MixinShulkerBullet extends Projectile implements InjectionShulkerBullet {

    @Shadow @Nullable private Entity finalTarget;

    @Shadow @Nullable private Direction currentMoveDirection;

    public MixinShulkerBullet(EntityType<? extends Projectile> entityType, Level level) {
        super(entityType, level);
    }

    @Shadow protected abstract void selectNextMoveDirection(@Nullable Direction.Axis axis);

    @Override
    public Entity getTarget() {
        return this.finalTarget;
    }

    @Override
    public void setTarget(Entity e) {
        this.finalTarget = e;
        this.currentMoveDirection = Direction.UP;
        this.selectNextMoveDirection(Direction.Axis.X);
    }

    @Inject(method = "hurt", at = @At("HEAD"), cancellable = true)
    private void banner$hurt(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (org.bukkit.craftbukkit.v1_19_R3.event.CraftEventFactory.handleNonLivingEntityDamageEvent(this, source, amount, false)) {
            cir.setReturnValue(false);
        }
    }
}
