package com.mohistmc.banner.mixin.world.inventory;

import com.mohistmc.banner.bukkit.BukkitContainer;
import com.mohistmc.banner.injection.world.inventory.InjectionAbstractContainerMenu;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import org.bukkit.craftbukkit.v1_19_R3.entity.CraftHumanEntity;
import org.bukkit.craftbukkit.v1_19_R3.inventory.CraftInventory;
import org.bukkit.craftbukkit.v1_19_R3.inventory.CraftItemStack;
import org.bukkit.event.Event;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.InventoryView;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Mixin(AbstractContainerMenu.class)
public abstract class MixinAbstractContainerMenu implements InjectionAbstractContainerMenu {

    @Shadow private ItemStack remoteCarried;

    @Shadow public abstract ItemStack getCarried();

    @Shadow @Nullable private ContainerSynchronizer synchronizer;
    @Shadow private int quickcraftStatus;

    @Shadow public static int getQuickcraftHeader(int clickedButton) {
        return clickedButton;
    }

    @Shadow protected abstract void resetQuickCraft();

    @Shadow
    public static int getQuickcraftType(int eventButton) {
        return 0;
    }

    @Shadow private int quickcraftType;

    @Shadow
    public static boolean isValidQuickcraftType(int dragMode, Player player) {
        return false;
    }

    @Shadow @Final private Set<Slot> quickcraftSlots;
    @Shadow public NonNullList<Slot> slots;
    private InventoryView bukkitView;

    @Shadow
    public static boolean canItemQuickReplace(@Nullable Slot slot, ItemStack stack, boolean stackSizeMatters) {
        return false;
    }

    @Shadow public abstract void setCarried(ItemStack stack);

    @Shadow public abstract void sendAllDataToRemote();

    @Shadow public abstract ItemStack quickMoveStack(Player player, int index);

    @Shadow @Final public int containerId;

    @Shadow public abstract int incrementStateId();

    @Shadow public abstract Slot getSlot(int slotId);

    @Shadow public abstract boolean canTakeItemForPickAll(ItemStack stack, Slot slot);

    @Shadow public abstract boolean canDragTo(Slot slot);

    @Shadow protected abstract SlotAccess createCarriedSlotAccess();

    @Shadow protected abstract boolean tryItemClickBehaviourOverride(Player player, ClickAction action, Slot slot, ItemStack clickedItem, ItemStack carriedItem);

    @Shadow private ItemStack carried;

    public boolean checkReachable = true;
    private Component title;

    @Override
    public void transferTo(AbstractContainerMenu other, CraftHumanEntity player) {
        InventoryView source = this.getBukkitView(), destination = other.getBukkitView();
        ((CraftInventory) source.getTopInventory()).getInventory().onClose(player);
        ((CraftInventory) source.getBottomInventory()).getInventory().onClose(player);
        ((CraftInventory) destination.getTopInventory()).getInventory().onOpen(player);
        ((CraftInventory) destination.getBottomInventory()).getInventory().onOpen(player);
    }

    @Override
    public Component getTitle() {
        // Banner: null title -> empty title
        if (this.title == null) {
            this.title = Component.literal("");
        }
        return this.title;
    }

    @Override
    public void setTitle(Component title) {
        this.title = title;
    }

    @Override
    public void broadcastCarriedItem() {
        this.remoteCarried = this.getCarried().copy();
        if (this.synchronizer != null) {
            this.synchronizer.sendCarriedChange(((AbstractContainerMenu) (Object) this), this.remoteCarried);
        }
    }

    /**
     * @author wdog5
     * @reason bukkit
     */
    @Overwrite
    private void doClick(int i, int j, ClickType inventoryclicktype, Player entityhuman) {
        Inventory playerinventory = entityhuman.getInventory();
        Slot slot;
        ItemStack itemstack;
        int k;
        ItemStack itemstack1;
        int l;

        if (inventoryclicktype == ClickType.QUICK_CRAFT) {
            int i1 = this.quickcraftStatus;

            this.quickcraftStatus = getQuickcraftHeader(j);
            if ((i1 != 1 || this.quickcraftStatus != 2) && i1 != this.quickcraftStatus) {
                this.resetQuickCraft();
            } else if (this.getCarried().isEmpty()) {
                this.resetQuickCraft();
            } else if (this.quickcraftStatus == 0) {
                this.quickcraftType = getQuickcraftType(j);
                if (isValidQuickcraftType(this.quickcraftType, entityhuman)) {
                    this.quickcraftStatus = 1;
                    this.quickcraftSlots.clear();
                } else {
                    this.resetQuickCraft();
                }
            } else if (this.quickcraftStatus == 1) {
                slot = (Slot) this.slots.get(i);
                itemstack = this.getCarried();
                if (canItemQuickReplace(slot, itemstack, true) && slot.mayPlace(itemstack) && (this.quickcraftType == 2 || itemstack.getCount() > this.quickcraftSlots.size()) && this.canDragTo(slot)) {
                    this.quickcraftSlots.add(slot);
                }
            } else if (this.quickcraftStatus == 2) {
                if (!this.quickcraftSlots.isEmpty()) {
                    if (false && this.quickcraftSlots.size() == 1) { // CraftBukkit - treat everything as a drag since we are unable to easily call InventoryClickEvent instead
                        k = ((Slot) this.quickcraftSlots.iterator().next()).index;
                        this.resetQuickCraft();
                        this.doClick(k, this.quickcraftType, ClickType.PICKUP, entityhuman);
                        return;
                    }

                    itemstack1 = this.getCarried().copy();
                    l = this.getCarried().getCount();
                    Iterator iterator = this.quickcraftSlots.iterator();

                    Map<Integer, ItemStack> draggedSlots = new HashMap<Integer, ItemStack>(); // CraftBukkit - Store slots from drag in map (raw slot id -> new stack)
                    while (iterator.hasNext()) {
                        Slot slot1 = (Slot) iterator.next();
                        ItemStack itemstack2 = this.getCarried();

                        if (slot1 != null && canItemQuickReplace(slot1, itemstack2, true) && slot1.mayPlace(itemstack2) && (this.quickcraftType == 2 || itemstack2.getCount() >= this.quickcraftSlots.size()) && this.canDragTo(slot1)) {
                            ItemStack itemstack3 = itemstack1.copy();
                            int j1 = slot1.hasItem() ? slot1.getItem().getCount() : 0;

                            //getQuickCraftPlaceCount(this.quickcraftSlots, this.quickcraftType, itemstack3, j1); // Banner TODO
                            int k1 = Math.min(itemstack3.getMaxStackSize(), slot1.getMaxStackSize(itemstack3));

                            if (itemstack3.getCount() > k1) {
                                itemstack3.setCount(k1);
                            }

                            l -= itemstack3.getCount() - j1;
                            // slot1.setByPlayer(itemstack3);
                            draggedSlots.put(slot1.index, itemstack3); // CraftBukkit - Put in map instead of setting
                        }
                    }

                    // CraftBukkit start - InventoryDragEvent
                    InventoryView view = getBukkitView();
                    org.bukkit.inventory.ItemStack newcursor = CraftItemStack.asCraftMirror(itemstack1);
                    newcursor.setAmount(l);
                    Map<Integer, org.bukkit.inventory.ItemStack> eventmap = new HashMap<Integer, org.bukkit.inventory.ItemStack>();
                    for (Map.Entry<Integer, ItemStack> ditem : draggedSlots.entrySet()) {
                        eventmap.put(ditem.getKey(), CraftItemStack.asBukkitCopy(ditem.getValue()));
                    }

                    // It's essential that we set the cursor to the new value here to prevent item duplication if a plugin closes the inventory.
                    ItemStack oldCursor = this.getCarried();
                    this.setCarried(CraftItemStack.asNMSCopy(newcursor));

                    InventoryDragEvent event = new InventoryDragEvent(view, (newcursor.getType() != org.bukkit.Material.AIR ? newcursor : null), CraftItemStack.asBukkitCopy(oldCursor), this.quickcraftType == 1, eventmap);
                    entityhuman.level().getCraftServer().getPluginManager().callEvent(event);

                    // Whether or not a change was made to the inventory that requires an update.
                    boolean needsUpdate = event.getResult() != Event.Result.DEFAULT;

                    if (event.getResult() != Event.Result.DENY) {
                        for (Map.Entry<Integer, ItemStack> dslot : draggedSlots.entrySet()) {
                            view.setItem(dslot.getKey(), CraftItemStack.asBukkitCopy(dslot.getValue()));
                        }
                        // The only time the carried item will be set to null is if the inventory is closed by the server.
                        // If the inventory is closed by the server, then the cursor items are dropped.  This is why we change the cursor early.
                        if (this.getCarried() != null) {
                            this.setCarried(CraftItemStack.asNMSCopy(event.getCursor()));
                            needsUpdate = true;
                        }
                    } else {
                        this.setCarried(oldCursor);
                    }

                    if (needsUpdate && entityhuman instanceof ServerPlayer) {
                        this.sendAllDataToRemote();
                    }
                    // CraftBukkit end
                }

                this.resetQuickCraft();
            } else {
                this.resetQuickCraft();
            }
        } else if (this.quickcraftStatus != 0) {
            this.resetQuickCraft();
        } else {
            int l1;

            if ((inventoryclicktype == ClickType.PICKUP || inventoryclicktype == ClickType.QUICK_MOVE) && (j == 0 || j == 1)) {
                ClickAction clickaction = j == 0 ? ClickAction.PRIMARY : ClickAction.SECONDARY;

                if (i == -999) {
                    if (!this.getCarried().isEmpty()) {
                        if (clickaction == ClickAction.PRIMARY) {
                            // CraftBukkit start
                            ItemStack carried = this.getCarried();
                            this.setCarried(ItemStack.EMPTY);
                            entityhuman.drop(carried, true);
                            // CraftBukkit start
                        } else {
                            entityhuman.drop(this.getCarried().split(1), true);
                        }
                    }
                } else if (inventoryclicktype == ClickType.QUICK_MOVE) {
                    if (i < 0) {
                        return;
                    }

                    slot = (Slot) this.slots.get(i);
                    if (!slot.mayPickup(entityhuman)) {
                        return;
                    }

                    for (itemstack = this.quickMoveStack(entityhuman, i); !itemstack.isEmpty() && ItemStack.isSameItem(slot.getItem(), itemstack); itemstack = this.quickMoveStack(entityhuman, i)) {
                        ;
                    }
                } else {
                    if (i < 0) {
                        return;
                    }

                    slot = (Slot) this.slots.get(i);
                    itemstack = slot.getItem();
                    ItemStack itemstack4 = this.getCarried();

                    entityhuman.updateTutorialInventoryAction(itemstack4, slot.getItem(), clickaction);
                    if (!this.tryItemClickBehaviourOverride(entityhuman, clickaction, slot, itemstack, itemstack4)) {
                        if (itemstack.isEmpty()) {
                            if (!itemstack4.isEmpty()) {
                                l1 = clickaction == ClickAction.PRIMARY ? itemstack4.getCount() : 1;
                                this.setCarried(slot.safeInsert(itemstack4, l1));
                            }
                        } else if (slot.mayPickup(entityhuman)) {
                            if (itemstack4.isEmpty()) {
                                l1 = clickaction == ClickAction.PRIMARY ? itemstack.getCount() : (itemstack.getCount() + 1) / 2;
                                Optional<ItemStack> optional = slot.tryRemove(l1, Integer.MAX_VALUE, entityhuman);

                                optional.ifPresent((itemstack5) -> {
                                    this.setCarried(itemstack5);
                                    slot.onTake(entityhuman, itemstack5);
                                });
                            } else if (slot.mayPlace(itemstack4)) {
                                if (ItemStack.isSameItemSameTags(itemstack, itemstack4)) {
                                    l1 = clickaction == ClickAction.PRIMARY ? itemstack4.getCount() : 1;
                                    this.setCarried(slot.safeInsert(itemstack4, l1));
                                } else if (itemstack4.getCount() <= slot.getMaxStackSize(itemstack4)) {
                                    this.setCarried(itemstack);
                                    slot.setByPlayer(itemstack4);
                                }
                            } else if (ItemStack.isSameItemSameTags(itemstack, itemstack4)) {
                                Optional<ItemStack> optional1 = slot.tryRemove(itemstack.getCount(), itemstack4.getMaxStackSize() - itemstack4.getCount(), entityhuman);

                                optional1.ifPresent((itemstack5) -> {
                                    itemstack4.grow(itemstack5.getCount());
                                    slot.onTake(entityhuman, itemstack5);
                                });
                            }
                        }
                    }

                    slot.setChanged();
                    // CraftBukkit start - Make sure the client has the right slot contents
                    if (entityhuman instanceof ServerPlayer && slot.getMaxStackSize() != 64) {
                        ((ServerPlayer) entityhuman).connection.send(new ClientboundContainerSetSlotPacket(this.containerId, this.incrementStateId(), slot.index, slot.getItem()));
                        // Updating a crafting inventory makes the client reset the result slot, have to send it again
                        if (this.getBukkitView().getType() == InventoryType.WORKBENCH || this.getBukkitView().getType() == InventoryType.CRAFTING) {
                            ((ServerPlayer) entityhuman).connection.send(new ClientboundContainerSetSlotPacket(this.containerId, this.incrementStateId(), 0, this.getSlot(0).getItem()));
                        }
                    }
                    // CraftBukkit end
                }
            } else {
                Slot slot2;
                int i2;

                if (inventoryclicktype == ClickType.SWAP) {
                    slot2 = (Slot) this.slots.get(i);
                    itemstack1 = playerinventory.getItem(j);
                    itemstack = slot2.getItem();
                    if (!itemstack1.isEmpty() || !itemstack.isEmpty()) {
                        if (itemstack1.isEmpty()) {
                            if (slot2.mayPickup(entityhuman)) {
                                playerinventory.setItem(j, itemstack);
                                slot2.onSwapCraft(itemstack.getCount());
                                slot2.setByPlayer(ItemStack.EMPTY);
                                slot2.onTake(entityhuman, itemstack);
                            }
                        } else if (itemstack.isEmpty()) {
                            if (slot2.mayPlace(itemstack1)) {
                                i2 = slot2.getMaxStackSize(itemstack1);
                                if (itemstack1.getCount() > i2) {
                                    slot2.setByPlayer(itemstack1.split(i2));
                                } else {
                                    playerinventory.setItem(j, ItemStack.EMPTY);
                                    slot2.setByPlayer(itemstack1);
                                }
                            }
                        } else if (slot2.mayPickup(entityhuman) && slot2.mayPlace(itemstack1)) {
                            i2 = slot2.getMaxStackSize(itemstack1);
                            if (itemstack1.getCount() > i2) {
                                slot2.setByPlayer(itemstack1.split(i2));
                                slot2.onTake(entityhuman, itemstack);
                                if (!playerinventory.add(itemstack)) {
                                    entityhuman.drop(itemstack, true);
                                }
                            } else {
                                playerinventory.setItem(j, itemstack);
                                slot2.setByPlayer(itemstack1);
                                slot2.onTake(entityhuman, itemstack);
                            }
                        }
                    }
                } else if (inventoryclicktype == ClickType.CLONE && entityhuman.getAbilities().instabuild && this.getCarried().isEmpty() && i >= 0) {
                    slot2 = (Slot) this.slots.get(i);
                    if (slot2.hasItem()) {
                        itemstack1 = slot2.getItem().copy();
                        itemstack1.setCount(itemstack1.getMaxStackSize());
                        this.setCarried(itemstack1);
                    }
                } else if (inventoryclicktype == ClickType.THROW && this.getCarried().isEmpty() && i >= 0) {
                    slot2 = (Slot) this.slots.get(i);
                    k = j == 0 ? 1 : slot2.getItem().getCount();
                    itemstack = slot2.safeTake(k, Integer.MAX_VALUE, entityhuman);
                    entityhuman.drop(itemstack, true);
                } else if (inventoryclicktype == ClickType.PICKUP_ALL && i >= 0) {
                    slot2 = (Slot) this.slots.get(i);
                    itemstack1 = this.getCarried();
                    if (!itemstack1.isEmpty() && (!slot2.hasItem() || !slot2.mayPickup(entityhuman))) {
                        l = j == 0 ? 0 : this.slots.size() - 1;
                        i2 = j == 0 ? 1 : -1;

                        for (l1 = 0; l1 < 2; ++l1) {
                            for (int j2 = l; j2 >= 0 && j2 < this.slots.size() && itemstack1.getCount() < itemstack1.getMaxStackSize(); j2 += i2) {
                                Slot slot3 = (Slot) this.slots.get(j2);

                                if (slot3.hasItem() && canItemQuickReplace(slot3, itemstack1, true) && slot3.mayPickup(entityhuman) && this.canTakeItemForPickAll(itemstack1, slot3)) {
                                    ItemStack itemstack5 = slot3.getItem();

                                    if (l1 != 0 || itemstack5.getCount() != itemstack5.getMaxStackSize()) {
                                        ItemStack itemstack6 = slot3.safeTake(itemstack5.getCount(), itemstack1.getMaxStackSize() - itemstack1.getCount(), entityhuman);

                                        itemstack1.grow(itemstack6.getCount());
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

    }


    /**
     * @author wdog5
     * @reason bukkit
     */
    @Overwrite
    public void removed(Player entityhuman) {
        if (entityhuman instanceof ServerPlayer) {
            ItemStack itemstack = this.getCarried();

            if (!itemstack.isEmpty()) {
                this.setCarried(ItemStack.EMPTY); // CraftBukkit - SPIGOT-4556 - from below
                if (entityhuman.isAlive() && !((ServerPlayer) entityhuman).hasDisconnected()) {
                    entityhuman.getInventory().placeItemBackInInventory(itemstack);
                } else {
                    entityhuman.drop(itemstack, false);
                }

                // this.setCarried(ItemStack.EMPTY); // CraftBukkit - moved up
            }
        }

    }

    @Override
    public boolean bridge$checkReachable() {
        return checkReachable;
    }

    @Override
    public void banner$setCheckReachable(boolean checkReachable) {
        this.checkReachable = checkReachable;
    }

    @Override
    public InventoryView getBukkitView() {
        if (bukkitView == null) {
            bukkitView = BukkitContainer.createInvView((AbstractContainerMenu) (Object) this);
        }
        return bukkitView;
    }

    @Inject(method = "getCarried", at = @At("HEAD"))
    private void banner$checkCarried(CallbackInfoReturnable<ItemStack> cir) {
        // CraftBukkit start
        if (this.carried.isEmpty()) {
            this.setCarried(ItemStack.EMPTY);
        }
        // CraftBukkit end
    }
}
