package com.mohistmc.banner.injection.server.network;

import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.craftbukkit.entity.CraftPlayer;

public interface InjectionServerCommonPacketListenerImpl {

    default void disconnect(String s) {
        throw new IllegalStateException("Not implemented");
    }

    default boolean bridge$processedDisconnect() {
        throw new IllegalStateException("Not implemented");
    }

    default boolean banner$isDisconnected() {
        throw new IllegalStateException("Not implemented");
    }

    default void banner$setPlayer(ServerPlayer player) {
        throw new IllegalStateException("Not implemented");
    }

    default void banner$setProcessedDisconnect(boolean processedDisconnect) {
        throw new IllegalStateException("Not implemented");
    }

    default CraftPlayer getCraftPlayer() {
        throw new IllegalStateException("Not implemented");
    }

    default boolean isTransferred() {
        throw new IllegalStateException("Not implemented");
    }

    default ConnectionProtocol getProtocol() {
        throw new IllegalStateException("Not implemented");
    }

    default void sendPacket(Packet<?> packet) {
        throw new IllegalStateException("Not implemented");
    }

    default ServerPlayer bridge$player() {
        throw new IllegalStateException("Not implemented");
    }
}
