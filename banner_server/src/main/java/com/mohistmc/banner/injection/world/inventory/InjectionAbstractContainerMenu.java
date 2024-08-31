package com.mohistmc.banner.injection.world.inventory;

import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.bukkit.craftbukkit.entity.CraftHumanEntity;
import org.bukkit.inventory.InventoryView;

public interface InjectionAbstractContainerMenu {

    default boolean bridge$checkReachable() {
        throw new IllegalStateException("Not implemented");
    }

    default void banner$setCheckReachable(boolean checkReachable) {
        throw new IllegalStateException("Not implemented");
    }

    default InventoryView getBukkitView() {
        throw new IllegalStateException("Not implemented");
    }

    default void setBukkitView(InventoryView view) {
        throw new IllegalStateException("Not implemented");
    }

    default void transferTo(AbstractContainerMenu other, CraftHumanEntity player) {
        throw new IllegalStateException("Not implemented");
    }

    default Component getTitle() {
        throw new IllegalStateException("Not implemented");
    }

    default void setTitle(Component title) {
        throw new IllegalStateException("Not implemented");
    }

    default void broadcastCarriedItem() {
        throw new IllegalStateException("Not implemented");
    }
}
