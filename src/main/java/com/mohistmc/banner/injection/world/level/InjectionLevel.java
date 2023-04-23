package com.mohistmc.banner.injection.world.level;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import org.bukkit.craftbukkit.v1_19_R3.CraftServer;
import org.bukkit.craftbukkit.v1_19_R3.CraftWorld;
import org.spigotmc.SpigotWorldConfig;

public interface InjectionLevel {

    default CraftWorld getWorld() {
        return null;
    }

    default CraftServer getCraftServer() {
        return null;
    }

    default void notifyAndUpdatePhysics(BlockPos blockposition, LevelChunk chunk, BlockState oldBlock, BlockState newBlock, BlockState actualBlock, int i, int j) {
    }

    default BlockEntity getBlockEntity(BlockPos blockposition, boolean validate) {
        return null;
    }

    default SpigotWorldConfig bridge$spigotConfig() {
        return null;
    }
}
