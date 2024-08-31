package com.mohistmc.banner.injection.world.inventory;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import org.bukkit.event.inventory.InventoryType;

public interface InjectionTransientCraftingContainer {

    default InventoryType getInvType() {
        throw new IllegalStateException("Not implemented");
    }

    default void bridge$setResultInventory(Container resultInventory) {
        throw new IllegalStateException("Not implemented");
    }


    default void setOwner(Player owner) {
        throw new IllegalStateException("Not implemented");
    }
}
