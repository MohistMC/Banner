package com.mohistmc.banner.mixin.world;

import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.StackedContentsCompatible;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_19_R3.entity.CraftHumanEntity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.InventoryHolder;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.ArrayList;
import java.util.List;

@Mixin(SimpleContainer.class)
public abstract class MixinSimpleContainer implements Container, StackedContentsCompatible {

    // @formatter:off
    @Shadow @Final public NonNullList<ItemStack> items;
    // @formatter:on
    private static final int MAX_STACK = 64;
    private int maxStack = MAX_STACK;
    protected InventoryHolder bukkitOwner;
    public List<HumanEntity> transaction = new ArrayList<>();

    public void banner$constructor(int numSlots) {
        throw new RuntimeException();
    }

    public void banner$constructor(int numSlots, InventoryHolder owner) {
        this.banner$constructor(numSlots);
        this.bukkitOwner = owner;
    }

    @Override
    public List<ItemStack> getContents() {
        return this.items;
    }

    @Override
    public void onOpen(CraftHumanEntity who) {
        transaction.add(who);
    }

    @Override
    public void onClose(CraftHumanEntity who) {
        transaction.remove(who);
    }

    @Override
    public List<HumanEntity> getViewers() {
        return transaction;
    }

    @Override
    public InventoryHolder getOwner() {
        return bukkitOwner;
    }

    @Override
    public void setOwner(InventoryHolder owner) {
        this.bukkitOwner = owner;
    }

    @Override
    public Location getLocation() {
        return null;
    }

    @Override
    public Recipe<?> getCurrentRecipe() {
        return null;
    }

    @Override
    public void setCurrentRecipe(Recipe<?> recipe) {
    }

    @Override
    public void setMaxStackSize(int size) {
        this.maxStack = size;
    }

    @Override
    public int getMaxStackSize() {
        if (maxStack == 0) maxStack = MAX_STACK;
        return maxStack;
    }
}
