package com.mohistmc.banner.mixin.world.item.crafting;

import com.mohistmc.banner.bukkit.inventory.recipe.BannerModdedRecipe;
import com.mohistmc.banner.bukkit.inventory.recipe.BannerShapelessRecipe;
import com.mohistmc.banner.injection.world.item.crafting.InjectionShapelessRecipe;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftRecipe;
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftShapelessRecipe;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ShapelessRecipe.class)
public abstract class MixinShapelessRecipe implements CraftingRecipe, InjectionShapelessRecipe {

    @Shadow @Final
    ItemStack result;

    @Shadow @Final
    String group;

    @Shadow @Final
    NonNullList<Ingredient> ingredients;

    @Override
    // CraftBukkit start
    public org.bukkit.inventory.Recipe toBukkitRecipe() {
        if (this.result.isEmpty()) {
            return new BannerModdedRecipe(((ShapelessRecipe) (Object) this));
        }
        CraftItemStack result = CraftItemStack.asCraftMirror(this.result);
        CraftShapelessRecipe recipe = new CraftShapelessRecipe(result, ((ShapelessRecipe) (Object) this));
        recipe.setGroup(this.group);
        recipe.setCategory(CraftRecipe.getCategory(this.category()));

        for (Ingredient list : this.ingredients) {
            recipe.addIngredient(CraftRecipe.toBukkit(list));
        }
        return recipe;
    }
    // CraftBukkit end
}
