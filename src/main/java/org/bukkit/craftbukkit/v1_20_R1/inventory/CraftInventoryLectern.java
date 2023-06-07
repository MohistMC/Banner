package org.bukkit.craftbukkit.v1_20_R1.inventory;

import net.minecraft.world.Container;
import org.bukkit.block.Lectern;
import org.bukkit.inventory.LecternInventory;

public class CraftInventoryLectern extends CraftInventory implements LecternInventory {

    public net.minecraft.world.MenuProvider tile;

    public CraftInventoryLectern(Container inventory) {
        super(inventory);

        /**
        if (inventory instanceof BannerLecternInventory) {
            this.tile = ((BannerLecternInventory) inventory).getLectern();
        }*/
    }

    @Override
    public Lectern getHolder() {
        return (Lectern) inventory.getOwner();
    }
}
