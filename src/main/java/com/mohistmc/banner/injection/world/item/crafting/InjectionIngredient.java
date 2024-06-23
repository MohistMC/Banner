package com.mohistmc.banner.injection.world.item.crafting;

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
}
