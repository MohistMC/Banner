package com.mohistmc.banner.mixin.world.entity.ai.behavior;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.behavior.PrepareRamNearestTarget;
import org.bukkit.craftbukkit.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.event.entity.EntityTargetEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PrepareRamNearestTarget.class)
public class MixinPrepareRamNearestTarget {


    @Inject(method = "method_36270",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/ai/behavior/PrepareRamNearestTarget;chooseRamPosition(Lnet/minecraft/world/entity/PathfinderMob;Lnet/minecraft/world/entity/LivingEntity;)V"), cancellable = true)
    private void banner$targetEvent(PathfinderMob pathfinderMob, LivingEntity livingEntity, CallbackInfo ci) {
        // CraftBukkit start
        EntityTargetEvent event = CraftEventFactory.callEntityTargetLivingEvent(pathfinderMob, livingEntity, (livingEntity instanceof ServerPlayer) ? EntityTargetEvent.TargetReason.CLOSEST_PLAYER : EntityTargetEvent.TargetReason.CLOSEST_ENTITY);
        if (event.isCancelled() || event.getTarget() == null) {
            ci.cancel();
        }
        livingEntity = ((CraftLivingEntity) event.getTarget()).getHandle();
        // CraftBukkit end
    }
}
