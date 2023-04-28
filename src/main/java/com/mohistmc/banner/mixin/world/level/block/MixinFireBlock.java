package com.mohistmc.banner.mixin.world.level.block;

import com.mohistmc.banner.injection.world.level.block.InjectionFireBlock;
import net.minecraft.world.level.block.FireBlock;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(FireBlock.class)
public class MixinFireBlock implements InjectionFireBlock {
}
