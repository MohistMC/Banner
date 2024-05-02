package com.mohistmc.banner.bukkit;

import net.minecraft.world.Container;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.block.CraftBlockState;
import org.bukkit.craftbukkit.inventory.CraftInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class CraftCustomContainer extends CraftBlockState implements InventoryHolder {

    private final CraftWorld world;
    private final Container container;

    public CraftCustomContainer(Block block) {
        super(block);
        world = (CraftWorld) block.getWorld();
        container = (Container) world.getBlockAt(getX(), getY(), getZ());
    }

    @Override
    public Inventory getInventory() {
        CraftInventory inventory = new CraftInventory(container);
        return inventory;
    }
}