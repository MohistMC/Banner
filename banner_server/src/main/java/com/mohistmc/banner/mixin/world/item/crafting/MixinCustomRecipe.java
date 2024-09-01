package com.mohistmc.banner.mixin.world.item.crafting;

import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.CustomRecipe;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.inventory.CraftComplexRecipe;
import org.bukkit.inventory.Recipe;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(CustomRecipe.class)
public abstract class MixinCustomRecipe implements CraftingRecipe {

    @Override
    public Recipe toBukkitRecipe(NamespacedKey id) {
        return new CraftComplexRecipe(id, ((CustomRecipe) (Object) this));
    }
}
