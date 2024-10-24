package org.bukkit.craftbukkit.inventory;

import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.bukkit.Location;
import org.bukkit.craftbukkit.entity.CraftHumanEntity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.InventoryHolder;

public class CraftInventoryCustom extends CraftInventory {
    public CraftInventoryCustom(InventoryHolder owner, InventoryType type) {
        super(new MinecraftInventory(owner, type));
    }

    public CraftInventoryCustom(InventoryHolder owner, InventoryType type, String title) {
        super(new MinecraftInventory(owner, type, title));
    }

    public CraftInventoryCustom(InventoryHolder owner, int size) {
        super(new MinecraftInventory(owner, size));
    }

    public CraftInventoryCustom(InventoryHolder owner, int size, String title) {
        super(new MinecraftInventory(owner, size, title));
    }

    static class MinecraftInventory implements Container {
        private final NonNullList<ItemStack> items;
        private int maxStack = MAX_STACK;
        private final List<HumanEntity> viewers;
        private final String title;
        private InventoryType type;
        private final InventoryHolder owner;

        public MinecraftInventory(InventoryHolder owner, InventoryType type) {
            this(owner, type.getDefaultSize(), type.getDefaultTitle());
            this.type = type;
        }

        public MinecraftInventory(InventoryHolder owner, InventoryType type, String title) {
            this(owner, type.getDefaultSize(), title);
            this.type = type;
        }

        public MinecraftInventory(InventoryHolder owner, int size) {
            this(owner, size, "Chest");
        }

        public MinecraftInventory(InventoryHolder owner, int size, String title) {
            Preconditions.checkArgument(title != null, "title cannot be null");
            this.items = NonNullList.withSize(size, ItemStack.EMPTY);
            this.title = title;
            this.viewers = new ArrayList<HumanEntity>();
            this.owner = owner;
            this.type = InventoryType.CHEST;
        }

        @Override
        public int getContainerSize() {
            return this.items.size();
        }

        @Override
        public ItemStack getItem(int slot) {
            return this.items.get(slot);
        }

        @Override
        public ItemStack removeItem(int slot, int amount) {
            ItemStack stack = this.getItem(slot);
            ItemStack result;
            if (stack == ItemStack.EMPTY) return stack;
            if (stack.getCount() <= amount) {
                this.setItem(slot, ItemStack.EMPTY);
                result = stack;
            } else {
                result = CraftItemStack.copyNMSStack(stack, amount);
                stack.shrink(amount);
            }
            this.setChanged();
            return result;
        }

        @Override
        public ItemStack removeItemNoUpdate(int slot) {
            ItemStack stack = this.getItem(slot);
            ItemStack result;
            if (stack == ItemStack.EMPTY) return stack;
            if (stack.getCount() <= 1) {
                this.setItem(slot, null);
                result = stack;
            } else {
                result = CraftItemStack.copyNMSStack(stack, 1);
                stack.shrink(1);
            }
            return result;
        }

        @Override
        public void setItem(int slot, ItemStack stack) {
            this.items.set(slot, stack);
            if (stack != ItemStack.EMPTY && this.getMaxStackSize() > 0 && stack.getCount() > this.getMaxStackSize()) {
                stack.setCount(this.getMaxStackSize());
            }
        }

        @Override
        public int getMaxStackSize() {
            return this.maxStack;
        }

        @Override
        public void setMaxStackSize(int size) {
            this.maxStack = size;
        }

        @Override
        public void setChanged() {}

        @Override
        public boolean stillValid(Player player) {
            return true;
        }

        @Override
        public List<ItemStack> getContents() {
            return this.items;
        }

        @Override
        public void onOpen(CraftHumanEntity who) {
            this.viewers.add(who);
        }

        @Override
        public void onClose(CraftHumanEntity who) {
            this.viewers.remove(who);
        }

        @Override
        public List<HumanEntity> getViewers() {
            return this.viewers;
        }

        public InventoryType getType() {
            return this.type;
        }

        @Override
        public InventoryHolder getOwner() {
            return this.owner;
        }

        @Override
        public boolean canPlaceItem(int slot, ItemStack stack) {
            return true;
        }

        @Override
        public void startOpen(Player player) {

        }

        @Override
        public void stopOpen(Player player) {

        }

        @Override
        public void clearContent() {
            this.items.clear();
        }

        @Override
        public Location getLocation() {
            return null;
        }

        public String getTitle() {
            return this.title;
        }

        @Override
        public boolean isEmpty() {
            Iterator iterator = this.items.iterator();

            ItemStack itemstack;

            do {
                if (!iterator.hasNext()) {
                    return true;
                }

                itemstack = (ItemStack) iterator.next();
            } while (itemstack.isEmpty());

            return false;
        }
    }
}
