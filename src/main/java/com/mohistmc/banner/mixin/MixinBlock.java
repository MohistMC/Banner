package com.mohistmc.banner.mixin;

import com.mohistmc.banner.injection.InjectionBlock;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Block.class)
public class MixinBlock implements InjectionBlock {
}
