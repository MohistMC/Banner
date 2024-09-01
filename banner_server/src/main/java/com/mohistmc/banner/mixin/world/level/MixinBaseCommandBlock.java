package com.mohistmc.banner.mixin.world.level;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.world.level.BaseCommandBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(BaseCommandBlock.class)
public class MixinBaseCommandBlock {

    @Redirect(method = "performCommand", at = @At(value = "INVOKE", target = "Lnet/minecraft/commands/Commands;performPrefixedCommand(Lnet/minecraft/commands/CommandSourceStack;Ljava/lang/String;)V"))
    private void banner$useBukkitStyle(Commands instance, CommandSourceStack commandSourceStack, String string) {
        instance.dispatchServerCommand(commandSourceStack, string);// CraftBukkit
    }
}
