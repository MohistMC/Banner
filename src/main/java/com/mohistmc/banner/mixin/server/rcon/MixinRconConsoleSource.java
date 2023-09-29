package com.mohistmc.banner.mixin.server.rcon;

import com.mohistmc.banner.injection.server.rcon.InjectionRconConsoleSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.rcon.RconConsoleSource;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_20_R1.command.CraftRemoteConsoleCommandSender;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.net.SocketAddress;

@Mixin(RconConsoleSource.class)
public abstract class MixinRconConsoleSource implements InjectionRconConsoleSource {

    @Shadow @Final private StringBuffer buffer;
    // CraftBukkit start
    public SocketAddress socketAddress;
    private CraftRemoteConsoleCommandSender remoteConsole = null;

    public void banner$constructor(MinecraftServer pServer, SocketAddress socketAddress) {
        this.socketAddress = socketAddress;
    }

    @Override
    public SocketAddress bridge$socketAddress() {
        return socketAddress;
    }

    @Override
    public void banner$setSocketAddress(SocketAddress socketAddress) {
        this.socketAddress = socketAddress;
    }

    @Override
    public void sendMessage(String message) {
        this.buffer.append(message);
    }

    @Override
    public CommandSender getBukkitSender(CommandSourceStack wrapper) {
        if (remoteConsole == null) {
            remoteConsole = new CraftRemoteConsoleCommandSender((RconConsoleSource) (Object) this);
        }
        return this.remoteConsole;
    }
}
