package io.papermc.paper.event.block;

import org.bukkit.Material;
import org.bukkit.block.Beacon;
import org.bukkit.block.Block;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.BlockEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Called when a beacon is deactivated, either because its base block(s) or itself were destroyed.
 */
public class BeaconDeactivatedEvent extends BlockEvent {
    private static final HandlerList handlers = new HandlerList();

    public BeaconDeactivatedEvent(@NotNull Block block) {
        super(block);
    }

    /**
     * Returns the beacon that was deactivated.
     * This will return null if the beacon does not exist.
     * (which can occur after the deactivation of a now broken beacon)
     *
     * @return The beacon that got deactivated, or null if it does not exist.
     */
    @Nullable
    public Beacon getBeacon() {
        return block.getType() == Material.BEACON ? (Beacon) block.getState() : null;
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