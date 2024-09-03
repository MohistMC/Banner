package com.mohistmc.banner.injection.server.rcon;

import net.minecraft.commands.CommandSourceStack;

import java.net.SocketAddress;

public interface InjectionRconConsoleSource {

    default SocketAddress bridge$socketAddress() {
        throw new IllegalStateException("Not implemented");
    }

    default void banner$setSocketAddress(SocketAddress socketAddress) {
        throw new IllegalStateException("Not implemented");
    }

    default void sendMessage(String message) {
        throw new IllegalStateException("Not implemented");
    }

    default org.bukkit.command.CommandSender getBukkitSender(CommandSourceStack wrapper) {
        throw new IllegalStateException("Not implemented");
    }
}
