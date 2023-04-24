package com.mohistmc.banner.mixin.server.level;

import com.mohistmc.banner.injection.server.level.InjectionDistanceManager;
import net.minecraft.server.level.DistanceManager;
import org.spongepowered.asm.mixin.Mixin;

// TODO fix inject method
@Mixin(DistanceManager.class)
public class MixinDistanceManager implements InjectionDistanceManager {
}
