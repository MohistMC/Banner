package com.mohistmc.banner.mixin.commands.arguments.selector;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.selector.EntitySelector;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EntitySelector.class)
public class MixinEntitySelector {

    @Redirect(method = "checkPermissions", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/commands/CommandSourceStack;hasPermission(I)Z"))
    private boolean banner$checkPerm(CommandSourceStack instance, int permissionLevel) {
        return instance.hasPermission(permissionLevel, "minecraft.command.selector");// CraftBukkit
    }
}
