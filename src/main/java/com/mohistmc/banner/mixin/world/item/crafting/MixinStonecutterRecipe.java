package com.mohistmc.banner.mixin.world.item.crafting;

import com.mohistmc.banner.recipe.BannerModdedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleItemRecipe;
import net.minecraft.world.item.crafting.StonecutterRecipe;
import org.bukkit.craftbukkit.v1_19_R3.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_19_R3.inventory.CraftRecipe;
import org.bukkit.craftbukkit.v1_19_R3.inventory.CraftStonecuttingRecipe;
import org.bukkit.craftbukkit.v1_19_R3.util.CraftNamespacedKey;
import org.bukkit.inventory.Recipe;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(StonecutterRecipe.class)
public abstract class MixinStonecutterRecipe extends SingleItemRecipe {

    public MixinStonecutterRecipe(RecipeType<?> recipeType, RecipeSerializer<?> recipeSerializer, ResourceLocation resourceLocation, String string, Ingredient ingredient, ItemStack itemStack) {
        super(recipeType, recipeSerializer, resourceLocation, string, ingredient, itemStack);
    }

    @Override
    public Recipe toBukkitRecipe() {
        if (this.result.isEmpty()) {
            return new BannerModdedRecipe((StonecutterRecipe) (Object) this);
        }
        CraftItemStack result = CraftItemStack.asCraftMirror(this.result);

        CraftStonecuttingRecipe recipe = new CraftStonecuttingRecipe(CraftNamespacedKey.fromMinecraft(this.id), result, CraftRecipe.toBukkit(this.ingredient));
        recipe.setGroup(this.group);

        return recipe;
    }
}
