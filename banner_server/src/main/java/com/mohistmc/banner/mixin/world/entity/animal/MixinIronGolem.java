package com.mohistmc.banner.mixin.world.entity.animal;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.AbstractGolem;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.level.Level;
import org.bukkit.event.entity.EntityTargetEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(IronGolem.class)
public abstract class MixinIronGolem extends AbstractGolem {

    protected MixinIronGolem(EntityType<? extends AbstractGolem> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "doPush", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/animal/IronGolem;setTarget(Lnet/minecraft/world/entity/LivingEntity;)V"))
    private void banner$targetReason(Entity entityIn, CallbackInfo ci) {
        bridge$pushGoalTargetReason(EntityTargetEvent.TargetReason.COLLISION, true);
    }
}
