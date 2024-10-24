package com.mohistmc.banner.mixin.world.item.crafting;

import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.CustomRecipe;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.inventory.CraftComplexRecipe;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.craftbukkit.inventory.CraftRecipe;
import org.bukkit.inventory.Recipe;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(CustomRecipe.class)
public abstract class MixinCustomRecipe implements CraftingRecipe {

    @Override
    public Recipe toBukkitRecipe(NamespacedKey id) {
        CraftItemStack result = CraftItemStack.asCraftMirror(getResultItem(RegistryAccess.EMPTY));
        CraftComplexRecipe recipe = new CraftComplexRecipe(id, result, ((CustomRecipe) (Object) this));
        recipe.setGroup(this.getGroup());
        recipe.setCategory(CraftRecipe.getCategory(this.category()));

        return recipe;
    }
}
