package com.mohistmc.banner.injection.server.players;

import java.util.UUID;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.ServerStatsCounter;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.bukkit.Location;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.event.player.PlayerRespawnEvent;

public interface InjectionPlayerList {

    default CraftServer getCraftServer() {
        throw new IllegalStateException("Not implemented");
    }

    default ServerPlayer respawn(ServerPlayer entityplayer, boolean flag, Entity.RemovalReason entity_removalreason, PlayerRespawnEvent.RespawnReason reason) {
        throw new IllegalStateException("Not implemented");
    }

    default ServerPlayer respawn(ServerPlayer entityplayer, ServerLevel worldserver, boolean flag, Location location, boolean avoidSuffocation, Entity.RemovalReason entity_removalreason, PlayerRespawnEvent.RespawnReason reason) {
        throw new IllegalStateException("Not implemented");
    }

    default void broadcastAll(Packet<?> packet, Player entityhuman) {
        throw new IllegalStateException("Not implemented");
    }

    default void broadcastAll(Packet<?> packet, Level world) {
        throw new IllegalStateException("Not implemented");
    }

    default void broadcastMessage(Component[] iChatBaseComponents) {
        throw new IllegalStateException("Not implemented");
    }

    default ServerStatsCounter getPlayerStats(ServerPlayer entityhuman) {
        throw new IllegalStateException("Not implemented");
    }

    default ServerStatsCounter getPlayerStats(UUID uuid, String displayName) {
        throw new IllegalStateException("Not implemented");
    }

    default String bridge$quiltMsg() {
        throw new IllegalStateException("Not implemented");
    }

    default ServerPlayer player() {
        throw new IllegalStateException("Not implemented");
    }
}
