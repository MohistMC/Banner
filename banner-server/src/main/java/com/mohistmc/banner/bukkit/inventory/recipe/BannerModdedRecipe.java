package com.mohistmc.banner.bukkit.inventory.recipe;

import com.mohistmc.banner.bukkit.BukkitMethodHooks;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.inventory.CraftComplexRecipe;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.craftbukkit.util.CraftNamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class BannerModdedRecipe extends CraftComplexRecipe {

    private final Recipe<?> recipe;

    public BannerModdedRecipe(NamespacedKey id, Recipe<?> recipe) {
        super(id, (ItemStack) null, (CustomRecipe) recipe);
        this.recipe = recipe;
    }

    @Override
    public @NotNull ItemStack getResult() {
        return CraftItemStack.asCraftMirror(new RecipeHolder<>(CraftNamespacedKey.toMinecraft(this.getKey()), this.recipe).value().getResultItem(BukkitMethodHooks.getServer().registryAccess()));
    }

    @Override
    public void addToCraftingManager() {
        BukkitMethodHooks.getServer().getRecipeManager().addRecipe((new RecipeHolder<>(CraftNamespacedKey.toMinecraft(this.getKey()), this.recipe)));
    }
}