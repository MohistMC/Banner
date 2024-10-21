package com.mohistmc.banner.bukkit;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
import org.bukkit.Location;
import org.bukkit.block.Lectern;
import org.bukkit.craftbukkit.entity.CraftHumanEntity;
import org.bukkit.craftbukkit.util.CraftLocation;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.InventoryHolder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BannerLecternInventory implements Container {

    public List<HumanEntity> transaction = new ArrayList<>();
    private int maxStack = 1;
    private LecternBlockEntity lecternBlockEntity;

    public BannerLecternInventory(LecternBlockEntity lecternBlockEntity) {
        this.lecternBlockEntity = lecternBlockEntity;
    }

    public void setLecternBlockEntity(LecternBlockEntity newEntity) {
        this.lecternBlockEntity = newEntity;
    }

    @Override
    public List<ItemStack> getContents() {
        return Arrays.asList(lecternBlockEntity.getBook());
    }

    @Override
    public void onOpen(CraftHumanEntity who) {
        this.transaction.add(who);
    }

    @Override
    public void onClose(CraftHumanEntity who) {
        this.transaction.remove(who);
    }

    @Override
    public List<HumanEntity> getViewers() {
        return this.transaction;
    }

    @Override
    public void setMaxStackSize(int i) {
        this.maxStack = i;
    }

    @Override
    public Location getLocation() {
        if (lecternBlockEntity.getLevel() == null) return null;
        return CraftLocation.toBukkit(lecternBlockEntity.getBlockPos(), lecternBlockEntity.getLevel().getWorld());
    }

    @Override
    public InventoryHolder getOwner() {
        return (Lectern) lecternBlockEntity.bridge$getOwner();
    }

    public LecternBlockEntity getLectern() {
        return lecternBlockEntity;
    }
    // CraftBukkit end

    @Override
    public int getContainerSize() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return lecternBlockEntity.getBook().isEmpty();
    }

    @Override
    public ItemStack getItem(int slot) {
        return slot == 0 ? lecternBlockEntity.getBook() : ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        if (slot == 0) {
            ItemStack itemstack = lecternBlockEntity.getBook().split(amount);

            if (lecternBlockEntity.getBook().isEmpty()) {
                lecternBlockEntity.onBookItemRemove();
            }

            return itemstack;
        } else {
            return ItemStack.EMPTY;
        }
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        if (slot == 0) {
            ItemStack itemstack = lecternBlockEntity.getBook();

            lecternBlockEntity.setBook(ItemStack.EMPTY);
            lecternBlockEntity.onBookItemRemove();
            return itemstack;
        } else {
            return ItemStack.EMPTY;
        }
    }

    @Override
    // CraftBukkit start
    public void setItem(int slot, ItemStack stack) {
        if (slot == 0) {
            lecternBlockEntity.setBook(stack);
            if (lecternBlockEntity.getLevel() != null) {
                LecternBlock.resetBookState(null, lecternBlockEntity.getLevel(), lecternBlockEntity.getBlockPos(), lecternBlockEntity.getBlockState(), lecternBlockEntity.hasBook());
            }
        }
    }
    // CraftBukkit end

    @Override
    public int getMaxStackSize() {
        return this.maxStack; // CraftBukkit
    }

    @Override
    public void setChanged() {
        lecternBlockEntity.setChanged();
    }

    @Override
    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(lecternBlockEntity, player) && lecternBlockEntity.hasBook();
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return false;
    }

    @Override
    public void clearContent() {}
}
