package com.mohistmc.banner.mixin.world.inventory;

import com.mohistmc.banner.asm.annotation.CreateConstructor;
import com.mohistmc.banner.asm.annotation.ShadowConstructor;
import com.mohistmc.banner.injection.world.inventory.InjectionTransientCraftingContainer;
import java.util.List;
import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.bukkit.Location;
import org.bukkit.craftbukkit.entity.CraftHumanEntity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.InventoryHolder;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(TransientCraftingContainer.class)
public abstract class MixinTransientTransientCraftingContainer implements Container, InjectionTransientCraftingContainer {

    @Shadow @Final private NonNullList<ItemStack> items;

    @Shadow @Final public AbstractContainerMenu menu;
    // CraftBukkit start - add fields
    public List<HumanEntity> transaction = new java.util.ArrayList<>();
    private RecipeHolder<?> currentRecipe;
    public Container resultInventory;
    private Player owner;
    private int maxStack = MAX_STACK;

    @ShadowConstructor
    public void banner$constructor(AbstractContainerMenu eventHandlerIn, int width, int height) {
        throw new RuntimeException();
    }

    @CreateConstructor
    public void banner$constructor(AbstractContainerMenu eventHandlerIn, int width, int height, Player owner) {
        banner$constructor(eventHandlerIn, width, height);
        this.owner = owner;
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
    public InventoryType getInvType() {
        return items.size() == 4 ? InventoryType.CRAFTING : InventoryType.WORKBENCH;
    }

    @Override
    public void onClose(CraftHumanEntity who) {
        transaction.remove(who);
    }

    @Override
    public InventoryHolder getOwner() {
        return (owner == null) ? null : owner.getBukkitEntity();
    }

    // Banner start
    @Override
    public void setOwner(Player owner) {
        this.owner = owner;
    }
    // Banner end

    @Override
    public int getMaxStackSize() {
        return maxStack;
    }

    @Override
    public List<HumanEntity> getViewers() {
        return transaction;
    }

    @Override
    public RecipeHolder<?> getCurrentRecipe() {
        return currentRecipe;
    }

    @Override
    public Location getLocation() {
        return menu instanceof CraftingMenu ? ((CraftingMenu) menu).access.getLocation() : owner.getBukkitEntity().getLocation();
    }

    @Override
    public void setMaxStackSize(int size) {
        maxStack = size;
        resultInventory.setMaxStackSize(size);
    }

    @Override
    public void bridge$setResultInventory(Container resultInventory) {
        this.resultInventory = resultInventory;
    }

    @Override
    public void setCurrentRecipe(RecipeHolder<?> recipe) {
        this.currentRecipe = recipe;
    }
}
