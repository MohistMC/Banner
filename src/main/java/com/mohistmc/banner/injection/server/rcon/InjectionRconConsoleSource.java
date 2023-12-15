package com.mohistmc.banner.injection.server.rcon;

import net.minecraft.commands.CommandSourceStack;

import java.net.SocketAddress;

public interface InjectionRconConsoleSource {

    default SocketAddress bridge$socketAddress() {
        return null;
    }

    default void banner$setSocketAddress(SocketAddress socketAddress) {

    }

    default void sendMessage(String message) {
    }

    default org.bukkit.command.CommandSender getBukkitSender(CommandSourceStack wrapper) {
        return null;
    }
}
