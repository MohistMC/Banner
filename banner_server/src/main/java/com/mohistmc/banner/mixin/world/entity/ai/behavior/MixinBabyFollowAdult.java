package com.mohistmc.banner.mixin.world.entity.ai.behavior;

import java.util.function.Function;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.BabyFollowAdult;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.behavior.OneShot;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import org.bukkit.craftbukkit.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(BabyFollowAdult.class)
public class MixinBabyFollowAdult {

    /**
     * @author wdog5
     * @reason bukkit
     */
    @Overwrite
    public static OneShot<AgeableMob> create(UniformInt uniformInt, Function<LivingEntity, Float> entityFloatFunction) {
        return BehaviorBuilder.create((entity) -> {
            return entity.group(entity.present(MemoryModuleType.NEAREST_VISIBLE_ADULT), entity.registered(MemoryModuleType.LOOK_TARGET), entity.absent(MemoryModuleType.WALK_TARGET)).apply(entity, (memoryAccessor, memoryAccessor1, memoryAccessor2) -> {
                return (serverLevel, ageableMob, flag) -> {
                    if (!ageableMob.isBaby()) {
                        return false;
                    } else {
                        LivingEntity ageablemob = entity.get(memoryAccessor);
                        if (ageableMob.closerThan(ageablemob, (double) (uniformInt.getMaxValue() + 1)) && !ageableMob.closerThan(ageablemob, (double) uniformInt.getMinValue())) {
                            // CraftBukkit start
                            EntityTargetLivingEntityEvent event = CraftEventFactory.callEntityTargetLivingEvent(ageableMob, ageablemob, EntityTargetEvent.TargetReason.FOLLOW_LEADER);
                            if (event.isCancelled()) {
                                return false;
                            }
                            if (event.getTarget() == null) {
                                memoryAccessor.erase();
                                return true;
                            }
                            ageablemob = ((CraftLivingEntity) event.getTarget()).getHandle();
                            // CraftBukkit end
                            WalkTarget walktarget = new WalkTarget(new EntityTracker(ageablemob, false), entityFloatFunction.apply(ageableMob), uniformInt.getMinValue() - 1);
                            memoryAccessor1.set(new EntityTracker(ageablemob, true));
                            memoryAccessor2.set(walktarget);
                            return true;
                        } else {
                            return false;
                        }
                    }
                };
            });
        });
    }

}
