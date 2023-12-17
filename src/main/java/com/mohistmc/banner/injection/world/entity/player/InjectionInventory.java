package com.mohistmc.banner.injection.world.entity.player;

import java.util.List;
import net.minecraft.world.item.ItemStack;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftHumanEntity;
import org.bukkit.entity.HumanEntity;

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
