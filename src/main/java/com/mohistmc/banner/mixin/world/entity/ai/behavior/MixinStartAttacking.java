package com.mohistmc.banner.mixin.world.entity.ai.behavior;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.StartAttacking;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryAccessor;
import org.bukkit.craftbukkit.v1_19_R3.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_19_R3.event.CraftEventFactory;
import org.bukkit.event.entity.EntityTargetEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

@Mixin(StartAttacking.class)
public class MixinStartAttacking {

    @Inject(method = "method_47123", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/ai/behavior/declarative/MemoryAccessor;set(Ljava/lang/Object;)V"),
            locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
    private static <E extends Mob> void banner$targetEvent(Predicate<E> predicate, Function<E, Optional<? extends LivingEntity>> function,
                                           MemoryAccessor memoryAccessor, MemoryAccessor memoryAccessor2,
                                           ServerLevel serverLevel, Mob mob, long l, CallbackInfoReturnable<Boolean> cir,
                                           Optional optional, LivingEntity livingEntity) {
        // CraftBukkit start
        EntityTargetEvent event = CraftEventFactory.callEntityTargetLivingEvent(mob, livingEntity, (livingEntity instanceof ServerPlayer) ? EntityTargetEvent.TargetReason.CLOSEST_PLAYER : EntityTargetEvent.TargetReason.CLOSEST_ENTITY);
        if (event.isCancelled()) {
            cir.setReturnValue(false);
        }
        if (event.getTarget() == null) {
            memoryAccessor.erase();
            cir.setReturnValue(true);
        }
        livingEntity = ((CraftLivingEntity) event.getTarget()).getHandle();
        // CraftBukkit end
    }
}
