package io.papermc.paper.event.block;

import org.bukkit.block.Beacon;
import org.bukkit.block.Block;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.BlockEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a beacon is activated.
 * Activation occurs when the beacon beam becomes visible.
 */
public class BeaconActivatedEvent extends BlockEvent {
    private static final HandlerList handlers = new HandlerList();

    public BeaconActivatedEvent(@NotNull Block block) {
        super(block);
    }

    /**
     * Returns the beacon that was activated.
     *
     * @return the beacon that was activated.
     */
    @NotNull
    public Beacon getBeacon() {
        return (Beacon) block.getState();
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return handlers;
    }
}