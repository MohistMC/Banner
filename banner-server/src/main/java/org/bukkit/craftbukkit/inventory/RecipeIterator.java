package org.bukkit.craftbukkit.inventory;

import java.util.Iterator;
import java.util.Map;

import com.mohistmc.banner.bukkit.BukkitMethodHooks;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import org.bukkit.inventory.Recipe;

public class RecipeIterator implements Iterator<Recipe> {
    private final Iterator<Map.Entry<RecipeType<?>, RecipeHolder<?>>> recipes;

    public RecipeIterator() {
        this.recipes = BukkitMethodHooks.getServer().getRecipeManager().byType.entries().iterator();
    }

    @Override
    public boolean hasNext() {
        return this.recipes.hasNext();
    }

    @Override
    public Recipe next() {
        return this.recipes.next().getValue().toBukkitRecipe();
    }

    @Override
    public void remove() {
        this.recipes.remove();
    }
}
