package com.mohistmc.banner.mixin.world.entity.ambient;

import net.minecraft.world.entity.ambient.Bat;
import org.bukkit.craftbukkit.v1_19_R3.event.CraftEventFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Bat.class)
public abstract class MixinBat {

    @Shadow public abstract boolean isResting();

    @Inject(method = "customServerAiStep", cancellable = true, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/ambient/Bat;setResting(Z)V"))
    private void banner$toggleSleep(CallbackInfo ci) {
        if (!CraftEventFactory.handleBatToggleSleepEvent((Bat) (Object) this, !this.isResting())) {
            ci.cancel();
        }
    }
}
