package com.mohistmc.banner.injection.world.level.block.entity;

import java.util.List;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public interface InjectionBeehiveBlockEntity {

    default int bridge$maxBees() {
        throw new IllegalStateException("Not implemented");
    }

    default void banner$setMaxBees(int maxBees) {
        throw new IllegalStateException("Not implemented");
    }

    default List<Entity> releaseBees(BlockState iblockdata, BeehiveBlockEntity.BeeReleaseStatus tileentitybeehive_releasestatus, boolean force) {
        throw new IllegalStateException("Not implemented");
    }
}
