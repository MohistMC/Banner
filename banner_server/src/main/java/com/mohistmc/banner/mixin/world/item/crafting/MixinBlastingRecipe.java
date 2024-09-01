package com.mohistmc.banner.mixin.world.item.crafting;

import com.mohistmc.banner.bukkit.inventory.recipe.BannerModdedRecipe;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.BlastingRecipe;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeType;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.inventory.CraftBlastingRecipe;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.craftbukkit.inventory.CraftRecipe;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(BlastingRecipe.class)
public abstract class MixinBlastingRecipe extends AbstractCookingRecipe {

    public MixinBlastingRecipe(RecipeType<?> recipeType, String string, CookingBookCategory cookingBookCategory, Ingredient ingredient, ItemStack itemStack, float f, int i) {
        super(recipeType, string, cookingBookCategory, ingredient, itemStack, f, i);
    }

    @Override
    public org.bukkit.inventory.Recipe toBukkitRecipe(NamespacedKey id) {
        if (this.result.isEmpty()) {
            return new BannerModdedRecipe(id, (BlastingRecipe) (Object) this);
        }
        CraftItemStack result = CraftItemStack.asCraftMirror(this.result);

        CraftBlastingRecipe recipe =  new CraftBlastingRecipe(id, result, CraftRecipe.toBukkit(this.ingredient), this.experience, this.cookingTime);
        recipe.setGroup(this.group);
        recipe.setCategory(CraftRecipe.getCategory(this.category()));
        return recipe;
    }
}
