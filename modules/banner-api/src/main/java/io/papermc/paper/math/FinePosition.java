package io.papermc.paper.math;

import org.bukkit.util.NumberConversions;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * A position represented with doubles.
 * <p>
 * <b>May see breaking changes until Experimental annotation is removed.</b>
 * @see BlockPosition
 */
@ApiStatus.Experimental
public interface FinePosition extends Position {

    @Override
    default int blockX() {
        return NumberConversions.floor(this.x());
    }

    @Override
    default int blockY() {
        return NumberConversions.floor(this.y());
    }

    @Override
    default int blockZ() {
        return NumberConversions.floor(this.z());
    }

    @Override
    default boolean isBlock() {
        return false;
    }

    @Override
    default boolean isFine() {
        return true;
    }

    @Override
    default @NotNull BlockPosition toBlock() {
        return new BlockPositionImpl(this.blockX(), this.blockY(), this.blockZ());
    }

    @Override
    default @NotNull FinePosition offset(int x, int y, int z) {
        return this.offset((double) x, y, z);
    }

    @Override
    default @NotNull FinePosition offset(double x, double y, double z) {
        return x == 0.0 && y == 0.0 && z == 0.0 ? this : new FinePositionImpl(this.x() + x, this.y() + y, this.z() + z);
    }
}