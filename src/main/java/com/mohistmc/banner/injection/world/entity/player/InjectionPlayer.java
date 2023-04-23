package com.mohistmc.banner.injection.world.entity.player;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Unit;
import net.minecraft.core.BlockPos;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.bukkit.craftbukkit.v1_19_R3.entity.CraftHumanEntity;
import org.bukkit.event.entity.EntityExhaustionEvent;

public interface InjectionPlayer {

    default CraftHumanEntity getBukkitEntity() {
        return null;
    }

    default ItemEntity drop(ItemStack itemstack, boolean flag, boolean flag1, boolean callEvent) {
        return null;
    }

    default boolean damageEntity0(DamageSource damagesource, float f) {
        return false;
    }

    default Either<Player.BedSleepingProblem, Unit> startSleepInBed(BlockPos blockposition, boolean force) {
        return null;
    }

    default void causeFoodExhaustion(float f, EntityExhaustionEvent.ExhaustionReason reason) {

    }
}
