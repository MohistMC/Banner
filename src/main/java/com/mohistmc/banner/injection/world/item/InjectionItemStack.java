package com.mohistmc.banner.injection.world.item;

import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;

public interface InjectionItemStack {

    @Deprecated
    default void setItem(Item item) {
    }

    default PatchedDataComponentMap getComponentsClone() {
        return null;
    }

    default void setComponentsClone(@Nullable PatchedDataComponentMap patchedDataComponentMap) {
    }
}
