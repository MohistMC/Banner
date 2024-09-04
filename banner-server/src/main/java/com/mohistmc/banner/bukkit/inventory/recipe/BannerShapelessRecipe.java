package com.mohistmc.banner.bukkit.inventory.recipe;

import com.mohistmc.banner.bukkit.BukkitMethodHooks;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.craftbukkit.inventory.CraftShapelessRecipe;
import org.bukkit.craftbukkit.util.CraftNamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class BannerShapelessRecipe extends CraftShapelessRecipe {

    private final ShapelessRecipe recipe;

    public BannerShapelessRecipe(NamespacedKey id, ShapelessRecipe recipe) {
        super(id, null);
        this.recipe = recipe;
    }

    @Override
    public @NotNull ItemStack getResult() {
        return CraftItemStack.asCraftMirror(this.recipe.getResultItem(BukkitMethodHooks.getServer().registryAccess()));
    }

    @Override
    public void addToCraftingManager() {
        BukkitMethodHooks.getServer().getRecipeManager().addRecipe((new RecipeHolder<>(CraftNamespacedKey.toMinecraft(this.getKey()), this.recipe)));
    }
}