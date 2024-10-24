package com.mohistmc.banner.injection.world.level.chunk;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

public interface InjectionLevelChunk {

    default org.bukkit.Chunk getBukkitChunk() {
        throw new IllegalStateException("Not implemented");
    }

    default ServerLevel banner$r() {
        throw new IllegalStateException("Not implemented");
    }

    default BlockState setBlockState(BlockPos pos, BlockState state, boolean isMoving, boolean doPlace) {
        throw new IllegalStateException("Not implemented");
    }

    default void loadCallback() {
        throw new IllegalStateException("Not implemented");
    }

    default void unloadCallback() {
        throw new IllegalStateException("Not implemented");
    }
}
