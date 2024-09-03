package com.mohistmc.banner.mixin.world.entity.player;

import com.mohistmc.banner.injection.world.entity.player.InjectionInventory;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.Nameable;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.bukkit.Location;
import org.bukkit.craftbukkit.entity.CraftHumanEntity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.InventoryHolder;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

//TODO fix inject methods
@Mixin(Inventory.class)
public abstract class MixinInventory implements Container, Nameable, InjectionInventory {

    @Shadow @Final public NonNullList<ItemStack> items;
    @Shadow @Final public NonNullList<ItemStack> armor;
    @Shadow @Final public NonNullList<ItemStack> offhand;
    @Shadow @Final private List<NonNullList<ItemStack>> compartments;
    @Shadow @Final public Player player;

    @Shadow protected abstract boolean hasRemainingSpaceForItem(ItemStack destination, ItemStack origin);

    public List<HumanEntity> transaction = new java.util.ArrayList<>();
    private int maxStack = MAX_STACK;

    @Override
    public List<ItemStack> getContents() {
        List<ItemStack> combined = new ArrayList<>(items.size() + armor.size() + offhand.size());
        for (List<net.minecraft.world.item.ItemStack> sub : this.compartments) {
            combined.addAll(sub);
        }

        return combined;
    }

    @Override
    public List<ItemStack> getArmorContents() {
        return this.armor;
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
        return this.player.getBukkitEntity();
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
    public int canHold(ItemStack itemstack) {
        int remains = itemstack.getCount();
        for (int i = 0; i < this.items.size(); ++i) {
            ItemStack itemstack1 = this.getItem(i);
            if (itemstack1.isEmpty()) return itemstack.getCount();

            if (this.hasRemainingSpaceForItem(itemstack1, itemstack)) {
                remains -= (itemstack1.getMaxStackSize() < this.getMaxStackSize() ? itemstack1.getMaxStackSize() : this.getMaxStackSize()) - itemstack1.getCount();
            }
            if (remains <= 0) return itemstack.getCount();
        }
        ItemStack offhandItemStack = this.getItem(this.items.size() + this.armor.size());
        if (this.hasRemainingSpaceForItem(offhandItemStack, itemstack)) {
            remains -= (offhandItemStack.getMaxStackSize() < this.getMaxStackSize() ? offhandItemStack.getMaxStackSize() : this.getMaxStackSize()) - offhandItemStack.getCount();
        }
        if (remains <= 0) return itemstack.getCount();

        return itemstack.getCount() - remains;
    }

    @Override
    public Location getLocation() {
        return player.getBukkitEntity().getLocation();
    }
}
