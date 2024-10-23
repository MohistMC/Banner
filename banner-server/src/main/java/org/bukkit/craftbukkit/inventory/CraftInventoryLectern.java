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

        if (inventory instanceof BannerLecternInventory bannerLecternInventory) {
            bannerLecternInventory.setLecternBlockEntity((LecternBlockEntity) inventory);
            this.tile = bannerLecternInventory.getLectern();
        }
    }

    @Override
    public Lectern getHolder() {
        return (Lectern) this.inventory.getOwner();
    }
}
