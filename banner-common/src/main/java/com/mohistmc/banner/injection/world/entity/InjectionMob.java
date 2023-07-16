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

public interface InjectionMob {

    default boolean getBanner$targetSuccess() {
        return false;
    }

    default boolean bridge$aware() {
        return false;
    }

    default void banner$setAware(boolean aware){
    }

    default void setPersistenceRequired(boolean persistenceRequired) {
    }

    default boolean setTarget(LivingEntity entityliving, EntityTargetEvent.TargetReason reason, boolean fireEvent) {
        return false;
    }

    default SoundEvent getAmbientSound0() {
        return null;
    }

    default ItemStack equipItemIfPossible(ItemStack itemstack, ItemEntity entityitem) {
        return itemstack;
    }

    default <T extends Mob> T convertTo(EntityType<T> entitytypes, boolean flag, EntityTransformEvent.TransformReason transformReason, CreatureSpawnEvent.SpawnReason spawnReason) {
        return null;
    }

    default void bridge$pushGoalTargetReason(EntityTargetEvent.TargetReason reason, boolean fireEvent) {
    }

    default void bridge$pushTransformReason(EntityTransformEvent.TransformReason transformReason) {
    }
}
