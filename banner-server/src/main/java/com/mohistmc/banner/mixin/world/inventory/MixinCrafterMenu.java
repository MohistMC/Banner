package com.mohistmc.banner.mixin.world.inventory;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CrafterMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.ResultContainer;
import org.bukkit.craftbukkit.inventory.CraftInventoryCrafter;
import org.bukkit.craftbukkit.inventory.CraftInventoryView;
import org.bukkit.craftbukkit.inventory.view.CraftCrafterView;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CrafterMenu.class)
public abstract class MixinCrafterMenu extends AbstractContainerMenu {


    @Shadow @Final private CraftingContainer container;
    @Shadow @Final private ResultContainer resultContainer;
    @Shadow @Final private Player player;

    private CraftCrafterView bukkitEntity;

    protected MixinCrafterMenu(@Nullable MenuType<?> menuType, int i) {
        super(menuType, i);
    }

    @Override
    public CraftCrafterView getBukkitView() {
        if (bukkitEntity != null) {
            return bukkitEntity;
        }

        CraftInventoryCrafter inventory = new CraftInventoryCrafter(this.container, this.resultContainer);
        bukkitEntity = new CraftCrafterView((((ServerPlayer) this.player).getBukkitEntity()), inventory, (CrafterMenu) (Object) this);
        return bukkitEntity;
    }

    @Inject(method = "stillValid", cancellable = true, at = @At("HEAD"))
    public void banner$unreachable(Player playerIn, CallbackInfoReturnable<Boolean> cir) {
        if (!bridge$checkReachable()) cir.setReturnValue(true);
    }
}
