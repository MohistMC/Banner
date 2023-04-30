package com.mohistmc.banner.mixin.world.inventory;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mohistmc.banner.injection.world.inventory.InjectionAnvilMenu;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import org.bukkit.craftbukkit.v1_19_R3.event.CraftEventFactory;
import org.bukkit.craftbukkit.v1_19_R3.inventory.CraftInventory;
import org.bukkit.craftbukkit.v1_19_R3.inventory.CraftInventoryAnvil;
import org.bukkit.craftbukkit.v1_19_R3.inventory.CraftInventoryView;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AnvilMenu.class)
public abstract class MixinAnvilMenu extends ItemCombinerMenu implements InjectionAnvilMenu {

    @Shadow @Final public DataSlot cost;
    // CraftBukkit start
    private static final int DEFAULT_DENIED_COST = -1;
    public int maximumRepairCost = 40;
    private CraftInventoryView bukkitEntity;
    // CraftBukkit end

    public MixinAnvilMenu(@Nullable MenuType<?> menuType, int i, Inventory inventory, ContainerLevelAccess containerLevelAccess) {
        super(menuType, i, inventory, containerLevelAccess);
    }

    @ModifyReturnValue(method = "mayPickup", at = @At("RETURN"))
    private boolean banner$changePickUpValue(Player player, boolean hasStack, CallbackInfoReturnable<Boolean> cir) {
        return (player.getAbilities().instabuild || player.experienceLevel >= this.cost.get()) && this.cost.get() > DEFAULT_DENIED_COST && hasStack; // CraftBukkit - allow cost 0 like a free item
    }

    @Redirect(method = "createResult", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/DataSlot;set(I)V", ordinal = 1))
    private void banner$resetAnvilCost1(DataSlot instance, int i) {
        this.cost.set(DEFAULT_DENIED_COST);
    }

    @Redirect(method = "createResult", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/DataSlot;set(I)V", ordinal = 2))
    private void banner$resetAnvilCost2(DataSlot instance, int i) {
        this.cost.set(DEFAULT_DENIED_COST);
    }

    @Redirect(method = "createResult", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/DataSlot;set(I)V", ordinal = 3))
    private void banner$resetAnvilCost3(DataSlot instance, int i) {
        this.cost.set(DEFAULT_DENIED_COST);
    }

    @Redirect(method = "createResult", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/DataSlot;set(I)V", ordinal = 4))
    private void banner$resetAnvilCost4(DataSlot instance, int i) {
        this.cost.set(DEFAULT_DENIED_COST);
    }

    @Redirect(method = "onTake", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/DataSlot;set(I)V"))
    private void banner$reset(DataSlot instance, int i) {
        this.cost.set(DEFAULT_DENIED_COST);
    }

    @Redirect(method = "createResult", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/ResultContainer;setItem(ILnet/minecraft/world/item/ItemStack;)V", ordinal = 0))
    private void banner$addAnvilCause0(ResultContainer instance, int slot, ItemStack stack) {
        CraftEventFactory.callPrepareAnvilEvent(getBukkitView(), ItemStack.EMPTY); // CraftBukkit
    }

    @Redirect(method = "createResult", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/ResultContainer;setItem(ILnet/minecraft/world/item/ItemStack;)V", ordinal = 1))
    private void banner$addAnvilCause1(ResultContainer instance, int slot, ItemStack stack) {
        CraftEventFactory.callPrepareAnvilEvent(getBukkitView(), ItemStack.EMPTY); // CraftBukkit
    }

    @Redirect(method = "createResult", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/ResultContainer;setItem(ILnet/minecraft/world/item/ItemStack;)V", ordinal = 2))
    private void banner$addAnvilCause2(ResultContainer instance, int slot, ItemStack stack) {
        CraftEventFactory.callPrepareAnvilEvent(getBukkitView(), ItemStack.EMPTY); // CraftBukkit
    }

    @Redirect(method = "createResult", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/ResultContainer;setItem(ILnet/minecraft/world/item/ItemStack;)V", ordinal = 3))
    private void banner$addAnvilCause3(ResultContainer instance, int slot, ItemStack stack) {
        CraftEventFactory.callPrepareAnvilEvent(getBukkitView(), ItemStack.EMPTY); // CraftBukkit
    }

    @Redirect(method = "createResult", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/ResultContainer;setItem(ILnet/minecraft/world/item/ItemStack;)V", ordinal = 4))
    private void banner$addAnvilCause4(ResultContainer instance, int slot, ItemStack stack) {
        CraftEventFactory.callPrepareAnvilEvent(getBukkitView(), stack); // CraftBukkit
    }

    @Inject(method = "createResult", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/AnvilMenu;broadcastChanges()V", shift = At.Shift.BEFORE))
    private void banner$addMessage(CallbackInfo ci) {
        sendAllDataToRemote(); // CraftBukkit - SPIGOT-6686: Always send completed inventory to stay in sync with client
    }

    @ModifyExpressionValue(method = "createResult", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/DataSlot;get()I"))
    private boolean banner$resetValue() {
        return this.cost.get() >= maximumRepairCost;
    }

    @Override
    public int bridge$getDeniedCost() {
        return DEFAULT_DENIED_COST;
    }

    @Override
    public int bridge$maximumRepairCost() {
        return maximumRepairCost;
    }

    @Override
    public void banner$setMaximumRepairCost(int maximumRepairCost) {
        this.maximumRepairCost = maximumRepairCost;
    }

    // CraftBukkit start
    @Override
    public CraftInventoryView getBukkitView() {
        if (bukkitEntity != null) {
            return bukkitEntity;
        }
        CraftInventory inventory = new CraftInventoryAnvil(
                access.getLocation(), this.inputSlots, this.resultSlots, ((AnvilMenu) (Object) this));
        bukkitEntity = new CraftInventoryView(this.player.getBukkitEntity(), inventory, ((AnvilMenu) (Object) this));
        return bukkitEntity;
    }
    // CraftBukkit end
}
