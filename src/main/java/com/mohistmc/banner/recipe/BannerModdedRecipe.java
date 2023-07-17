package com.mohistmc.banner.recipe;

import com.mohistmc.banner.bukkit.BukkitExtraConstants;
import net.minecraft.world.item.crafting.Recipe;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftComplexRecipe;
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_20_R1.util.CraftNamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class BannerModdedRecipe extends CraftComplexRecipe {

    private final Recipe<?> recipe;

    public BannerModdedRecipe(Recipe<?> recipe) {
        super(null);
        this.recipe = recipe;
    }

    @Override
    public @NotNull ItemStack getResult() {
        return CraftItemStack.asCraftMirror(this.recipe.getResultItem(BukkitExtraConstants.getServer().registryAccess()));
    }

    @Override
    public @NotNull NamespacedKey getKey() {
        return CraftNamespacedKey.fromMinecraft(this.recipe.getId());
    }

    @Override
    public void addToCraftingManager() {
        BukkitExtraConstants.getServer().getRecipeManager().addRecipe(this.recipe);
    }
}