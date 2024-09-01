package com.mohistmc.banner.mixin.world.level.block.entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
import org.bukkit.Location;
import org.bukkit.craftbukkit.entity.CraftHumanEntity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.InventoryHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(targets = "net/minecraft/world/level/block/entity/LecternBlockEntity$1")
public abstract class MixinLecternBlockEntity1 implements Container {

    @Shadow(aliases = {"field_17391"}, remap = false) private LecternBlockEntity outerThis;

    public List<HumanEntity> transaction = new ArrayList<>();
    private int maxStack = 1;

    @Override
    public void setItem(int index, ItemStack stack) {
        if (index == 0) {
            outerThis.setBook(stack);
            if (outerThis.getLevel() != null) {
                LecternBlock.resetBookState(null, outerThis.getLevel(), outerThis.getBlockPos(), outerThis.getBlockState(), outerThis.hasBook());
            }
        }
    }

    @Override
    public List<ItemStack> getContents() {
        return Collections.singletonList(outerThis.getBook());
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
        return outerThis.bridge$getOwner();
    }

    @Override
    public void setOwner(InventoryHolder owner) {
    }

    @Override
    public int getMaxStackSize() {
        if (maxStack == 0) maxStack = 1;
        return maxStack;
    }

    @Override
    public void setMaxStackSize(int size) {
        this.maxStack = size;
    }

    @Override
    public Location getLocation() {
        if (outerThis.getLevel() == null) return null;
        return new Location(outerThis.getLevel().getWorld(), outerThis.getBlockPos().getX(), outerThis.getBlockPos().getY(), outerThis.getBlockPos().getZ());
    }

    @Override
    public RecipeHolder<?> getCurrentRecipe() {
        return null;
    }

    @Override
    public void setCurrentRecipe(RecipeHolder<?> recipe) {
    }

    public LecternBlockEntity getLectern() {
        return outerThis;
    }
}
