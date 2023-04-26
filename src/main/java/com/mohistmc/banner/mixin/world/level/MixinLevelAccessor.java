package com.mohistmc.banner.mixin.world.level;

import com.mohistmc.banner.injection.world.level.InjectionLevelAccessor;
import net.minecraft.world.level.LevelAccessor;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(LevelAccessor.class)
public interface MixinLevelAccessor extends InjectionLevelAccessor {
}
