package com.mohistmc.banner.mixin.world.entity.ai.behavior;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.StartAttacking;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import org.bukkit.craftbukkit.v1_19_R3.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_19_R3.event.CraftEventFactory;
import org.bukkit.event.entity.EntityTargetEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

@Mixin(StartAttacking.class)
public class MixinStartAttacking {

    /**
     * @author wdog5
     * @reason
     */
    @Overwrite
    public static <E extends Mob> BehaviorControl<E> create(Predicate<E> canAttack, Function<E, Optional<? extends LivingEntity>> targetFinder) {
        return BehaviorBuilder.create((instance) -> {
            return instance.group(instance.absent(MemoryModuleType.ATTACK_TARGET), instance.registered(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE)).apply(instance, (memoryAccessor, memoryAccessor2) -> {
                return (serverLevel, mob, l) -> {
                    if (!canAttack.test(mob)) {
                        return false;
                    } else {
                        Optional<? extends LivingEntity> optional = (Optional)targetFinder.apply(mob);
                        if (optional.isEmpty()) {
                            return false;
                        } else {
                            LivingEntity livingEntity = (LivingEntity)optional.get();
                            if (!mob.canAttack(livingEntity)) {
                                return false;
                            } else {
                                // CraftBukkit start
                                EntityTargetEvent event = CraftEventFactory.callEntityTargetLivingEvent(mob, livingEntity, (livingEntity instanceof ServerPlayer) ? EntityTargetEvent.TargetReason.CLOSEST_PLAYER : EntityTargetEvent.TargetReason.CLOSEST_ENTITY);
                                if (event.isCancelled()) {
                                    return false;
                                }
                                if (event.getTarget() == null) {
                                    memoryAccessor.erase();
                                    return true;
                                }
                                livingEntity = ((CraftLivingEntity) event.getTarget()).getHandle();
                                // CraftBukkit end
                                memoryAccessor.set(livingEntity);
                                memoryAccessor2.erase();
                                return true;
                            }
                        }
                    }
                };
            });
        });
    }
}
