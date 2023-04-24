package com.mohistmc.banner.mixin.world.level;

import com.mohistmc.banner.injection.world.level.InjectionLevelWriter;
import net.minecraft.world.level.LevelWriter;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(LevelWriter.class)
public interface MixinLevelWriter extends InjectionLevelWriter {
}
