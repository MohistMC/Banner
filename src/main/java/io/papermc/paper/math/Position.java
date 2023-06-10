package io.papermc.paper.math;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * Common interface for {@link FinePosition} and {@link BlockPosition}.
 * <p>
 * <b>May see breaking changes until Experimental annotation is removed.</b>
 */
@ApiStatus.Experimental
public interface Position {

    FinePosition FINE_ZERO = new FinePositionImpl(0, 0, 0);
    BlockPosition BLOCK_ZERO = new BlockPositionImpl(0, 0, 0);

    /**
     * Gets the block x value for this position
     *
     * @return the block x value
     */
    int blockX();

    /**
     * Gets the block x value for this position
     *
     * @return the block x value
     */
    int blockY();

    /**
     * Gets the block x value for this position
     *
     * @return the block x value
     */
    int blockZ();

    /**
     * Gets the x value for this position
     *
     * @return the x value
     */
    double x();

    /**
     * Gets the y value for this position
     *
     * @return the y value
     */
    double y();

    /**
     * Gets the z value for this position
     *
     * @return the z value
     */
    double z();

    /**
     * Checks of this position represents a {@link BlockPosition}
     *
     * @return true if block
     */
    boolean isBlock();

    /**
     * Checks if this position represents a {@link FinePosition}
     *
     * @return true if fine
     */
    boolean isFine();

    /**
     * Returns a position offset by the specified amounts.
     *
     * @param x x value to offset
     * @param y y value to offset
     * @param z z value to offset
     * @return the offset position
     */
    @NotNull Position offset(int x, int y, int z);

    /**
     * Returns a position offset by the specified amounts.
     *
     * @param x x value to offset
     * @param y y value to offset
     * @param z z value to offset
     * @return the offset position
     */
    @NotNull FinePosition offset(double x, double y, double z);

    /**
     * Returns a new position at the center of the block position this represents
     *
     * @return a new center position
     */
    @Contract(value = "-> new", pure = true)
    default @NotNull FinePosition toCenter() {
        return new FinePositionImpl(this.blockX() + 0.5, this.blockY() + 0.5, this.blockZ() + 0.5);
    }

    /**
     * Returns the block position of this position
     * or itself if it already is a block position
     *
     * @return the block position
     */
    @Contract(pure = true)
    @NotNull BlockPosition toBlock();

    /**
     * Converts this position to a vector
     *
     * @return a new vector
     */
    @Contract(value = "-> new", pure = true)
    default @NotNull Vector toVector() {
        return new Vector(this.x(), this.y(), this.z());
    }

    /**
     * Creates a new location object at this position with the specified world
     *
     * @param world the world for the location object
     * @return a new location
     */
    @Contract(value = "_ -> new", pure = true)
    default @NotNull Location toLocation(@NotNull World world) {
        return new Location(world, this.x(), this.y(), this.z());
    }

    /**
     * Creates a position at the coordinates
     *
     * @param x x coord
     * @param y y coord
     * @param z z coord
     * @return a position with those coords
     */
    @Contract(value = "_, _, _ -> new", pure = true)
    static @NotNull BlockPosition block(int x, int y, int z) {
        return new BlockPositionImpl(x, y, z);
    }

    /**
     * Creates a position from the location.
     *
     * @param location the location to copy the position of
     * @return a new position at that location
     */
    @Contract(value = "_ -> new", pure = true)
    static @NotNull BlockPosition block(@NotNull Location location) {
        return new BlockPositionImpl(location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    /**
     * Creates a position at the coordinates
     *
     * @param x x coord
     * @param y y coord
     * @param z z coord
     * @return a position with those coords
     */
    @Contract(value = "_, _, _ -> new", pure = true)
    static @NotNull FinePosition fine(double x, double y, double z) {
        return new FinePositionImpl(x, y, z);
    }

    /**
     * Creates a position from the location.
     *
     * @param location the location to copy the position of
     * @return a new position at that location
     */
    @Contract(value = "_ -> new", pure = true)
    static @NotNull FinePosition fine(@NotNull Location location) {
        return new FinePositionImpl(location.getX(), location.getY(), location.getZ());
    }
}