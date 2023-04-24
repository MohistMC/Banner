package com.mohistmc.banner.mixin.world.entity;

import com.mohistmc.banner.injection.world.entity.InjectionLightningBolt;
import net.minecraft.world.entity.LightningBolt;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(LightningBolt.class)
public class MixinLightningBolt implements InjectionLightningBolt {

    public boolean isSilent = false; // Spigot

    @Override
    public boolean bridge$isSilent() {
        return isSilent;
    }

    @Override
    public void banner$setIsSilent(boolean isSilent) {
        this.isSilent = isSilent;
    }
}
