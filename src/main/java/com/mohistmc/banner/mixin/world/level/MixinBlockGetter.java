package com.mohistmc.banner.mixin.world.level;

import com.mohistmc.banner.injection.world.level.InjectionBlockGetter;
import net.minecraft.world.level.BlockGetter;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(BlockGetter.class)
public interface MixinBlockGetter extends InjectionBlockGetter {
}
