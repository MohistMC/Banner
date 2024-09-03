package com.mohistmc.banner.mixin.world.entity;

import io.izzel.arclight.mixin.Decorate;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Leashable;
import org.bukkit.Bukkit;
import org.bukkit.event.entity.EntityUnleashEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Leashable.class)
public interface MixinLeashable {

    @Decorate(method = "leashTooFarBehaviour", inject = true, at = @At("HEAD"))
    private void banner$distanceLeash() {
        if (this instanceof Entity entity) {
            Bukkit.getPluginManager().callEvent(new EntityUnleashEvent(entity.getBukkitEntity(), EntityUnleashEvent.UnleashReason.DISTANCE));
        }
    }
}