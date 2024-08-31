package com.mohistmc.banner.injection.server.level;

import net.minecraft.server.level.Ticket;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.level.ChunkPos;

public interface InjectionDistanceManager {

    default boolean addTicket(long i, Ticket<?> ticket) {
        throw new IllegalStateException("Not implemented");
    }

    default boolean removeTicket(long i, Ticket<?> ticket) {
        throw new IllegalStateException("Not implemented");
    }

    default <T> boolean addRegionTicketAtDistance(TicketType<T> tickettype, ChunkPos chunkcoordintpair, int i, T t0) {
        throw new IllegalStateException("Not implemented");
    }

    default <T> boolean removeRegionTicketAtDistance(TicketType<T> tickettype, ChunkPos chunkcoordintpair, int i, T t0) {
        throw new IllegalStateException("Not implemented");
    }

    default <T> void removeAllTicketsFor(TicketType<T> ticketType, int ticketLevel, T ticketIdentifier) {
        throw new IllegalStateException("Not implemented");
    }
}
