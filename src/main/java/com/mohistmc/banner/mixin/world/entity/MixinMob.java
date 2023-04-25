package com.mohistmc.banner.mixin.world.entity;

import com.mohistmc.banner.injection.world.entity.InjectionMob;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Mob.class)
public abstract class MixinMob implements InjectionMob {

    @Shadow private boolean persistenceRequired;
    public boolean aware = true; // CraftBukkit

    @Override
    public void setPersistenceRequired(boolean persistenceRequired) {
        this.persistenceRequired = persistenceRequired;
    }

    @Override
    public boolean bridge$aware() {
        return aware;
    }

    @Override
    public void banner$setAware(boolean aware) {
        this.aware = aware;
    }
}
