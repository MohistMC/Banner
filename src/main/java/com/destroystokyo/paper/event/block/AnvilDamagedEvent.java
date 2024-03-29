package com.destroystokyo.paper.event.block;

import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.inventory.InventoryEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.InventoryView;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Called when an anvil is damaged from being used
 */
public class AnvilDamagedEvent extends InventoryEvent implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private boolean cancel;
    private DamageState damageState;

    public AnvilDamagedEvent(@NotNull InventoryView inventory, @NotNull BlockData blockData) {
        super(inventory);
        this.damageState = DamageState.getState(blockData);
    }

    @NotNull
    @Override
    public AnvilInventory getInventory() {
        return (AnvilInventory) super.getInventory();
    }

    /**
     * Gets the new state of damage on the anvil
     *
     * @return Damage state
     */
    @NotNull
    public DamageState getDamageState() {
        return damageState;
    }

    /**
     * Sets the new state of damage on the anvil
     *
     * @param damageState Damage state
     */
    public void setDamageState(@NotNull DamageState damageState) {
        this.damageState = damageState;
    }

    /**
     * Gets if anvil is breaking on this use
     *
     * @return True if breaking
     */
    public boolean isBreaking() {
        return damageState == DamageState.BROKEN;
    }

    /**
     * Sets if anvil is breaking on this use
     *
     * @param breaking True if breaking
     */
    public void setBreaking(boolean breaking) {
        if (breaking) {
            damageState = DamageState.BROKEN;
        } else if (damageState == DamageState.BROKEN) {
            damageState = DamageState.DAMAGED;
        }
    }

    public boolean isCancelled() {
        return cancel;
    }

    public void setCancelled(boolean cancel) {
        this.cancel = cancel;
    }

    @NotNull
    public HandlerList getHandlers() {
        return handlers;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return handlers;
    }

    /**
     * Represents the amount of damage on an anvil block
     */
    public enum DamageState {
        FULL(Material.ANVIL),
        CHIPPED(Material.CHIPPED_ANVIL),
        DAMAGED(Material.DAMAGED_ANVIL),
        BROKEN(Material.AIR);

        private Material material;

        DamageState(@NotNull Material material) {
            this.material = material;
        }

        /**
         * Get block material of this state
         *
         * @return Material
         */
        @NotNull
        public Material getMaterial() {
            return material;
        }

        /**
         * Get damaged state by block data
         *
         * @param blockData Block data
         * @return DamageState
         * @throws IllegalArgumentException If non anvil block data is given
         */
        @NotNull
        public static DamageState getState(@Nullable BlockData blockData) {
            return blockData == null ? BROKEN : getState(blockData.getMaterial());
        }

        /**
         * Get damaged state by block material
         *
         * @param material Block material
         * @return DamageState
         * @throws IllegalArgumentException If non anvil material is given
         */
        @NotNull
        public static DamageState getState(@Nullable Material material) {
            if (material == null) {
                return BROKEN;
            }
            for (DamageState state : values()) {
                if (state.material == material) {
                    return state;
                }
            }
            throw new IllegalArgumentException("Material not an anvil");
        }
    }
}