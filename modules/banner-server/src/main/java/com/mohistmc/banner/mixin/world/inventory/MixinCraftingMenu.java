package com.mohistmc.banner.mixin.world.inventory;

import com.mohistmc.banner.bukkit.BukkitSnapshotCaptures;
import java.util.Optional;

import io.izzel.arclight.mixin.Decorate;
import io.izzel.arclight.mixin.DecorationOps;
import io.izzel.arclight.mixin.Local;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.AbstractCraftingMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RepairItemRecipe;
import net.minecraft.world.level.Level;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.craftbukkit.inventory.CraftInventoryCrafting;
import org.bukkit.craftbukkit.inventory.CraftInventoryView;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CraftingMenu.class)
public abstract class MixinCraftingMenu extends AbstractCraftingMenu {


    public MixinCraftingMenu(MenuType<?> menuType, int i, int j, int k) {
        super(menuType, i, j, k);
    }

    @Accessor("access") public abstract ContainerLevelAccess bridge$getWorldPos();
    // @formatter:on

    private CraftInventoryView bukkitEntity;
    private Inventory playerInventory;

    @Inject(method = "stillValid", cancellable = true, at = @At("HEAD"))
    public void banner$unreachable(Player playerIn, CallbackInfoReturnable<Boolean> cir) {
        if (!this.bridge$checkReachable()) cir.setReturnValue(true);
    }

    @Inject(method = "slotsChanged", at = @At("HEAD"))
    public void banner$capture(Container inventoryIn, CallbackInfo ci) {
        BukkitSnapshotCaptures.captureWorkbenchContainer((CraftingMenu) (Object) this);
    }

    private static transient boolean banner$capture;

    @Redirect(method = "slotChangedCraftingGrid", at = @At(value = "INVOKE", remap = false, target = "Ljava/util/Optional;isPresent()Z"))
    private static boolean banner$testRepair(Optional<RecipeHolder<CraftingRecipe>> optional) {
        banner$capture = optional.map(it -> ((RecipeHolder) (Object) it).toBukkitRecipe()).orElse(null) instanceof RepairItemRecipe;
        return optional.isPresent();
    }


    @Decorate(method = "slotChangedCraftingGrid", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/ResultContainer;setItem(ILnet/minecraft/world/item/ItemStack;)V"))
    private static void arclight$preCraft(ResultContainer instance, int i, ItemStack itemStack, AbstractContainerMenu abstractContainerMenu, Level level, Player player, CraftingContainer craftingContainer, ResultContainer resultContainer, @Nullable RecipeHolder<CraftingRecipe> recipeHolder,
                                          @Local(ordinal = -1) ItemStack stack) throws Throwable {
        stack = CraftEventFactory.callPreCraftEvent(craftingContainer, instance, itemStack, abstractContainerMenu.getBukkitView(), banner$capture);
        DecorationOps.callsite().invoke(instance, i, stack);
    }

    @Inject(method = "<init>(ILnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/world/inventory/ContainerLevelAccess;)V", at = @At("RETURN"))
    public void banner$init(int i, Inventory playerInventory, ContainerLevelAccess callable, CallbackInfo ci) {
        ((TransientCraftingContainer)this.craftSlots).setOwner(playerInventory.player);
        ((TransientCraftingContainer)this.craftSlots).bridge$setResultInventory(this.resultSlots);
         this.playerInventory = playerInventory;
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
