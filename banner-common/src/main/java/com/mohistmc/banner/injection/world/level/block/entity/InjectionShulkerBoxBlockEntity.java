package com.mohistmc.banner.injection.world.level.block.entity;

import org.bukkit.entity.HumanEntity;

import java.util.List;

public interface InjectionShulkerBoxBlockEntity {

    default List<HumanEntity> bridge$transaction() {
        return null;
    }

    default void banner$setTransaction(List<HumanEntity> transaction) {
    }

    default boolean bridge$opened() {
        return false;
    }

    default void banner$setOpened(boolean opened) {
    }
}
