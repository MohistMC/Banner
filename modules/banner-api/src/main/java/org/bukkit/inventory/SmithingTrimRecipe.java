package org.bukkit.inventory;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a smithing trim recipe.
 */
public class SmithingTrimRecipe extends SmithingRecipe implements ComplexRecipe {

    private final RecipeChoice template;

    /**
     * Create a smithing recipe to produce the specified result ItemStack.
     *
     * @param key The unique recipe key
     * @param template The template item.
     * @param base The base ingredient
     * @param addition The addition ingredient
     */
    public SmithingTrimRecipe(@NotNull NamespacedKey key, @Nullable RecipeChoice template, @Nullable RecipeChoice base, @Nullable RecipeChoice addition) {
        super(key, new ItemStack(Material.AIR), base, addition);
        this.template = template;
    }

    /**
     * Get the template recipe item.
     *
     * @return template choice
     */
    @Nullable
    public RecipeChoice getTemplate() {
        return (template != null) ? template.clone() : null;
    }
}
