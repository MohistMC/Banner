package com.mohistmc.banner.bukkit.inventory;

import net.minecraft.world.Container;
import org.bukkit.craftbukkit.inventory.CraftInventory;
import org.bukkit.craftbukkit.inventory.CraftInventoryPlayer;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.List;

public class CraftCustomInventory implements InventoryHolder {

    private final CraftInventory container;

    public CraftCustomInventory(Container inventory) {
        this.container = new CraftInventory(inventory);
    }

    public CraftCustomInventory(net.minecraft.world.entity.player.Inventory playerInventory) {
        this.container = new CraftInventoryPlayer(playerInventory);
    }

    @Override
    public Inventory getInventory() {
        return this.container;
    }

    public static List<HumanEntity> getViewers(Container inventory) {
        try {
            return inventory.getViewers();
        } catch (AbstractMethodError e) {
            return new java.util.ArrayList<>();
        }
    }
}
