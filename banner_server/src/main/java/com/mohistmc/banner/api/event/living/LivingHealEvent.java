package com.mohistmc.banner.api.event.living;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Cancellable;

public class LivingHealEvent  extends LivingEvent implements Cancellable {

    private float amount;
    private boolean cancelled = false;

    public LivingHealEvent(LivingEntity livingEntity, float amount) {
        super(livingEntity);
        this.setAmount(amount);
    }

    public float getAmount() {
        return amount;
    }

    public void setAmount(float amount) {
        this.amount = amount;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }
}
