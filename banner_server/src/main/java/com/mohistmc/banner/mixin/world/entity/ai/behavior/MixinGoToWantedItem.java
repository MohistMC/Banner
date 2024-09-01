package com.mohistmc.banner.mixin.world.entity.ai.behavior;

import java.util.function.Predicate;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.GoToWantedItem;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryAccessor;
import net.minecraft.world.entity.animal.allay.Allay;
import net.minecraft.world.entity.item.ItemEntity;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.event.entity.EntityTargetEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(GoToWantedItem.class)
public class MixinGoToWantedItem {

    @Inject(method = "method_46945", at = @At(value = "NEW",
            args = "class=net/minecraft/world/entity/ai/memory/WalkTarget"),
            locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
    private static void banner$targetEvent(BehaviorBuilder.Instance instance, MemoryAccessor memoryAccessor,
                                           MemoryAccessor memoryAccessor2, Predicate predicate, int i, float f,
                                           MemoryAccessor memoryAccessor3, MemoryAccessor memoryAccessor4,
                                           ServerLevel serverLevel, LivingEntity livingEntity, long l,
                                           CallbackInfoReturnable<Boolean> cir, ItemEntity itemEntity) {
        // CraftBukkit start
        if (livingEntity instanceof Allay) {
            EntityTargetEvent event = CraftEventFactory.callEntityTargetEvent(livingEntity, itemEntity, EntityTargetEvent.TargetReason.CLOSEST_ENTITY);
            if (event.isCancelled()) {
                cir.setReturnValue(false);
            }
            if (!(event.getTarget() instanceof ItemEntity)) {
                memoryAccessor2.erase();
            }

            itemEntity = (ItemEntity) ((CraftEntity) event.getTarget()).getHandle();
        }
        // CraftBukkit end
    }

}
