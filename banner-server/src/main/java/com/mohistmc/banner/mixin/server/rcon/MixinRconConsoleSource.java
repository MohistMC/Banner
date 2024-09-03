package com.mohistmc.banner.mixin.server.rcon;

import com.mohistmc.banner.asm.annotation.CreateConstructor;
import com.mohistmc.banner.asm.annotation.ShadowConstructor;
import com.mohistmc.banner.injection.server.rcon.InjectionRconConsoleSource;
import java.net.SocketAddress;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.rcon.RconConsoleSource;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.command.CraftRemoteConsoleCommandSender;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(RconConsoleSource.class)
public abstract class MixinRconConsoleSource implements InjectionRconConsoleSource {

    @Shadow @Final private StringBuffer buffer;
    // CraftBukkit start
    public SocketAddress socketAddress;
    private CraftRemoteConsoleCommandSender remoteConsole = null;

    @ShadowConstructor
    public void banner$constructor(MinecraftServer pServer) {
        throw new RuntimeException();
    }

    @CreateConstructor
    public void banner$constructor(MinecraftServer pServer, SocketAddress socketAddress) {
        banner$constructor(pServer);
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
