package com.mohistmc.banner.injection;

import net.minecraft.world.item.ItemStack;

import java.util.List;

public interface SimpleContainerInjection {

    default List<ItemStack> getContents() {
        return null;
    }

    default void setMaxStackSize(int i) {

    }

}
