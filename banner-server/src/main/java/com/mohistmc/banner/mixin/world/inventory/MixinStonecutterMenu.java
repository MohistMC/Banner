package com.mohistmc.banner.mixin.world.inventory;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.StonecutterMenu;
import org.bukkit.craftbukkit.inventory.CraftInventoryStonecutter;
import org.bukkit.craftbukkit.inventory.CraftInventoryView;
import org.bukkit.craftbukkit.inventory.view.CraftStonecutterView;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(StonecutterMenu.class)
public abstract class MixinStonecutterMenu extends AbstractContainerMenu{


    // @formatter:off
    @Shadow @Final public Container container;
    @Shadow @Final
    ResultContainer resultContainer;
    // @formatter:on

    protected MixinStonecutterMenu(@Nullable MenuType<?> menuType, int i) {
        super(menuType, i);
    }

    private CraftStonecutterView bukkitEntity = null;
    private Inventory playerInventory;

    @Inject(method = "<init>(ILnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/world/inventory/ContainerLevelAccess;)V", at = @At("RETURN"))
    public void banner$init(int windowIdIn, Inventory playerInventoryIn, ContainerLevelAccess worldPosCallableIn, CallbackInfo ci) {
        this.playerInventory = playerInventoryIn;
    }

    @Override
    public CraftStonecutterView getBukkitView() {
        if (bukkitEntity != null) {
            return bukkitEntity;
        }

        CraftInventoryStonecutter inventory = new CraftInventoryStonecutter(this.container, this.resultContainer);
        bukkitEntity = new CraftStonecutterView(this.playerInventory.player.getBukkitEntity(), inventory, (StonecutterMenu) (Object) this);
        return bukkitEntity;
    }

}
