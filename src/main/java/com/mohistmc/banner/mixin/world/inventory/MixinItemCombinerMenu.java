package com.mohistmc.banner.mixin.world.inventory;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ItemCombinerMenu;
import net.minecraft.world.inventory.MenuType;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemCombinerMenu.class)
public abstract class MixinItemCombinerMenu extends AbstractContainerMenu {

    protected MixinItemCombinerMenu(@Nullable MenuType<?> menuType, int i) {
        super(menuType, i);
    }

    @Inject(method = "stillValid", at = @At("HEAD"))
    private void banner$addCheckReachAble(Player player, CallbackInfoReturnable<Boolean> cir) {
        if (!this.bridge$checkReachable()) {
            cir.cancel();
        }
    }
}
