package org.bukkit.craftbukkit.inventory;

import java.util.List;

import com.mohistmc.banner.bukkit.BukkitMethodHooks;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.util.CraftNamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapelessRecipe;

public class CraftShapelessRecipe extends ShapelessRecipe implements CraftRecipe {
    // TODO: Could eventually use this to add a matches() method or some such
    private net.minecraft.world.item.crafting.ShapelessRecipe recipe;

    public CraftShapelessRecipe(NamespacedKey key, ItemStack result) {
        super(key, result);
    }

    public CraftShapelessRecipe(NamespacedKey key, ItemStack result, net.minecraft.world.item.crafting.ShapelessRecipe recipe) {
        this(key, result);
        this.recipe = recipe;
    }

    public static CraftShapelessRecipe fromBukkitRecipe(ShapelessRecipe recipe) {
        if (recipe instanceof CraftShapelessRecipe) {
            return (CraftShapelessRecipe) recipe;
        }
        CraftShapelessRecipe ret = new CraftShapelessRecipe(recipe.getKey(), recipe.getResult());
        ret.setGroup(recipe.getGroup());
        ret.setCategory(recipe.getCategory());
        for (RecipeChoice ingred : recipe.getChoiceList()) {
            ret.addIngredient(ingred);
        }
        return ret;
    }

    @Override
    public void addToCraftingManager() {
        List<org.bukkit.inventory.RecipeChoice> ingred = this.getChoiceList();
        NonNullList<Ingredient> data = NonNullList.withSize(ingred.size(), Ingredient.EMPTY);
        for (int i = 0; i < ingred.size(); i++) {
            data.set(i, this.toNMS(ingred.get(i), true));
        }

        BukkitMethodHooks.getServer().getRecipeManager().addRecipe(new RecipeHolder<>(CraftNamespacedKey.toMinecraft(this.getKey()), new net.minecraft.world.item.crafting.ShapelessRecipe(this.getGroup(), CraftRecipe.getCategory(this.getCategory()), CraftItemStack.asNMSCopy(this.getResult()), data)));
    }
}
