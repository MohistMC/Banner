package com.mohistmc.banner.mixin.world.inventory;

import com.mohistmc.banner.bukkit.BukkitCaptures;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import org.bukkit.craftbukkit.v1_19_R3.inventory.CraftInventoryCrafting;
import org.bukkit.craftbukkit.v1_19_R3.inventory.CraftInventoryView;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InventoryMenu.class)
public abstract class MixinInventoryMenu extends RecipeBookMenu<CraftingContainer> {


    // @formatter:off
    @Shadow @Final private CraftingContainer craftSlots;
    @Shadow @Final private ResultContainer resultSlots;
    // @formatter:on

    private CraftInventoryView bukkitEntity;
    private Inventory playerInventory;

    public MixinInventoryMenu(MenuType<?> menuType, int i) {
        super(menuType, i);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    public void banner$init(Inventory playerInventory, boolean localWorld, Player playerIn, CallbackInfo ci) {
        this.playerInventory = playerInventory;
         this.craftSlots.setOwner(playerInventory.player.getBukkitEntity());
         ((TransientCraftingContainer) this.craftSlots).bridge$setResultInventory(this.resultSlots);
        this.setTitle(Component.translatable("container.crafting"));
    }

    @Inject(method = "slotsChanged", at = @At("HEAD"))
    public void banner$captureContainer(Container inventoryIn, CallbackInfo ci) {
        BukkitCaptures.captureWorkbenchContainer((AbstractContainerMenu) (Object) this);
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
