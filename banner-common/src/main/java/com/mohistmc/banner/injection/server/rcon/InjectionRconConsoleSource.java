package com.mohistmc.banner.injection.server.rcon;

import net.minecraft.commands.CommandSourceStack;

public interface InjectionRconConsoleSource {

    default void sendMessage(String message) {
    }

    default org.bukkit.command.CommandSender getBukkitSender(CommandSourceStack wrapper) {
        return null;
    }
}
