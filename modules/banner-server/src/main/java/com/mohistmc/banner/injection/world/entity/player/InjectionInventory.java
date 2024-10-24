package com.mohistmc.banner.injection.world.entity.player;

import java.util.List;
import net.minecraft.world.item.ItemStack;
import org.bukkit.craftbukkit.entity.CraftHumanEntity;
import org.bukkit.entity.HumanEntity;

public interface InjectionInventory {

    default List<ItemStack> getContents() {
        throw new IllegalStateException("Not implemented");
    }

    default List<ItemStack> getArmorContents() {
        throw new IllegalStateException("Not implemented");
    }

    default void onOpen(CraftHumanEntity who) {
        throw new IllegalStateException("Not implemented");
    }

    default void onClose(CraftHumanEntity who) {
        throw new IllegalStateException("Not implemented");
    }

    default List<HumanEntity> getViewers() {
        throw new IllegalStateException("Not implemented");
    }

    default org.bukkit.inventory.InventoryHolder getOwner(){
        throw new IllegalStateException("Not implemented");
    }

    default void setMaxStackSize(int size) {
        throw new IllegalStateException("Not implemented");
    }

    default int canHold(ItemStack itemstack) {
        throw new IllegalStateException("Not implemented");
    }

}
