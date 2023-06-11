package com.mohistmc.banner.mixin.world.inventory;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.BeaconMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.MenuType;
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftInventory;
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftInventoryBeacon;
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftInventoryView;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BeaconMenu.class)
public abstract class MixinBeaconMenu extends AbstractContainerMenu {

    protected MixinBeaconMenu(@Nullable MenuType<?> menuType, int i) {
        super(menuType, i);
    }

    // @formatter:off
    @Shadow @Final private Container beacon;
    // @formatter:on

    private CraftInventoryView bukkitEntity;
    private Inventory playerInventory;

    @Inject(method = "<init>(ILnet/minecraft/world/Container;Lnet/minecraft/world/inventory/ContainerData;Lnet/minecraft/world/inventory/ContainerLevelAccess;)V", at = @At("RETURN"))
    public void banner$init(int id, Container inventory, ContainerData containerData, ContainerLevelAccess worldPosCallable, CallbackInfo ci) {
        this.playerInventory = (Inventory) inventory;
    }

    @Inject(method = "stillValid", cancellable = true, at = @At("HEAD"))
    public void banner$unreachable(Player playerIn, CallbackInfoReturnable<Boolean> cir) {
        if (!bridge$checkReachable()) cir.setReturnValue(true);
    }

    @Override
    public CraftInventoryView getBukkitView() {
        if (bukkitEntity != null) {
            return bukkitEntity;
        }

        CraftInventory inventory = new CraftInventoryBeacon(this.beacon);
        bukkitEntity = new CraftInventoryView(this.playerInventory.player.getBukkitEntity(), inventory, (AbstractContainerMenu) (Object) this);
        return bukkitEntity;
    }
}
