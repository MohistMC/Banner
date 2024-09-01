package com.mohistmc.banner.mixin.world.inventory;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CartographyTableMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.ResultContainer;
import org.bukkit.craftbukkit.inventory.CraftInventoryCartography;
import org.bukkit.craftbukkit.inventory.CraftInventoryView;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryView;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CartographyTableMenu.class)
public abstract class MixinCartographyTableMenu extends AbstractContainerMenu {

    @Shadow @Final public Container container;
    @Shadow @Final private ResultContainer resultContainer;
    // CraftBukkit start
    private CraftInventoryView bukkitEntity = null;
    private Player player;

    protected MixinCartographyTableMenu(@Nullable MenuType<?> menuType, int i) {
        super(menuType, i);
    }

    @Override
    public InventoryView getBukkitView() {
        if (bukkitEntity != null) {
            return bukkitEntity;
        }

        CraftInventoryCartography inventory = new CraftInventoryCartography(this.container, this.resultContainer);
        bukkitEntity = new CraftInventoryView(this.player, inventory, this);
        return bukkitEntity;
    }

    @Inject(method = "stillValid", at = @At("HEAD"))
    private void banner$addCheckValid(net.minecraft.world.entity.player.Player player, CallbackInfoReturnable<Boolean> cir) {
        if (!this.bridge$checkReachable()) {
            cir.cancel();
        }
    }

    @Inject(method = "<init>(ILnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/world/inventory/ContainerLevelAccess;)V",
            at = @At("TAIL"))
    private void banner$setPlayer(int i, Inventory inventory, ContainerLevelAccess containerLevelAccess, CallbackInfo ci) {
        player = (Player) inventory.player.getBukkitEntity(); // CraftBukkit
    }


}
