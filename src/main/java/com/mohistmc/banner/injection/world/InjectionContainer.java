package com.mohistmc.banner.injection.world;

import java.util.Collections;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.bukkit.craftbukkit.entity.CraftHumanEntity;
import org.bukkit.inventory.InventoryView;

public interface InjectionContainer {

    int MAX_STACK = 64;

    default java.util.List<ItemStack> getContents() {
        return Collections.emptyList();
    }

    default void onOpen(CraftHumanEntity who) {
    }

    default void onClose(CraftHumanEntity who) {
    }

    default java.util.List<org.bukkit.entity.HumanEntity> getViewers() {
        return Collections.emptyList();
    }

    default org.bukkit.inventory.InventoryHolder getOwner() {
        return null;
    }

    default void setOwner(org.bukkit.inventory.InventoryHolder owner) {
    }

    default void setMaxStackSize(int size) {
    }

    default org.bukkit.Location getLocation() {
        return null;
    }

    default RecipeHolder<?> getCurrentRecipe() {
        return null;
    }

    default void setCurrentRecipe(RecipeHolder<?> recipe) {
    }

    default InventoryView getBukkitView() {
        return null;
    }
}
