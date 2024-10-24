package com.mohistmc.banner.mixin.world.item.crafting;

import com.mohistmc.banner.injection.world.item.crafting.InjectionRecipe;
import net.minecraft.world.Container;
import net.minecraft.world.item.crafting.Recipe;
import org.bukkit.NamespacedKey;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Recipe.class)
public interface MixinRecipe<C extends Container> extends InjectionRecipe {

    @Override
    default org.bukkit.inventory.Recipe toBukkitRecipe(NamespacedKey id) {
        return null;
    }
}
