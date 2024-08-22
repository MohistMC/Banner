package com.mohistmc.banner.mixin.core.server.network;

import io.izzel.arclight.mixin.Decorate;
import io.izzel.arclight.mixin.DecorationOps;
import net.fabricmc.fabric.api.networking.v1.FabricServerConfigurationNetworkHandler;
import net.minecraft.network.Connection;
import net.minecraft.network.TickablePacketListener;
import net.minecraft.network.protocol.configuration.ServerConfigurationPacketListener;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerLinks;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import net.minecraft.server.network.ServerConfigurationPacketListenerImpl;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.CraftServerLinks;
import org.bukkit.event.player.PlayerLinksSendEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ServerConfigurationPacketListenerImpl.class)
public abstract class MixinServerConfigurationPacketListenerImpl extends ServerCommonPacketListenerImpl implements ServerConfigurationPacketListener, TickablePacketListener, FabricServerConfigurationNetworkHandler {

    public MixinServerConfigurationPacketListenerImpl(MinecraftServer minecraftServer, Connection connection, CommonListenerCookie commonListenerCookie) {
        super(minecraftServer, connection, commonListenerCookie);
    }

    @Decorate(method = "startConfiguration", require = 0, at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;serverLinks()Lnet/minecraft/server/ServerLinks;"))
    private ServerLinks banner$sendLinksEvent(MinecraftServer instance) throws Throwable {
        var links = (ServerLinks) DecorationOps.callsite().invoke(instance);
        var wrapper = new CraftServerLinks(links);
        var event = new PlayerLinksSendEvent(this.bridge$player().getBukkitEntity(), wrapper);
        Bukkit.getPluginManager().callEvent(event);
        return wrapper.getServerLinks();
    }
}
