package com.mohistmc.banner.injection.server.network;

import java.util.Set;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.world.entity.RelativeMovement;
import org.bukkit.Location;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.slf4j.Logger;

public interface InjectionServerGamePacketListenerImpl extends InjectionServerCommonPacketListenerImpl {

    default CraftPlayer getCraftPlayer() {
        return null;
    }

    default void teleport(double d0, double d1, double d2, float f, float f1, PlayerTeleportEvent.TeleportCause cause) {
    }

    default boolean teleport(double d0, double d1, double d2, float f, float f1, Set<RelativeMovement> set, PlayerTeleportEvent.TeleportCause cause) { // CraftBukkit - Return event status
        return false;
    }

    default void teleport(Location dest) {
    }

    default void internalTeleport(double d0, double d1, double d2, float f, float f1, Set<RelativeMovement> set) {
    }

    default void chat(String s, PlayerChatMessage original, boolean async) {
    }

    default void handleCommand(String s) {
    }

    default boolean isDisconnected() {
        return false;
    }

    default boolean checkLimit(long timestamp) {
        return false;
    }

    default CraftServer bridge$craftServer() {
        return null;
    }

    default Logger bridge$logger() {
        return null;
    }

    default void pushTeleportCause(PlayerTeleportEvent.TeleportCause cause) {

    }
}
