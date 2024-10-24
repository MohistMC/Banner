package com.mohistmc.banner.mixin.world.inventory;

import java.util.List;
import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.item.ItemStack;
import org.bukkit.Location;
import org.bukkit.craftbukkit.entity.CraftHumanEntity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.InventoryHolder;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ResultContainer.class)
public abstract class MixinResultContainer implements Container {

    @Shadow @Final private NonNullList<ItemStack> itemStacks;
    private int maxStack = MAX_STACK;

    @Override
    public List<ItemStack> getContents() {
        return this.itemStacks;
    }

    // Don't need a transaction; the InventoryCrafting keeps track of it for us
    @Override
    public void onOpen(CraftHumanEntity who) {}
    @Override
    public void onClose(CraftHumanEntity who) {}


    @Override
    public List<HumanEntity> getViewers() {
        return new java.util.ArrayList<>();
    }

    @Override
    public InventoryHolder getOwner() {
        return null; // Result slots don't get an owner
    }

    @Override
    public int getMaxStackSize() {
        return maxStack;
    }

    @Override
    public void setMaxStackSize(int size) {
        maxStack = size;
    }

    @Override
    public Location getLocation() {
        return null;
    }
}
