package com.mohistmc.banner.injection.world.level.block.entity;

import org.bukkit.entity.HumanEntity;

import java.util.List;

public interface InjectionShulkerBoxBlockEntity {

    default List<HumanEntity> bridge$transaction() {
        throw new IllegalStateException("Not implemented");
    }

    default void banner$setTransaction(List<HumanEntity> transaction) {
        throw new IllegalStateException("Not implemented");
    }

    default boolean bridge$opened() {
        throw new IllegalStateException("Not implemented");
    }

    default void banner$setOpened(boolean opened) {
        throw new IllegalStateException("Not implemented");
    }
}
