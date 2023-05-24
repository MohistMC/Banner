package com.mohistmc.banner.mixin.world.entity.ai.behavior;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.StopAttackingIfTargetInvalid;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryAccessor;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import org.bukkit.craftbukkit.v1_19_R3.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_19_R3.event.CraftEventFactory;
import org.bukkit.event.entity.EntityTargetEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.function.BiConsumer;
import java.util.function.Predicate;

@Mixin(StopAttackingIfTargetInvalid.class)
public abstract class MixinStopAttackingIfTargetInvalid {

    @Inject(method = "method_47135", at = @At(value = "INVOKE",
           target = "Ljava/util/function/BiConsumer;accept(Ljava/lang/Object;Ljava/lang/Object;)V"),
           locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
    private static <E extends Mob> void banner$targetEvent(BehaviorBuilder.Instance instance, MemoryAccessor memoryAccessor,
                                          boolean bl, MemoryAccessor memoryAccessor2, Predicate<LivingEntity> predicate,
                                          BiConsumer<E, LivingEntity> biConsumer, ServerLevel serverLevel, Mob mob,
                                          long l, CallbackInfoReturnable<Boolean> cir, LivingEntity livingEntity) {
       // CraftBukkit start
       LivingEntity old = mob.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).orElse(null);
       EntityTargetEvent event = CraftEventFactory.callEntityTargetLivingEvent(mob, null, (old != null && !old.isAlive()) ? EntityTargetEvent.TargetReason.TARGET_DIED : EntityTargetEvent.TargetReason.FORGOT_TARGET);
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
