package com.mohistmc.banner.injection.world.item.crafting;

import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;

public interface InjectionRecipeManager {

    default Map<RecipeType<?>, Object2ObjectLinkedOpenHashMap<ResourceLocation, RecipeHolder<?>>> bridge$recipesCB() {
        throw new IllegalStateException("Not implemented");
    }

    default void addRecipe(RecipeHolder<?> irecipe) {
        throw new IllegalStateException("Not implemented");
    }

    default boolean removeRecipe(ResourceLocation mcKey) {
        throw new IllegalStateException("Not implemented");
    }

    default void clearRecipes() {
        throw new IllegalStateException("Not implemented");
    }
}
