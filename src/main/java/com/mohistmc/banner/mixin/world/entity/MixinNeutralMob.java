package com.mohistmc.banner.mixin.world.entity;

import com.mohistmc.banner.injection.world.entity.InjectionNeutralMob;
import net.minecraft.world.entity.NeutralMob;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(NeutralMob.class)
public interface MixinNeutralMob extends InjectionNeutralMob {
}
