package io.papermc.paper.math;

import org.bukkit.Axis;
import org.bukkit.block.BlockFace;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * A position represented with integers.
 * <p>
 * <b>May see breaking changes until Experimental annotation is removed.</b>
 * @see FinePosition
 */
@ApiStatus.Experimental
public interface BlockPosition extends Position {

    @Override
    default double x() {
        return this.blockX();
    }

    @Override
    default double y() {
        return this.blockY();
    }

    @Override
    default double z() {
        return this.blockZ();
    }

    @Override
    default boolean isBlock() {
        return true;
    }

    @Override
    default boolean isFine() {
        return false;
    }

    @Override
    default @NotNull BlockPosition toBlock() {
        return this;
    }

    @Override
    default @NotNull BlockPosition offset(int x, int y, int z) {
        return x == 0 && y == 0 && z == 0 ? this : new BlockPositionImpl(this.blockX() + x, this.blockY() + y, this.blockZ() + z);
    }

    @Override
    default @NotNull FinePosition offset(double x, double y, double z) {
        return new FinePositionImpl(this.blockX() + x, this.blockY() + y, this.blockZ() + z);
    }

    /**
     * Returns a block position offset by 1 in the direction specified.
     *
     * @param blockFace the block face to offset towards
     * @return the offset block position
     */
    @Contract(value = "_ -> new", pure = true)
    default @NotNull BlockPosition offset(@NotNull BlockFace blockFace) {
        return this.offset(blockFace, 1);
    }

    /**
     * Returns a block position offset in the direction specified
     * multiplied by the amount.
     *
     * @param blockFace the block face to offset towards
     * @param amount the number of times to move in that direction
     * @return the offset block position
     */
    @Contract(pure = true)
    default @NotNull BlockPosition offset(@NotNull BlockFace blockFace, int amount) {
        return amount == 0 ? this : new BlockPositionImpl(this.blockX() + (blockFace.getModX() * amount), this.blockY() + (blockFace.getModY() * amount), this.blockZ() + (blockFace.getModZ() * amount));
    }

    /**
     * Returns a block position offset by the amount along
     * the specified axis.
     *
     * @param axis the axis to offset along
     * @param amount the amount to offset along that axis
     * @return the offset block position
     */
    @Contract(pure = true)
    default @NotNull BlockPosition offset(@NotNull Axis axis, int amount) {
        return amount == 0 ? this : switch (axis) {
            case X -> new BlockPositionImpl(this.blockX() + amount, this.blockY(), this.blockZ());
            case Y -> new BlockPositionImpl(this.blockX(), this.blockY() + amount, this.blockZ());
            case Z -> new BlockPositionImpl(this.blockX(), this.blockY(), this.blockZ() + amount);
        };
    }
}