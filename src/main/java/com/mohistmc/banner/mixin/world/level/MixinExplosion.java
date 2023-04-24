package com.mohistmc.banner.mixin.world.level;

import com.mohistmc.banner.injection.world.level.InjectionExplosion;
import net.minecraft.world.level.Explosion;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Explosion.class)
public class MixinExplosion implements InjectionExplosion {

    public boolean wasCanceled = false; // CraftBukkit - add field

    @Override
    public boolean bridge$wasCanceled() {
        return wasCanceled;
    }

    @Override
    public void banner$setWasCanceled(boolean wasCanceled) {
        this.wasCanceled = wasCanceled;
    }
}
