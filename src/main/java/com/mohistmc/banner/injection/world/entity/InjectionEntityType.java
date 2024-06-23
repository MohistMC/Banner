package com.mohistmc.banner.injection.world.entity;

import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MobSpawnType;
import org.jetbrains.annotations.Nullable;

public interface InjectionEntityType<T extends Entity> {

    @Nullable
    default T spawn(ServerLevel worldserver, BlockPos blockposition, MobSpawnType enummobspawn, org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason spawnReason) {
        throw new IllegalStateException("Not implemented");
    }

    @Nullable
    default  T spawn(ServerLevel worldserver, @Nullable CompoundTag nbttagcompound, @Nullable Consumer<T> consumer, BlockPos blockposition, MobSpawnType enummobspawn, boolean flag, boolean flag1, org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason spawnReason) {
        throw new IllegalStateException("Not implemented");
    }
}
