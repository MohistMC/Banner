package com.mohistmc.banner.mixin.world.entity.ai.behavior;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.ai.behavior.BabyFollowAdult;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryAccessor;
import org.bukkit.craftbukkit.v1_19_R3.entity.CraftAgeable;
import org.bukkit.craftbukkit.v1_19_R3.event.CraftEventFactory;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.function.Function;

@Mixin(BabyFollowAdult.class)
public class MixinBabyFollowAdult {

    @Inject(method = "method_46900", at = @At(value = "NEW",
            target = "Lnet/minecraft/world/entity/ai/memory/WalkTarget;<init>(Lnet/minecraft/world/entity/ai/behavior/PositionTracker;FI)V"),
            locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
    private static void banner$targetEvent(BehaviorBuilder.Instance instance, MemoryAccessor memoryAccessor,
                                           UniformInt uniformInt, Function function, MemoryAccessor memoryAccessor2,
                                           MemoryAccessor memoryAccessor3, ServerLevel serverLevel,
                                           AgeableMob ageableMob, long l, CallbackInfoReturnable<Boolean> cir,
                                           AgeableMob ageableMob2) {
        // CraftBukkit start
        EntityTargetLivingEntityEvent event = CraftEventFactory.callEntityTargetLivingEvent(ageableMob, ageableMob2, EntityTargetEvent.TargetReason.FOLLOW_LEADER);
        if (event.isCancelled()) {
            cir.setReturnValue(false);
        }
        if (event.getTarget() == null) {
            memoryAccessor.erase();
            cir.setReturnValue(true);
        }
        ageableMob2 = ((CraftAgeable) event.getTarget()).getHandle();
        // CraftBukkit end
    }

}
