package com.mohistmc.banner.mixin.world.inventory;

import com.mohistmc.banner.bukkit.BukkitSnapshotCaptures;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.AbstractCraftingMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.TransientCraftingContainer;
import org.bukkit.craftbukkit.inventory.CraftInventoryCrafting;
import org.bukkit.craftbukkit.inventory.CraftInventoryView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InventoryMenu.class)
public abstract class MixinInventoryMenu extends AbstractCraftingMenu {

    private CraftInventoryView bukkitEntity;
    private Inventory playerInventory;

    public MixinInventoryMenu(MenuType<?> menuType, int i, int j, int k) {
        super(menuType, i, j, k);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    public void banner$init(Inventory playerInventory, boolean localWorld, Player playerIn, CallbackInfo ci) {
        this.playerInventory = playerInventory;
        ((TransientCraftingContainer)this.craftSlots).bridge$setResultInventory(this.resultSlots);
        ((TransientCraftingContainer)this.craftSlots).setOwner(playerInventory.player);
        this.setTitle(Component.translatable("container.crafting"));
    }

    @Inject(method = "slotsChanged", at = @At("HEAD"))
    public void banner$captureContainer(Container inventoryIn, CallbackInfo ci) {
        BukkitSnapshotCaptures.captureWorkbenchContainer((AbstractContainerMenu) (Object) this);
    }

    @Override
    public CraftInventoryView getBukkitView() {
        if (bukkitEntity != null) {
            return bukkitEntity;
        }

        CraftInventoryCrafting inventory = new CraftInventoryCrafting(this.craftSlots, this.resultSlots);
        bukkitEntity = new CraftInventoryView(this.playerInventory.player.getBukkitEntity(), inventory, (AbstractContainerMenu) (Object) this);
        return bukkitEntity;
    }
}
