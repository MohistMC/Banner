package com.mohistmc.banner.mixin.world.item;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DebugStickItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(DebugStickItem.class)
public class MixinDebugStickItem {

    @Redirect(method = "handleInteraction", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;canUseGameMasterBlocks()Z"))
    private boolean banner$permCheck(Player player) {
        boolean banner$flag = !player.canUseGameMasterBlocks()
                && !(player.getAbilities().instabuild
                && player.getBukkitEntity().hasPermission("minecraft.debugstick"))
                && !player.getBukkitEntity().hasPermission("minecraft.debugstick.always");
        return !banner$flag;
    }
}
