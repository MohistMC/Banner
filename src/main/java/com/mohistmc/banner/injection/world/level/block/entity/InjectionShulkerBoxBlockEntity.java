package com.mohistmc.banner.injection.world.level.block.entity;

import java.util.List;
import org.bukkit.entity.HumanEntity;

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
