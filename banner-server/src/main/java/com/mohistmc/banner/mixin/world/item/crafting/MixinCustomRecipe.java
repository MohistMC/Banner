package com.mohistmc.banner.mixin.world.item.crafting;

import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.CustomRecipe;
import org.bukkit.inventory.Recipe;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(CustomRecipe.class)
public abstract class MixinCustomRecipe implements CraftingRecipe {

    @Override
    public Recipe toBukkitRecipe() {
        return new org.bukkit.craftbukkit.v1_20_R1.inventory.CraftComplexRecipe(((CustomRecipe) (Object) this));
    }
}
