package com.mohistmc.banner.mixin.core.world.entity.projectile;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.level.Level;
import org.bukkit.event.weather.LightningStrikeEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ThrownTrident.class)
public class MixinThrownTrident {

    // Banner TODO fixme
    /*
    @Redirect(method = "onHitEntity", at = @At(value = "INVOKE", target = "addFreshEntity"))
    private boolean banner$lightning(Level world, Entity entityIn) {
        ((ServerLevel) world).strikeLightning((LightningBolt) entityIn, LightningStrikeEvent.Cause.TRIDENT);
        return true;
    }*/

}
