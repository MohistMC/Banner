package com.mohistmc.banner.mixin.world.inventory;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.LoomMenu;
import net.minecraft.world.inventory.MenuType;
import org.bukkit.craftbukkit.inventory.CraftInventoryLoom;
import org.bukkit.craftbukkit.inventory.CraftInventoryView;
import org.bukkit.craftbukkit.inventory.view.CraftLoomView;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LoomMenu.class)
public abstract class MixinLoomMenu extends AbstractContainerMenu{

    protected MixinLoomMenu(@Nullable MenuType<?> menuType, int i) {
        super(menuType, i);
    }

    // @formatter:off
    @Shadow @Final private Container inputContainer;
    @Shadow @Final private Container outputContainer;
    // @formatter:on

    private CraftLoomView bukkitEntity;
    private Inventory playerInventory;

    @Inject(method = "<init>(ILnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/world/inventory/ContainerLevelAccess;)V", at = @At("RETURN"))
    public void banner$init(int id, Inventory playerInventory, ContainerLevelAccess worldCallable, CallbackInfo ci) {
        this.playerInventory = playerInventory;
    }

    @Inject(method = "stillValid", cancellable = true, at = @At("HEAD"))
    public void banner$unreachable(Player playerIn, CallbackInfoReturnable<Boolean> cir) {
        if (!bridge$checkReachable()) {
            cir.setReturnValue(true);
        }
    }

    @Override
    public CraftLoomView getBukkitView() {
        if (bukkitEntity != null) {
            return bukkitEntity;
        }

        CraftInventoryLoom inventory = new CraftInventoryLoom(this.inputContainer, this.outputContainer);
        bukkitEntity = new CraftLoomView(this.playerInventory.player.getBukkitEntity(), inventory, (LoomMenu) (Object) this);
        return bukkitEntity;
    }
}
