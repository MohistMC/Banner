package com.mohistmc.banner.injection.world.inventory;

import net.minecraft.world.Container;
import org.bukkit.event.inventory.InventoryType;

public interface InjectionTransientCraftingContainer {

    default InventoryType getInvType() {
        return null;
    }

    default void bridge$setResultInventory(Container resultInventory) {

    }
}
