package com.mohistmc.banner.injection.server.players;

import com.mojang.authlib.GameProfile;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerLoginPacketListenerImpl;
import net.minecraft.stats.ServerStatsCounter;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.bukkit.Location;
import org.bukkit.event.player.PlayerRespawnEvent;

import java.util.UUID;

public interface InjectionPlayerList {

    default String remove(ServerPlayer entityplayer) { // CraftBukkit - return string
        return null;
    }

    default ServerPlayer canPlayerLogin(ServerLoginPacketListenerImpl loginlistener, GameProfile gameprofile) {
        return null;
    }

    default ServerPlayer getPlayerForLogin(GameProfile gameprofile, ServerPlayer player) { // CraftBukkit - added EntityPlayer
        return null;
    }

    default ServerPlayer respawn(ServerPlayer entityplayer, boolean flag, PlayerRespawnEvent.RespawnReason reason) {
        return null;
    }

    default ServerPlayer respawn(ServerPlayer entityplayer, ServerLevel worldserver, boolean flag, Location location, boolean avoidSuffocation, PlayerRespawnEvent.RespawnReason reason) {
        return null;
    }

    default void broadcastAll(Packet packet, Player entityhuman) {
    }

    default void broadcastAll(Packet packet, Level world) {
    }

    default void broadcastMessage(Component[] iChatBaseComponents) {
    }

    default ServerStatsCounter getPlayerStats(ServerPlayer entityhuman) {
        return null;
    }

    default ServerStatsCounter getPlayerStats(UUID uuid, String displayName) {
        return null;
    }
}
