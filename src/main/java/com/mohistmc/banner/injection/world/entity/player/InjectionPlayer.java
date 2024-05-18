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
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftHumanEntity;
import org.bukkit.event.entity.EntityExhaustionEvent;

public interface InjectionPlayer extends InjectionLivingEntity {

    default boolean bridge$affectsSpawning() {
        return true;
    }

    default void banner$setAffectsSpawning(boolean affectsSpawning) {

    }

    @Override
    default CraftHumanEntity getBukkitEntity() {
        return null;
    }

    default void pushExhaustReason(EntityExhaustionEvent.ExhaustionReason reason) {
    }

    default ItemEntity drop(ItemStack itemstack, boolean flag, boolean flag1, boolean callEvent) {
        return null;
    }

    default Either<Player.BedSleepingProblem, Unit> startSleepInBed(BlockPos blockposition, boolean force) {
        return null;
    }

    default void causeFoodExhaustion(float f, EntityExhaustionEvent.ExhaustionReason reason) {

    }

    default boolean spawnEntityFromShoulder(CompoundTag nbttagcompound) { // CraftBukkit void->boolean
        return false;
    }

    default boolean bridge$fauxSleeping() {
        return false;
    }

    default void banner$setFauxSleeping(boolean fauxSleeping) {
    }

    default int bridge$oldLevel() {
        return 0;
    }

    default void banner$setOldLevel(int oldLevel) {
    }


    default Player forceSleepInBed(boolean force) {
        return null;
    }

    default AtomicBoolean bridge$startSleepInBed_force() {
        return null;
    }
}
