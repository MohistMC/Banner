package com.mohistmc.banner.mixin.world.entity.moster;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(AbstractSkeleton.class)
public abstract class MixinAbstractSkeleton extends Monster {

    protected MixinAbstractSkeleton(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "performRangedAttack", cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD,
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/monster/AbstractSkeleton;playSound(Lnet/minecraft/sounds/SoundEvent;FF)V"))
    private void banner$shootBow(LivingEntity livingEntity, float f, CallbackInfo ci, ItemStack itemStack, ItemStack itemStack2, AbstractArrow abstractArrow, double d, double e, double g, double h) {
        EntityShootBowEvent event = CraftEventFactory.callEntityShootBowEvent((AbstractSkeleton) (Object) this, this.getMainHandItem(), null, abstractArrow, InteractionHand.MAIN_HAND, 0.8F, true);
        if (event.isCancelled()) {
            event.getProjectile().remove();
            ci.cancel();
            return;
        }
        if (event.getProjectile() !=  abstractArrow.getBukkitEntity()) {
            this.playSound(SoundEvents.SKELETON_SHOOT, 1.0F, 1.0F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
            ci.cancel();
        }
    }
}
