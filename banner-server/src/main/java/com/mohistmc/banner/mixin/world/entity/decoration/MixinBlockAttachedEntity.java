package com.mohistmc.banner.mixin.world.entity.decoration;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.decoration.BlockAttachedEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Bukkit;
import org.bukkit.entity.Hanging;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockAttachedEntity.class)
public abstract class MixinBlockAttachedEntity extends Entity {

    public MixinBlockAttachedEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @ModifyConstant(method = "tick", constant = @Constant(intValue = 100))
    private int banner$modifyTick(int constant) {
        return this.level().bridge$spigotConfig().hangingTickFrequency;
    }


    @Inject(method = "tick", cancellable = true, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/decoration/BlockAttachedEntity;discard()V"))
    private void banner$hangingBreak(CallbackInfo ci) {
        BlockState material = this.level().getBlockState(new BlockPos(this.blockPosition()));
        HangingBreakEvent.RemoveCause cause;
        if (!material.isAir()) {
            cause = HangingBreakEvent.RemoveCause.OBSTRUCTION;
        } else {
            cause = HangingBreakEvent.RemoveCause.PHYSICS;
        }
        HangingBreakEvent event = new HangingBreakEvent((Hanging) this.getBukkitEntity(), cause);
        Bukkit.getPluginManager().callEvent(event);
        if (this.isRemoved() || event.isCancelled()) {
            ci.cancel();
        }
    }

    @Inject(method = "hurtServer", cancellable = true, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/decoration/BlockAttachedEntity;kill(Lnet/minecraft/server/level/ServerLevel;)V"))
    private void banner$hangingBreakByAttack(ServerLevel serverLevel, DamageSource damageSource, float f, CallbackInfoReturnable<Boolean> cir) {
        Entity damager = (damageSource.isDirect()) ? damageSource.getEntity() : damageSource.getDirectEntity();
        HangingBreakEvent event;
        if (damager != null) {
            event = new HangingBreakByEntityEvent((Hanging) this.getBukkitEntity(), damager.getBukkitEntity(), damageSource.is(DamageTypeTags.IS_EXPLOSION) ? HangingBreakEvent.RemoveCause.EXPLOSION : HangingBreakEvent.RemoveCause.ENTITY);
        } else {
            event = new HangingBreakEvent((Hanging) this.getBukkitEntity(), damageSource.is(DamageTypeTags.IS_EXPLOSION) ? HangingBreakEvent.RemoveCause.EXPLOSION : HangingBreakEvent.RemoveCause.DEFAULT);
        }
        Bukkit.getPluginManager().callEvent(event);
        if (this.isRemoved() || event.isCancelled()) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "move", cancellable = true, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/decoration/BlockAttachedEntity;kill(Lnet/minecraft/server/level/ServerLevel;)V"))
    private void banner$hangingBreakByMove(MoverType typeIn, Vec3 pos, CallbackInfo ci) {
        if (this.isRemoved()) {
            ci.cancel();
            return;
        }
        HangingBreakEvent event = new HangingBreakEvent((Hanging) this.getBukkitEntity(), HangingBreakEvent.RemoveCause.PHYSICS);
        Bukkit.getPluginManager().callEvent(event);
        if (this.isRemoved() || event.isCancelled()) {
            ci.cancel();
        }
    }

    @Inject(method = "push", cancellable = true, at = @At("HEAD"))
    private void banner$noVelocity(double x, double y, double z, CallbackInfo ci) {
        ci.cancel();
    }

    private static double a(int i) {
        return i % 32 == 0 ? 0.5D : 0.0D;
    }

    private static AABB calculateBoundingBox(Entity entity, BlockPos blockPosition, Direction direction, int width, int height) {
        double d0 = blockPosition.getX() + 0.5;
        double d2 = blockPosition.getY() + 0.5;
        double d3 = blockPosition.getZ() + 0.5;
        double d4 = 0.46875;
        double d5 = a(width);
        double d6 = a(height);
        d0 -= direction.getStepX() * 0.46875;
        d3 -= direction.getStepZ() * 0.46875;
        d2 += d6;
        Direction enumdirection = direction.getCounterClockWise();
        d0 += d5 * enumdirection.getStepX();
        d3 += d5 * enumdirection.getStepZ();
        if (entity != null) {
            entity.setPosRaw(d0, d2, d3);
        }
        double d7 = width;
        double d8 = height;
        double d9 = width;
        if (direction.getAxis() == Direction.Axis.Z) {
            d9 = 1.0;
        } else {
            d7 = 1.0;
        }
        d7 /= 32.0;
        d8 /= 32.0;
        d9 /= 32.0;
        return new AABB(d0 - d7, d2 - d8, d3 - d9, d0 + d7, d2 + d8, d3 + d9);
    }
}
