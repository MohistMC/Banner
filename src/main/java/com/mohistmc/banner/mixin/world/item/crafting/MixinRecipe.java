package com.mohistmc.banner.mixin.world.item.crafting;

import com.mohistmc.banner.injection.world.item.crafting.InjectionRecipe;
import net.minecraft.world.item.crafting.Recipe;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Recipe.class)
public interface MixinRecipe extends InjectionRecipe {

    @Override
    default org.bukkit.inventory.Recipe toBukkitRecipe() {
        return new org.bukkit.inventory.Recipe() {
            @Override
            public org.bukkit.inventory.ItemStack getResult() {
                return new org.bukkit.inventory.ItemStack(org.bukkit.Material.AIR);
            }
        };
    }

}
