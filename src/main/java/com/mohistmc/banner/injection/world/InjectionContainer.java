package com.mohistmc.banner.injection.world;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import org.bukkit.craftbukkit.v1_19_R3.entity.CraftHumanEntity;
import org.bukkit.inventory.InventoryView;

import java.util.Collections;

public interface InjectionContainer {

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

    default Recipe<?> getCurrentRecipe() {
        return null;
    }

    default void setCurrentRecipe(Recipe<?> recipe) {
    }

    default InventoryView getBukkitView() {
        return null;
    }
}
