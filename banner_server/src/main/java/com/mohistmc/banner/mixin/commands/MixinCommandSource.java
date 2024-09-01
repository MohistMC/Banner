package com.mohistmc.banner.mixin.commands;

import com.mohistmc.banner.injection.commands.InjectionCommandSource;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import org.bukkit.command.CommandSender;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(CommandSource.class)
public interface MixinCommandSource extends InjectionCommandSource {

    default CommandSender getBukkitSender(CommandSourceStack wrapper) {
        return this.banner$getBukkitSender(wrapper);
    }
}
