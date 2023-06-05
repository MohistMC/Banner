package org.bukkit.craftbukkit.v1_19_R3.inventory;

import com.mohistmc.banner.BannerServer;
import com.mohistmc.banner.bukkit.BukkitExtraConstants;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.crafting.CustomRecipe;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.v1_19_R3.util.CraftNamespacedKey;
import org.bukkit.inventory.ComplexRecipe;
import org.bukkit.inventory.ItemStack;

public class CraftComplexRecipe implements CraftRecipe, ComplexRecipe {

    private final CustomRecipe recipe;

    public CraftComplexRecipe(CustomRecipe recipe) {
        this.recipe = recipe;
    }

    @Override
    public ItemStack getResult() {
        return CraftItemStack.asCraftMirror(recipe.getResultItem(RegistryAccess.EMPTY));
    }

    @Override
    public NamespacedKey getKey() {
        return CraftNamespacedKey.fromMinecraft(recipe.getId());
    }

    @Override
    public void addToCraftingManager() {
        BukkitExtraConstants.getServer().getRecipeManager().addRecipe(recipe);
    }
}
