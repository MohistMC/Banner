package com.mohistmc.banner.injection.server.rcon;

import java.net.SocketAddress;
import net.minecraft.commands.CommandSourceStack;

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
