package com.mohistmc.banner.mixin.core.world.item;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DebugStickItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(DebugStickItem.class)
public class MixinDebugStickItem {

    @Redirect(method = "handleInteraction", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;canUseGameMasterBlocks()Z"))
    private boolean banner$permCheck(Player instance) {
        return !instance.canUseGameMasterBlocks()
                && !(instance.getAbilities().instabuild
                && instance.getBukkitEntity().hasPermission("minecraft.debugstick"))
                && !instance.getBukkitEntity().hasPermission("minecraft.debugstick.always");
    }
}
