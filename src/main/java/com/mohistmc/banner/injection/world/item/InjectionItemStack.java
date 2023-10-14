package com.mohistmc.banner.injection.world.item;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;

public interface InjectionItemStack {

    default void convertStack(int version) {
    }

    default void load(CompoundTag nbttagcompound) {
    }

    default CompoundTag getTagClone() {
        return null;
    }

    default void setTagClone(@Nullable CompoundTag nbtttagcompound) {
    }

    @Deprecated
    default void setItem(Item item) {
    }
}
