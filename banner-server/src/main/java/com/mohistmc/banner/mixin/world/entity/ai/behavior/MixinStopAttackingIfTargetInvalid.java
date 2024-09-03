package com.mohistmc.banner.mixin.world.entity.ai.behavior;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.StopAttackingIfTargetInvalid;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import org.bukkit.craftbukkit.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.event.entity.EntityTargetEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(StopAttackingIfTargetInvalid.class)
public abstract class MixinStopAttackingIfTargetInvalid {

    @Shadow
    private static boolean isTiredOfTryingToReachTarget(LivingEntity entity, Optional<Long> timeSinceInvalidTarget) { return false; }

    /**
     * @author Mgazul
     * @reason
     */
    @Overwrite
    public static <E extends Mob> BehaviorControl<E> create(Predicate<LivingEntity> canStopAttacking, BiConsumer<E, LivingEntity> onStopAttacking, boolean canGrowTiredOfTryingToReachTarget) {
        return BehaviorBuilder.create((instance) -> {
            return instance.group(instance.present(MemoryModuleType.ATTACK_TARGET), instance.registered(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE)).apply(instance, (memoryAccessor, memoryAccessor2) -> {
                return (serverLevel, mob, l) -> {
                    LivingEntity livingentity = instance.get(memoryAccessor);
                    if (mob.canAttack(livingentity) && (!canGrowTiredOfTryingToReachTarget || !isTiredOfTryingToReachTarget(mob, instance.tryGet(memoryAccessor2))) && livingentity.isAlive() && livingentity.level() == mob.level() && !canStopAttacking.test(livingentity)) {
                        return true;
                    } else {
                        // CraftBukkit start
                        LivingEntity old = mob.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).orElse(null);
                        EntityTargetEvent event = CraftEventFactory.callEntityTargetLivingEvent(mob, null, (old != null && !old.isAlive()) ? EntityTargetEvent.TargetReason.TARGET_DIED : EntityTargetEvent.TargetReason.FORGOT_TARGET);
                        if (event.isCancelled()) {
                            return false;
                        }
                        if (event.getTarget() != null) {
                            livingentity.getBrain().setMemory(MemoryModuleType.ATTACK_TARGET, ((CraftLivingEntity) event.getTarget()).getHandle());
                            return true;
                        }
                        // CraftBukkit end
                        onStopAttacking.accept(mob, livingentity);
                        memoryAccessor.erase();
                        return true;
                    }
                };
            });
        });
    }
}
