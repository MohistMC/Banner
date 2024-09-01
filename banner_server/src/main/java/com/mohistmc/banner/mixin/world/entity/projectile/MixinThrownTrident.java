package com.mohistmc.banner.mixin.world.entity.projectile;

import net.minecraft.world.entity.projectile.ThrownTrident;
import org.spongepowered.asm.mixin.Mixin;

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
