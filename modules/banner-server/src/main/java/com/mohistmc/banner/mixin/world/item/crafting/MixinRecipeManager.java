package com.mohistmc.banner.mixin.world.item.crafting;

import com.google.common.collect.ImmutableMap;
import com.mohistmc.banner.injection.world.item.crafting.InjectionRecipeManager;

import java.util.Collections;
import java.util.Map;

import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeMap;
import net.minecraft.world.item.crafting.RecipeType;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

// Banner TODO fix patch
@Mixin(RecipeManager.class)
public abstract class MixinRecipeManager implements InjectionRecipeManager {

    @Shadow @Final private HolderLookup.Provider registries;

    @Shadow public RecipeMap recipes;

    @Shadow public abstract void finalizeRecipeLoading(FeatureFlagSet featureFlagSet);

    public Map<RecipeType<?>, Object2ObjectLinkedOpenHashMap<ResourceLocation, RecipeHolder<?>>> recipesCB = ImmutableMap.of(); // CraftBukkit  // Mohist use obf name

    @Override
    public void addRecipe(RecipeHolder<?> irecipe) {
        org.spigotmc.AsyncCatcher.catchOp("Recipe Add"); // Spigot
        //this.recipes.addRecipe(irecipe);// Banner TODO fixme
        this.finalizeRecipeLoading();
    }

    @Override
    public boolean removeRecipe(ResourceKey<Recipe<?>> mcKey) {
        boolean removed = this.removeRecipe(mcKey);
        if (removed) {
            this.finalizeRecipeLoading();
        }

        return removed;
    }

    @Override
    public void clearRecipes() {
        this.recipes = RecipeMap.create(Collections.emptyList());
        this.finalizeRecipeLoading();
    }

    private FeatureFlagSet featureflagset;

    public void finalizeRecipeLoading() {
        if (this.featureflagset != null) {
            this.finalizeRecipeLoading(this.featureflagset);
        }
    }

    @Override
    public Map<RecipeType<?>, Object2ObjectLinkedOpenHashMap<ResourceLocation, RecipeHolder<?>>> bridge$recipesCB() {
        return recipesCB;
    }
}
