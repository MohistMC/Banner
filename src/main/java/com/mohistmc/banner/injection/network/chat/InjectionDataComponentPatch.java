package com.mohistmc.banner.injection.network.chat;

import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;

public interface InjectionDataComponentPatch {

    default void copy(DataComponentPatch orig) {
    }

    default void clear(DataComponentType<?> type) {
    }

    default boolean isEmpty() {
        return false;
    }
}
