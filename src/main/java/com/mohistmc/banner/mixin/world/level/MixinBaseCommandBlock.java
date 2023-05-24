package com.mohistmc.banner.mixin.world.level;

import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.world.level.BaseCommandBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(BaseCommandBlock.class)
public abstract class MixinBaseCommandBlock implements CommandSource {

    // CraftBukkit start
    @Override
    public abstract org.bukkit.command.CommandSender getBukkitSender(CommandSourceStack wrapper);

    @Redirect(method = "performCommand",
            at = @At(value = "INVOKE",
            target = "Lnet/minecraft/commands/Commands;performPrefixedCommand(Lnet/minecraft/commands/CommandSourceStack;Ljava/lang/String;)I"))
    private int banner$dispatchCommand(Commands instance, CommandSourceStack source, String command) {
        return instance.dispatchServerCommand(source, command);
    }
}
