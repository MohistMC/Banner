package com.mohistmc.banner.injection.world.inventory;

import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.bukkit.craftbukkit.entity.CraftHumanEntity;
import org.bukkit.inventory.InventoryView;

public interface InjectionAbstractContainerMenu {

    default boolean bridge$checkReachable() {
        return false;
    }

    default void banner$setCheckReachable(boolean checkReachable) {
    }

    default InventoryView getBukkitView() {
        return null;
    }

    default void setBukkitView(InventoryView view) {

    }

    default void transferTo(AbstractContainerMenu other, CraftHumanEntity player) {
    }

    default Component getTitle() {
        return null;
    }

    default void setTitle(Component title) {
    }

    default void broadcastCarriedItem() {
    }
}
