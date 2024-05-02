package com.mohistmc.banner.bukkit;

import java.util.ArrayList;
import java.util.List;
import java.util.Spliterator;
import java.util.function.Consumer;
import net.minecraft.world.Container;
import org.bukkit.craftbukkit.inventory.CraftInventory;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

public class BannerLecternInventory extends CraftInventory {

    public List<HumanEntity> transaction = new ArrayList<>();
    private int maxStack = 1;

    public BannerLecternInventory(Container inventory) {
        super(inventory);
    }

    @Override
    public InventoryHolder getHolder() {
        return super.getHolder();
    }

    @Override
    public List<HumanEntity> getViewers() {
        return transaction;
    }

    @Override
    public void setMaxStackSize(int size) {
        maxStack = size;
    }

    @Override
    public void forEach(Consumer<? super ItemStack> action) {
        super.forEach(action);
    }

    @Override
    public Spliterator<ItemStack> spliterator() {
        return super.spliterator();
    }
}
