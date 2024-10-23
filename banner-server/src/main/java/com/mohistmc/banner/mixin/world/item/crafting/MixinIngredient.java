package com.mohistmc.banner.mixin.world.item.crafting;

import com.mohistmc.banner.injection.world.item.crafting.InjectionIngredient;
import net.minecraft.core.Holder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

import java.util.List;

@Mixin(Ingredient.class)
public class MixinIngredient implements InjectionIngredient {

    // CraftBukkit start
    @Nullable
    private List<ItemStack> itemStacks;

    private static Ingredient ofStacks(List<ItemStack> stacks) {
        Ingredient recipe = Ingredient.of(stacks.stream().map(ItemStack::getItem));
        recipe.banner$setItemStacks(stacks);
        return recipe;
    }

    @Override
    public List<ItemStack> bridge$itemStacks() {
        return itemStacks;
    }

    @Override
    public void banner$setItemStacks(List<ItemStack> itemStacks) {
        this.itemStacks = itemStacks;
    }
}
