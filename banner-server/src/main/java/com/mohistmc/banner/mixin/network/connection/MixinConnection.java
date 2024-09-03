package com.mohistmc.banner.mixin.network.connection;

import com.mohistmc.banner.injection.network.connection.InjectionConnection;
import com.mojang.authlib.properties.Property;
import io.netty.channel.Channel;
import net.minecraft.network.Connection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.net.SocketAddress;
import java.util.UUID;

@Mixin(Connection.class)
public class MixinConnection implements InjectionConnection {

    @Shadow public Channel channel;
    public String hostname = ""; // CraftBukkit - add field
    public java.util.UUID spoofedUUID;
    public com.mojang.authlib.properties.Property[] spoofedProfile;

    @Override
    public String bridge$hostname() {
        return hostname;
    }

    @Override
    public void banner$setHostName(String hostName) {
        hostname = hostName;
    }

    // Spigot Start
    @Override
    public SocketAddress getRawAddress() {
        return this.channel.remoteAddress();
    }
    // Spigot End

    @Override
    public UUID bridge$spoofedUUID() {
        return spoofedUUID;
    }

    @Override
    public void banner$setSpoofedUUID(UUID spoofedUUID) {
        this.spoofedUUID = spoofedUUID;
    }

    @Override
    public Property[] bridge$spoofedProfile() {
        return spoofedProfile;
    }

    @Override
    public void bridge$setSpoofedProfile(Property[] spoofedProfile) {
        this.spoofedProfile = spoofedProfile;
    }

}
