package com.mohistmc.banner.injection.world.entity.player;

import com.mohistmc.banner.injection.world.entity.InjectionLivingEntity;
import com.mojang.datafixers.util.Either;
import java.util.concurrent.atomic.AtomicBoolean;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.bukkit.craftbukkit.entity.CraftHumanEntity;
import org.bukkit.event.entity.EntityExhaustionEvent;

public interface InjectionPlayer extends InjectionLivingEntity {

    default boolean bridge$affectsSpawning() {
        throw new IllegalStateException("Not implemented");
    }

    default void banner$setAffectsSpawning(boolean affectsSpawning) {
        throw new IllegalStateException("Not implemented");
    }

    @Override
    default CraftHumanEntity getBukkitEntity() {
        throw new IllegalStateException("Not implemented");
    }

    default void pushExhaustReason(EntityExhaustionEvent.ExhaustionReason reason) {
    }

    default ItemEntity drop(ItemStack itemstack, boolean flag, boolean flag1, boolean callEvent) {
        throw new IllegalStateException("Not implemented");
    }

    default Either<Player.BedSleepingProblem, Unit> startSleepInBed(BlockPos blockposition, boolean force) {
        throw new IllegalStateException("Not implemented");
    }

    default void causeFoodExhaustion(float f, EntityExhaustionEvent.ExhaustionReason reason) {
    }

    default boolean spawnEntityFromShoulder(CompoundTag nbttagcompound) { // CraftBukkit void->boolean
        throw new IllegalStateException("Not implemented");
    }

    default boolean bridge$fauxSleeping() {
        throw new IllegalStateException("Not implemented");
    }

    default void banner$setFauxSleeping(boolean fauxSleeping) {
        throw new IllegalStateException("Not implemented");
    }

    default int bridge$oldLevel() {
        throw new IllegalStateException("Not implemented");
    }

    default void banner$setOldLevel(int oldLevel) {
        throw new IllegalStateException("Not implemented");
    }

    default Player forceSleepInBed(boolean force) {
        throw new IllegalStateException("Not implemented");
    }

    default AtomicBoolean bridge$startSleepInBed_force() {
        throw new IllegalStateException("Not implemented");
    }
}
