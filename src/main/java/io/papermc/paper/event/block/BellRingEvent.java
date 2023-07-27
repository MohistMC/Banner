package io.papermc.paper.event.block;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.event.Cancellable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Called when a bell is rung.
 * @deprecated use {@link org.bukkit.event.block.BellRingEvent}
 */
@Deprecated
public class BellRingEvent extends org.bukkit.event.block.BellRingEvent implements Cancellable {

    public BellRingEvent(@NotNull Block block, @NotNull BlockFace direction, @Nullable Entity entity) {
        super(block, direction, entity);
    }
}