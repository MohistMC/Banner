package com.mohistmc.banner.mixin.world.entity.raid;

import net.minecraft.world.entity.raid.Raider;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Raider.class)
public class MixinRaider {

    // Banner TODO fixme
    /*
    @Inject(method = "die", locals = LocalCapture.CAPTURE_FAILHARD, require = 0, at = @At(value = "INVOKE", target = "addEffect"))
    private void banner$raid(DamageSource cause, CallbackInfo ci, Entity entity, Raid raid, ItemStack itemStack, Player playerEntity) {
        ((Player) playerEntity).pushEffectCause(EntityPotionEffectEvent.Cause.PATROL_CAPTAIN);
    }*/
}
