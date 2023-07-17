package com.mohistmc.banner.mixin.world.item.crafting;

import com.mohistmc.banner.recipe.BannerModdedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftFurnaceRecipe;
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftRecipe;
import org.bukkit.craftbukkit.v1_20_R1.util.CraftNamespacedKey;
import org.bukkit.inventory.Recipe;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(SmeltingRecipe.class)
public abstract class MixinSmeltingRecipe extends AbstractCookingRecipe {

    public MixinSmeltingRecipe(RecipeType<?> recipeType, ResourceLocation resourceLocation, String string, CookingBookCategory cookingBookCategory, Ingredient ingredient, ItemStack itemStack, float f, int i) {
        super(recipeType, resourceLocation, string, cookingBookCategory, ingredient, itemStack, f, i);
    }

    @Override
    public Recipe toBukkitRecipe() {
        if (this.result.isEmpty()) {
            return new BannerModdedRecipe((SmeltingRecipe) (Object) this);
        }
        CraftItemStack result = CraftItemStack.asCraftMirror(this.result);
        CraftFurnaceRecipe recipe = new CraftFurnaceRecipe(CraftNamespacedKey.fromMinecraft(this.id), result,
                CraftRecipe.toBukkit(this.ingredient), this.experience, this.cookingTime);
        recipe.setGroup(this.group);
        recipe.setCategory(CraftRecipe.getCategory(this.category()));

        return recipe;
    }
}
