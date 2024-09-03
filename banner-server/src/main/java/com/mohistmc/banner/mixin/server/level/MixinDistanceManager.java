package com.mohistmc.banner.mixin.server.level;

import com.llamalad7.mixinextras.sugar.Local;
import com.mohistmc.banner.injection.server.level.InjectionDistanceManager;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import java.util.Iterator;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.DistanceManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.Ticket;
import net.minecraft.server.level.TicketType;
import net.minecraft.server.level.TickingTracker;
import net.minecraft.util.SortedArraySet;
import net.minecraft.world.level.ChunkPos;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// TODO fix inject method
@Mixin(DistanceManager.class)
public abstract class MixinDistanceManager implements InjectionDistanceManager {


    // @formatter:off
    @Shadow @Final private DistanceManager.ChunkTicketTracker ticketTracker;
    @Shadow protected abstract SortedArraySet<Ticket<?>> getTickets(long p_229848_1_);
    @Shadow private static int getTicketLevelAt(SortedArraySet<Ticket<?>> p_229844_0_) { return 0; }
    @Shadow @Final public Long2ObjectOpenHashMap<SortedArraySet<Ticket<?>>> tickets;
    @Shadow abstract TickingTracker tickingTracker();
    // @formatter:on

    @Shadow private long ticketTickCounter;

    @Inject(method = "removePlayer", cancellable = true, at = @At(value = "INVOKE", remap = false, target = "Lit/unimi/dsi/fastutil/objects/ObjectSet;remove(Ljava/lang/Object;)Z"))
    private void banner$remove(SectionPos p_140829_, ServerPlayer p_140830_, CallbackInfo ci, @Local ObjectSet<?> set) {
        if (set == null) {
            ci.cancel();
        }
    }

    @Override
    public <T> boolean addRegionTicketAtDistance(TicketType<T> type, ChunkPos pos, int level, T value) {
        var ticket = new Ticket<>(type, 33 - level, value);
        var ret = this.addTicket(pos.toLong(), ticket);
        this.tickingTracker().addTicket(pos.toLong(), ticket);
        return ret;
    }

    @Override
    public boolean addTicket(long chunkPosIn, Ticket<?> ticketIn) {
        SortedArraySet<Ticket<?>> ticketSet = this.getTickets(chunkPosIn);
        int level = getTicketLevelAt(ticketSet);
        Ticket<?> ticket = ticketSet.addOrGet(ticketIn);
        ticket.setCreatedTick(this.ticketTickCounter);
        if (ticketIn.getTicketLevel() < level) {
            this.ticketTracker.update(chunkPosIn, ticketIn.getTicketLevel(), true);
        }
        if (ticket.getTicketLevel() < chunkPosIn) {
            this.ticketTracker.update(chunkPosIn, ticket.getTicketLevel(), true);
        }
        return ticketIn == ticket;
    }

    @Override
    public <T> boolean removeRegionTicketAtDistance(TicketType<T> type, ChunkPos pos, int level, T value) {
        var ticket = new Ticket<>(type, 33 - level, value);
        var ret = this.removeTicket(pos.toLong(), ticket);
        this.tickingTracker().removeTicket(pos.toLong(), ticket);
        return ret;
    }

    @Override
    public boolean removeTicket(long chunkPosIn, Ticket<?> ticketIn) {
        SortedArraySet<Ticket<?>> ticketSet = this.getTickets(chunkPosIn);
        boolean removed = false;
        if (ticketSet.remove(ticketIn)) {
            removed = true;
        }
        if (ticketSet.isEmpty()) {
            this.tickets.remove(chunkPosIn);
        }
        this.ticketTracker.update(chunkPosIn, getTicketLevelAt(ticketSet), false);
        return removed;
    }

    @Override
    public <T> void removeAllTicketsFor(TicketType<T> ticketType, int ticketLevel, T ticketIdentifier) {
        Ticket<T> target = new Ticket<>(ticketType, ticketLevel, ticketIdentifier);
        Iterator<Long2ObjectMap.Entry<SortedArraySet<Ticket<?>>>> iterator = this.tickets.long2ObjectEntrySet().fastIterator();
        while (iterator.hasNext()) {
            Long2ObjectMap.Entry<SortedArraySet<Ticket<?>>> entry = iterator.next();
            SortedArraySet<Ticket<?>> tickets = entry.getValue();
            if (tickets.remove(target)) {
                this.ticketTracker.update(entry.getLongKey(), getTicketLevelAt(tickets), false);
                if (tickets.isEmpty()) {
                    iterator.remove();
                }
            }
        }
    }
}
