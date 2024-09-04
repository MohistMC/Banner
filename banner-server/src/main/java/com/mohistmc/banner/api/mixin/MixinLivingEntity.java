package com.mohistmc.banner.api.mixin;

import com.mohistmc.banner.api.event.living.LivingHealEvent;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class MixinLivingEntity {

    @Inject(method = "heal",at=@At("HEAD"),cancellable = true)
    private void callLivingHealEvent(float amount, CallbackInfo ci) {
        LivingHealEvent bannerEvent = new LivingHealEvent((org.bukkit.entity.LivingEntity) ((LivingEntity) (Object) this).getBukkitEntity(), amount);

        amount = bannerEvent.getAmount();
        if (amount <= 0 || bannerEvent.isCancelled()) {
            ci.cancel();
        }
    }
}
