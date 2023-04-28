package com.mohistmc.banner.mixin.world.level;

import net.minecraft.world.CompoundContainer;
import net.minecraft.world.Container;
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

@Mixin(CompoundContainer.class)
public abstract class MixinCompoundContainer implements Container {

    @Shadow @Final public Container container1;
    @Shadow @Final public Container container2;
    private List<HumanEntity> transactions = new ArrayList<>();

    @Override
    public List<ItemStack> getContents() {
        int size = this.getContainerSize();
        List<ItemStack> ret = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            ret.add(this.getItem(i));
        }
        return ret;
    }

    @Override
    public void onOpen(CraftHumanEntity who) {
        this.container1.onOpen(who);
        this.container2.onOpen(who);
        this.transactions.add(who);
    }

    @Override
    public void onClose(CraftHumanEntity who) {
        this.container1.onClose(who);
        this.container2.onClose(who);
        this.transactions.remove(who);
    }

    @Override
    public List<HumanEntity> getViewers() {
        return transactions;
    }

    @Override
    public InventoryHolder getOwner() { return null; }

    @Override
    public void setOwner(InventoryHolder owner) { }

    @Override
    public int getMaxStackSize() {
        return Math.min(this.container1.getMaxStackSize(), this.container2.getMaxStackSize());
    }

    @Override
    public void setMaxStackSize(int size) {
        this.container1.setMaxStackSize(size);
        this.container2.setMaxStackSize(size);
    }

    @Override
    public Location getLocation() {
        return this.container1.getLocation();
    }

    @Override
    public Recipe<?> getCurrentRecipe() { return null; }

    @Override
    public void setCurrentRecipe(Recipe recipe) { }
}
