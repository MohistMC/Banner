package com.mohistmc.banner.mixin.world.entity.animal.horse;

import net.minecraft.world.entity.animal.horse.Llama;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Llama.class)
public abstract class MixinLlama {

    @Shadow public abstract void setStrength(int strength);

    public void setStrengthPublic(int i) {
        this.setStrength(i);
    }
}
