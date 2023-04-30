package com.mohistmc.banner.injection.world.level.chunk;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public interface InjectionLevelChunk {

    default org.bukkit.Chunk getBukkitChunk() {
        return null;
    }

    default BlockState setBlockState(BlockPos pos, BlockState state, boolean isMoving, boolean doPlace) {
        return null;
    }

    default void loadCallback() {
    }

    default void unloadCallback() {
    }
}
