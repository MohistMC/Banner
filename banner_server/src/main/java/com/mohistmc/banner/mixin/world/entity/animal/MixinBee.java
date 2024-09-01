package com.mohistmc.banner.mixin.world.entity.animal;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.level.Level;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Bee.class)
public abstract class MixinBee extends Animal {


    // @formatter:off
    @Shadow Bee.BeePollinateGoal beePollinateGoal;
    // @formatter:on

    protected MixinBee(EntityType<? extends Animal> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "doHurtTarget", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;addEffect(Lnet/minecraft/world/effect/MobEffectInstance;Lnet/minecraft/world/entity/Entity;)Z"))
    private void banner$sting(Entity entityIn, CallbackInfoReturnable<Boolean> cir) {
        ((LivingEntity) entityIn).pushEffectCause(EntityPotionEffectEvent.Cause.ATTACK);
    }

    /**
     * @author wdog5
     * @reason
     */
    @Overwrite
    public boolean hurt(DamageSource source, float amount) {
        if (this.isInvulnerableTo(source)) {
            return false;
        } else {
            Entity entity = source.getEntity();
            boolean ret = super.hurt(source, amount);
            if (ret && !this.level().isClientSide) {
                this.beePollinateGoal.stopPollinating();
            }
            return ret;
        }
    }
}
