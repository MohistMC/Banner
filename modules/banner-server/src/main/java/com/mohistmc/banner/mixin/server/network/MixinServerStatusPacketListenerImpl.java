package com.mohistmc.banner.mixin.server.network;

import com.mohistmc.banner.bukkit.BannerServerListPingEvent;
import com.mohistmc.banner.bukkit.BukkitMethodHooks;
import com.mojang.authlib.GameProfile;
import java.util.Collections;
import java.util.Optional;
import net.minecraft.SharedConstants;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.status.ClientboundStatusResponsePacket;
import net.minecraft.network.protocol.status.ServerStatus;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerStatusPacketListenerImpl;
import org.bukkit.craftbukkit.util.CraftChatMessage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerStatusPacketListenerImpl.class)
public class MixinServerStatusPacketListenerImpl {

    @Redirect(method = "handleStatusRequest", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/Connection;send(Lnet/minecraft/network/protocol/Packet;)V"))
    public void banner$handleServerPing(Connection networkManager, Packet<?> packetIn) {
        // CraftBukkit start
        MinecraftServer server = BukkitMethodHooks.getServer();

        BannerServerListPingEvent event = new BannerServerListPingEvent(networkManager, server);
        server.bridge$server().getPluginManager().callEvent(event);

        final Object[] players = event.getPlayers();

        java.util.List<GameProfile> profiles = new java.util.ArrayList<>(players.length);
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

        // Spigot Start
        if ( !server.hidesOnlinePlayers() && !profiles.isEmpty() ) {
            java.util.Collections.shuffle(profiles); // This sucks, its inefficient but we have no simple way of doing it differently
            profiles = profiles.subList(0, Math.min(profiles.size(), org.spigotmc.SpigotConfig.playerSample)); // Cap the sample to n (or less) displayed players, ie: Vanilla behaviour
        }
        // Spigot End

        ServerStatus.Players playerSample = new ServerStatus.Players(event.getMaxPlayers(), event.getNumPlayers(), (server.hidesOnlinePlayers()) ? Collections.emptyList() : profiles);

        ServerStatus ping = new ServerStatus(
                CraftChatMessage.fromString(event.getMotd(), true)[0],
                Optional.of(playerSample),
                Optional.of(new ServerStatus.Version(server.getServerModName() + " " + server.getServerVersion(), SharedConstants.getCurrentVersion().getProtocolVersion())),
                (event.icon.value != null) ? Optional.of(new ServerStatus.Favicon(event.icon.value)) : Optional.empty(),
                server.enforceSecureProfile()
        );
        networkManager.send(new ClientboundStatusResponsePacket(ping));
        // CraftBukkit end
    }
}
