package com.mohistmc.banner.mixin.world.entity.animal;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Wolf.class)
public abstract class MixinWolf extends TamableAnimal {

    protected MixinWolf(EntityType<? extends TamableAnimal> entityType, Level level) {
        super(entityType, level);
    }

    @Redirect(method = "hurt", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/animal/Wolf;setOrderedToSit(Z)V"))
    private void banner$handledBy(Wolf wolfEntity, boolean fire) {
    }

    // CraftBukkit - add overriden version
    public boolean setTarget(LivingEntity entityliving, org.bukkit.event.entity.EntityTargetEvent.TargetReason reason, boolean fire) {
        if (!super.setTarget(entityliving, reason, fire)) {
            return false;
        }
        entityliving = getTarget();
          /* // PAIL
        if (entityliving == null) {
            this.setAngry(false);
        } else if (!this.isTamed()) {
            this.setAngry(true);
        }*/
        return true;
    }
    // CraftBukkit end

    @Inject(method = "mobInteract", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/animal/Wolf;heal(F)V"))
    private void banner$healReason(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        pushHealReason(EntityRegainHealthEvent.RegainReason.EATING);
    }

    @Inject(method = "mobInteract", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/animal/Wolf;setTarget(Lnet/minecraft/world/entity/LivingEntity;)V"))
    private void banner$attackReason(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        bridge$pushGoalTargetReason(EntityTargetEvent.TargetReason.FORGOT_TARGET, true);
    }
}
