package com.mohistmc.banner.mixin.world.entity.ai.behavior;

import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.BabyFollowAdult;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.behavior.OneShot;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import org.bukkit.craftbukkit.v1_19_R3.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_19_R3.event.CraftEventFactory;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.function.Function;

@Mixin(BabyFollowAdult.class)
public class MixinBabyFollowAdult {

    /**
     * @author wdog5
     * @reason
     */
    @Overwrite
    public static OneShot<AgeableMob> create(UniformInt uniformInt, Function<LivingEntity, Float> entityFloatFunction) {
        return BehaviorBuilder.create((entity) -> {
            return entity.group(entity.present(MemoryModuleType.NEAREST_VISIBLE_ADULT), entity.registered(MemoryModuleType.LOOK_TARGET), entity.absent(MemoryModuleType.WALK_TARGET)).apply(entity, (value, p_258318_, p_258319_) -> {
                return (p_258326_, p_258327_, p_258328_) -> {
                    if (!p_258327_.isBaby()) {
                        return false;
                    } else {
                        LivingEntity ageablemob = entity.get(value);
                        if (p_258327_.closerThan(ageablemob, (double) (uniformInt.getMaxValue() + 1)) && !p_258327_.closerThan(ageablemob, (double) uniformInt.getMinValue())) {
                            // CraftBukkit start
                            EntityTargetLivingEntityEvent event = CraftEventFactory.callEntityTargetLivingEvent(p_258327_, ageablemob, EntityTargetEvent.TargetReason.FOLLOW_LEADER);
                            if (event.isCancelled()) {
                                return false;
                            }
                            if (event.getTarget() == null) {
                                value.erase();
                                return true;
                            }
                            ageablemob = ((CraftLivingEntity) event.getTarget()).getHandle();
                            // CraftBukkit end
                            WalkTarget walktarget = new WalkTarget(new EntityTracker(ageablemob, false), entityFloatFunction.apply(p_258327_), uniformInt.getMinValue() - 1);
                            p_258318_.set(new EntityTracker(ageablemob, true));
                            p_258319_.set(walktarget);
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
