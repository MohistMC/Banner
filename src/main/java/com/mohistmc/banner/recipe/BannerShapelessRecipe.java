package com.mohistmc.banner.recipe;

import com.mohistmc.banner.bukkit.BukkitExtraConstants;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.v1_19_R3.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_19_R3.inventory.CraftShapelessRecipe;
import org.bukkit.craftbukkit.v1_19_R3.util.CraftNamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class BannerShapelessRecipe extends CraftShapelessRecipe {

    private final ShapelessRecipe recipe;

    public BannerShapelessRecipe(ShapelessRecipe recipe) {
        super(null, recipe);
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