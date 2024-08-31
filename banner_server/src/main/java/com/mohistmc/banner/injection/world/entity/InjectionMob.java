package com.mohistmc.banner.injection.world.entity;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.EntityTransformEvent;

public interface InjectionMob extends InjectionNeutralMob {

    default boolean getBanner$targetSuccess() {
        throw new IllegalStateException("Not implemented");
    }

    default boolean bridge$aware() {
        throw new IllegalStateException("Not implemented");
    }

    default void banner$setAware(boolean aware){
        throw new IllegalStateException("Not implemented");
    }

    default void setPersistenceRequired(boolean persistenceRequired) {
        throw new IllegalStateException("Not implemented");
    }

    @Override
    default boolean setTarget(LivingEntity entityliving, EntityTargetEvent.TargetReason reason, boolean fireEvent) {
        throw new IllegalStateException("Not implemented");
    }

    default SoundEvent getAmbientSound0() {
        throw new IllegalStateException("Not implemented");
    }

    default ItemStack equipItemIfPossible(ItemStack itemstack, ItemEntity entityitem) {
        throw new IllegalStateException("Not implemented");
    }

    default <T extends Mob> T convertTo(EntityType<T> entitytypes, boolean flag, EntityTransformEvent.TransformReason transformReason, CreatureSpawnEvent.SpawnReason spawnReason) {
        throw new IllegalStateException("Not implemented");
    }

    default void bridge$pushGoalTargetReason(EntityTargetEvent.TargetReason reason, boolean fireEvent) {
        throw new IllegalStateException("Not implemented");
    }

    default void bridge$pushTransformReason(EntityTransformEvent.TransformReason transformReason) {
        throw new IllegalStateException("Not implemented");
    }
}
