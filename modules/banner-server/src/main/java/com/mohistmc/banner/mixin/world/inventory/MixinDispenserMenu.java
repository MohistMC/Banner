package com.mohistmc.banner.mixin.world.inventory;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.DispenserMenu;
import net.minecraft.world.inventory.MenuType;
import org.bukkit.craftbukkit.inventory.CraftInventory;
import org.bukkit.craftbukkit.inventory.CraftInventoryView;
import org.bukkit.inventory.InventoryView;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DispenserMenu.class)
public abstract class MixinDispenserMenu extends AbstractContainerMenu {

    @Shadow @Final public Container dispenser;
    // CraftBukkit start
    private CraftInventoryView bukkitEntity = null;
    private Inventory player;

    protected MixinDispenserMenu(@Nullable MenuType<?> menuType, int i) {
        super(menuType, i);
    }
    // CraftBukkit end

    @Inject(method = "<init>(ILnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/world/Container;)V", at = @At("RETURN"))
    private void banner$init(int i, Inventory inventory, Container container, CallbackInfo ci) {
        // CraftBukkit start - Save player
        this.player = inventory;
    }

    @Inject(method = "stillValid", at= @At("HEAD"))
    private void banner$checkValid(Player player, CallbackInfoReturnable<Boolean> cir) {
        if (!this.bridge$checkReachable()) {
            cir.cancel();
        }
    }

    @Override
    public InventoryView getBukkitView() {
        if (bukkitEntity != null) {
            return bukkitEntity;
        }

        CraftInventory inventory = new CraftInventory(this.dispenser);
        bukkitEntity = new CraftInventoryView(this.player.player.getBukkitEntity(), inventory, ((DispenserMenu) (Object) this));
        return bukkitEntity;
    }
    // CraftBukkit end
}
