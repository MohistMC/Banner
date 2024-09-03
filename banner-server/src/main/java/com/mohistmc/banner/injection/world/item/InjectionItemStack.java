package com.mohistmc.banner.injection.world.item;

import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;

public interface InjectionItemStack {

    @Deprecated
    default void setItem(Item item) {
        throw new IllegalStateException("Not implemented");
    }

    default PatchedDataComponentMap getComponentsClone() {
        throw new IllegalStateException("Not implemented");
    }

    default void setComponentsClone(@Nullable PatchedDataComponentMap patchedDataComponentMap) {
        throw new IllegalStateException("Not implemented");
    }

    default void restorePatch(DataComponentPatch empty) {
        throw new IllegalStateException("Not implemented");
    }
}
