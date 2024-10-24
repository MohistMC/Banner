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
        throw new IllegalStateException("Not implemented");
    }

    default void onClose(CraftHumanEntity who) {
        throw new IllegalStateException("Not implemented");
    }

    default java.util.List<org.bukkit.entity.HumanEntity> getViewers() {
        return Collections.emptyList();
    }

    default org.bukkit.inventory.InventoryHolder getOwner() {
        throw new IllegalStateException("Not implemented");
    }

    default void setOwner(org.bukkit.inventory.InventoryHolder owner) {
        throw new IllegalStateException("Not implemented");
    }

    default void setMaxStackSize(int size) {
        throw new IllegalStateException("Not implemented");
    }

    default org.bukkit.Location getLocation() {
        throw new IllegalStateException("Not implemented");
    }

    default RecipeHolder<?> getCurrentRecipe() {
        throw new IllegalStateException("Not implemented");
    }

    default void setCurrentRecipe(RecipeHolder<?> recipe) {
        throw new IllegalStateException("Not implemented");
    }

    default InventoryView getBukkitView() {
        throw new IllegalStateException("Not implemented");
    }
}
