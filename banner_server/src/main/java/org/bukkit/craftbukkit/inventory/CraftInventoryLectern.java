package org.bukkit.craftbukkit.inventory;

import com.mohistmc.banner.bukkit.BannerLecternInventory;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
import org.bukkit.block.Lectern;
import org.bukkit.inventory.LecternInventory;

public class CraftInventoryLectern extends CraftInventory implements LecternInventory {

    public MenuProvider tile;

    public CraftInventoryLectern(Container inventory) {
        super(inventory);
        // Banner TODO fixme
        /*
        if (inventory instanceof BannerLecternInventory) {
            this.tile = ((BannerLecternInventory) inventory).getLectern();
        }*/
    }

    @Override
    public Lectern getHolder() {
        return (Lectern) this.inventory.getOwner();
    }
}
