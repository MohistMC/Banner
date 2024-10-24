package com.mohistmc.banner.injection.commands;

import net.minecraft.commands.CommandSourceStack;

public interface InjectionCommandSource {

    default org.bukkit.command.CommandSender banner$getBukkitSender(CommandSourceStack wrapper) {
        throw new IllegalStateException("Not implemented");
    }
}
