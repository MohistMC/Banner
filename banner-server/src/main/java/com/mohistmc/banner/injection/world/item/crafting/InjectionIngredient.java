package com.mohistmc.banner.injection.world.item.crafting;

import net.minecraft.world.item.ItemStack;

import java.util.List;

public interface InjectionIngredient {

    default boolean bridge$exact() {
        throw new IllegalStateException("Not implemented");
    }

    default void banner$setExact(boolean exact) {
        throw new IllegalStateException("Not implemented");
    }

    default boolean isVanilla() {
        throw new IllegalStateException("Not implemented");
    }

    default List<ItemStack> bridge$itemStacks() {
        throw new IllegalStateException("Not implemented");
    }

    default void banner$setItemStacks(List<ItemStack> itemStacks) {
        throw new IllegalStateException("Not implemented");
    }
}
