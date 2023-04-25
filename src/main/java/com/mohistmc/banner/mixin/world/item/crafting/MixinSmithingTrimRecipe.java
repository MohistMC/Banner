package com.mohistmc.banner.mixin.world.item.crafting;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.SmithingRecipe;
import net.minecraft.world.item.crafting.SmithingTrimRecipe;
import org.bukkit.craftbukkit.v1_19_R3.inventory.CraftRecipe;
import org.bukkit.craftbukkit.v1_19_R3.inventory.CraftSmithingTrimRecipe;
import org.bukkit.craftbukkit.v1_19_R3.util.CraftNamespacedKey;
import org.bukkit.inventory.Recipe;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(SmithingTrimRecipe.class)
public abstract class MixinSmithingTrimRecipe implements SmithingRecipe {

    @Shadow @Final
    Ingredient addition;

    @Shadow @Final
    Ingredient base;

    @Shadow @Final
    Ingredient template;

    @Shadow @Final private ResourceLocation id;

    @Override
    public Recipe toBukkitRecipe() {
        return new CraftSmithingTrimRecipe(CraftNamespacedKey.fromMinecraft(this.id), CraftRecipe.toBukkit(this.template), CraftRecipe.toBukkit(this.base), CraftRecipe.toBukkit(this.addition));
    }
}
