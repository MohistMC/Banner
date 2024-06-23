package com.mohistmc.banner.injection.world.inventory;

import net.minecraft.world.entity.player.Inventory;

public interface InjectionLecternMenu {

    default void bridge$setPlayerInventory(Inventory playerInventory) {
        throw new IllegalStateException("Not implemented");
    }
}
