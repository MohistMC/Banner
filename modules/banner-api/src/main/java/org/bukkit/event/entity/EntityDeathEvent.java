package org.bukkit.event.entity;

import java.util.List;
import org.bukkit.damage.DamageSource;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Thrown whenever a LivingEntity dies
 */
public class EntityDeathEvent extends EntityEvent {
    private static final HandlerList handlers = new HandlerList();
    private final DamageSource damageSource;
    private final List<ItemStack> drops;
    private int dropExp = 0;

    public EntityDeathEvent(@NotNull final LivingEntity entity, @NotNull DamageSource damageSource, @NotNull final List<ItemStack> drops) {
        this(entity, damageSource, drops, 0);
    }

    public EntityDeathEvent(@NotNull final LivingEntity what, @NotNull DamageSource damageSource, @NotNull final List<ItemStack> drops, final int droppedExp) {
        super(what);
        this.damageSource = damageSource;
        this.drops = drops;
        this.dropExp = droppedExp;
    }

    @NotNull
    @Override
    public LivingEntity getEntity() {
        return (LivingEntity) entity;
    }

    /**
     * Gets the source of damage which caused the death.
     *
     * @return a DamageSource detailing the source of the damage for the death.
     */
    @NotNull
    public DamageSource getDamageSource() {
        return damageSource;
    }

    /**
     * Gets how much EXP should be dropped from this death.
     * <p>
     * This does not indicate how much EXP should be taken from the entity in
     * question, merely how much should be created after its death.
     *
     * @return Amount of EXP to drop.
     */
    public int getDroppedExp() {
        return dropExp;
    }

    /**
     * Sets how much EXP should be dropped from this death.
     * <p>
     * This does not indicate how much EXP should be taken from the entity in
     * question, merely how much should be created after its death.
     *
     * @param exp Amount of EXP to drop.
     */
    public void setDroppedExp(int exp) {
        this.dropExp = exp;
    }

    /**
     * Gets all the items which will drop when the entity dies
     *
     * @return Items to drop when the entity dies
     */
    @NotNull
    public List<ItemStack> getDrops() {
        return drops;
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
