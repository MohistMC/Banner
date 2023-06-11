package com.mohistmc.banner.mixin.server.network;

import com.mohistmc.banner.bukkit.BannerServerListPingEvent;
import com.mohistmc.banner.bukkit.BukkitExtraConstants;
import com.mojang.authlib.GameProfile;
import net.minecraft.SharedConstants;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.status.ClientboundStatusResponsePacket;
import net.minecraft.network.protocol.status.ServerStatus;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerStatusPacketListenerImpl;
import org.bukkit.craftbukkit.v1_20_R1.util.CraftChatMessage;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Collections;
import java.util.Optional;

@Mixin(ServerStatusPacketListenerImpl.class)
public class MixinServerStatusPacketListenerImpl {

    @Shadow @Final private Connection connection;

    @Redirect(method = "handleStatusRequest", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/Connection;send(Lnet/minecraft/network/protocol/Packet;)V"))
    private void banner$handleServerPing(Connection networkManager, Packet<?> packetIn) {
        // CraftBukkit start
        MinecraftServer server = BukkitExtraConstants.getServer();
        final Object[] players = server.getPlayerList().players.toArray();
        BannerServerListPingEvent event = new BannerServerListPingEvent(networkManager, server);
        server.bridge$server().getPluginManager().callEvent(event);

        java.util.List<GameProfile> profiles = new java.util.ArrayList<GameProfile>(players.length);
        for (Object player : players) {
            if (player != null) {
                ServerPlayer entityPlayer = ((ServerPlayer) player);
                if (entityPlayer.allowsListing()) {
                    profiles.add(entityPlayer.getGameProfile());
                } else {
                    profiles.add(MinecraftServer.ANONYMOUS_PLAYER_PROFILE);
                }
            }
        }

        ServerStatus.Players playerSample = new ServerStatus.Players(event.getMaxPlayers(), profiles.size(), (server.hidesOnlinePlayers()) ? Collections.emptyList() : profiles);

        ServerStatus ping = new ServerStatus(
                CraftChatMessage.fromString(event.getMotd(), true)[0],
                Optional.of(playerSample),
                Optional.of(new ServerStatus.Version(server.getServerModName() + " " + server.getServerVersion(), SharedConstants.getCurrentVersion().getProtocolVersion())),
                (event.icon.value != null) ? Optional.of(new ServerStatus.Favicon(event.icon.value)) : Optional.empty(),
                server.enforceSecureProfile()
        );

        this.connection.send(new ClientboundStatusResponsePacket(ping));
        // CraftBukkit end
    }
}
