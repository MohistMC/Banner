package com.mohistmc.banner.mixin.world.entity.moster;

import net.minecraft.world.entity.monster.Strider;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Strider.class)
public class MixinStrider {

    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/monster/Strider;setSuffocating(Z)V"))
    private void banner$temperatureChange(Strider instance, boolean flag) {
        if (flag ^ instance.isSuffocating()) {
            if (CraftEventFactory.callStriderTemperatureChangeEvent(instance, flag)) {
                instance.setSuffocating(flag);
            }
        }
    }
}
