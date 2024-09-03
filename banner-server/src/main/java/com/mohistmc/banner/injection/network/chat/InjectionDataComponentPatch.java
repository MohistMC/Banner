package com.mohistmc.banner.injection.network.chat;

import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;

public interface InjectionDataComponentPatch {

    default void copy(DataComponentPatch orig) {
        throw new IllegalStateException("Not implemented");
    }

    default void clear(DataComponentType<?> type) {
        throw new IllegalStateException("Not implemented");
    }

    default boolean isEmpty() {
        throw new IllegalStateException("Not implemented");
    }

    default boolean isSet(DataComponentType<?> type) {
        throw new IllegalStateException("Not implemented");
    }
}
