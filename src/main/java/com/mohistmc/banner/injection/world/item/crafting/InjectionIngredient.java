package com.mohistmc.banner.injection.world.item.crafting;

public interface InjectionIngredient {

    default boolean bridge$exact() {
        return false;
    }

    default void banner$setExact(boolean exact) {
    }

    default boolean isVanilla() {
        return false;
    }
}
