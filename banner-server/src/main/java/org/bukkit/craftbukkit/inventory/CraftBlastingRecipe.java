package org.bukkit.craftbukkit.inventory;

import com.mohistmc.banner.bukkit.BukkitMethodHooks;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.util.CraftNamespacedKey;
import org.bukkit.inventory.BlastingRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;

public class CraftBlastingRecipe extends BlastingRecipe implements CraftRecipe {
    public CraftBlastingRecipe(NamespacedKey key, ItemStack result, RecipeChoice source, float experience, int cookingTime) {
        super(key, result, source, experience, cookingTime);
    }

    public static CraftBlastingRecipe fromBukkitRecipe(BlastingRecipe recipe) {
        if (recipe instanceof CraftBlastingRecipe) {
            return (CraftBlastingRecipe) recipe;
        }
        CraftBlastingRecipe ret = new CraftBlastingRecipe(recipe.getKey(), recipe.getResult(), recipe.getInputChoice(), recipe.getExperience(), recipe.getCookingTime());
        ret.setGroup(recipe.getGroup());
        ret.setCategory(recipe.getCategory());
        return ret;
    }

    @Override
    public void addToCraftingManager() {
        ItemStack result = this.getResult();

        BukkitMethodHooks.getServer().getRecipeManager().addRecipe(new RecipeHolder<>(CraftNamespacedKey.toMinecraft(this.getKey()), new net.minecraft.world.item.crafting.BlastingRecipe(this.getGroup(), CraftRecipe.getCategory(this.getCategory()), this.toNMS(this.getInputChoice(), true), CraftItemStack.asNMSCopy(result), this.getExperience(), this.getCookingTime())));
    }
}
