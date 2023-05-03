package com.mohistmc.banner.bukkit;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import org.bukkit.event.entity.EntityPotionEffectEvent;

public class ProcessableEffect {

    private MobEffect type;
    private MobEffectInstance effect;
    private final EntityPotionEffectEvent.Cause cause;

    public ProcessableEffect(MobEffectInstance effect, EntityPotionEffectEvent.Cause cause) {
        this.effect = effect;
        this.cause = cause;
    }

    public ProcessableEffect(MobEffect type, EntityPotionEffectEvent.Cause cause) {
        this.type = type;
        this.cause = cause;
    }

    public MobEffect getType() {
        return type;
    }

    public void setType(MobEffect type) {
        this.type = type;
    }

    public MobEffectInstance getEffect() {
        return effect;
    }

    public EntityPotionEffectEvent.Cause getCause() {
        return cause;
    }

    public void setEffect(MobEffectInstance effect) {
        this.effect = effect;
    }
}
