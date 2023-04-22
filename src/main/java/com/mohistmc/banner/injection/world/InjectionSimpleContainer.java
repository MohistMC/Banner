package com.mohistmc.banner.injection.world;

import net.minecraft.world.item.ItemStack;

import java.util.List;

public interface InjectionSimpleContainer {

    default List<ItemStack> getContents() {
        return null;
    }

    default void setMaxStackSize(int i) {

    }

}
