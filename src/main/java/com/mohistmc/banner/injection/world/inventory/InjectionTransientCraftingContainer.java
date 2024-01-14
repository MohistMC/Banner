package com.mohistmc.banner.injection.world.inventory;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import org.bukkit.event.inventory.InventoryType;

public interface InjectionTransientCraftingContainer {

    default InventoryType getInvType() {
        return null;
    }

    default void bridge$setResultInventory(Container resultInventory) {
    }

    default void setOwner(Player owner) {
    }
}
