package com.mohistmc.banner.injection.world.level.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public interface InjectionContainerOpenersCounter {

    default boolean bridge$opened() {
        throw new IllegalStateException("Not implemented");
    }

    default void banner$setOpened(boolean opened) {
        throw new IllegalStateException("Not implemented");
    }

    default void onAPIOpen(Level world, BlockPos blockposition, BlockState iblockdata) {
        throw new IllegalStateException("Not implemented");
    }

    default void onAPIClose(Level world, BlockPos blockposition, BlockState iblockdata) {
        throw new IllegalStateException("Not implemented");
    }

    default void openerAPICountChanged(Level world, BlockPos blockposition, BlockState iblockdata, int i, int j) {
        throw new IllegalStateException("Not implemented");
    }
}
