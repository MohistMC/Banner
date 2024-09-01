package com.mohistmc.banner.mixin.world.entity.projectile;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.projectile.Fireball;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.event.entity.EntityCombustByEntityEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(SmallFireball.class)
public abstract class MixinSmallFireball extends Fireball {

    public MixinSmallFireball(EntityType<? extends Fireball> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "<init>(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/phys/Vec3;)V", at = @At("RETURN"))
    private void banner$init(Level level, LivingEntity livingEntity, Vec3 vec3, CallbackInfo ci) {
        if (this.getOwner() != null && this.getOwner() instanceof Mob) {
            this.banner$setIsIncendiary(this.level().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING));
        }
    }

    @Redirect(method = "onHitEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;igniteForSeconds(F)V"))
    private void banner$entityCombust(Entity entity, float seconds) {
        if (this.bridge$isIncendiary()) {
            EntityCombustByEntityEvent event = new EntityCombustByEntityEvent(this.getBukkitEntity(), entity.getBukkitEntity(), seconds);
            Bukkit.getPluginManager().callEvent(event);

            if (!event.isCancelled()) {
                 entity.banner$setSecondsOnFire((int) event.getDuration(), false);
            }
        }
    }

    @Inject(method = "onHitBlock", cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD,
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;setBlockAndUpdate(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)Z"))
    private void banner$burnBlock(BlockHitResult result, CallbackInfo ci, Entity entity, BlockPos pos) {
        if (!this.bridge$isIncendiary() || CraftEventFactory.callBlockIgniteEvent(this.level(), pos, (SmallFireball) (Object) this).isCancelled()) {
            ci.cancel();
        }
    }
}
