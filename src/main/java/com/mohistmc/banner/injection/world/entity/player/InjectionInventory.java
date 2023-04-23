package com.mohistmc.banner.injection.world.entity.player;

import net.minecraft.world.item.ItemStack;
import org.bukkit.craftbukkit.v1_19_R3.entity.CraftHumanEntity;
import org.bukkit.entity.HumanEntity;

import java.util.List;

public interface InjectionInventory {

    default List<ItemStack> getContents() {
        return null;
    }

    default List<ItemStack> getArmorContents() {
        return null;
    }

    default void onOpen(CraftHumanEntity who) {
    }

    default void onClose(CraftHumanEntity who) {
    }

    default List<HumanEntity> getViewers() {
        return null;
    }

    default org.bukkit.inventory.InventoryHolder getOwner(){
        return null;
    }

    default void setMaxStackSize(int size) {
    }

    default int canHold(ItemStack itemstack) {
        return 0;
    }

}
