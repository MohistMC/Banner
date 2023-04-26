package com.mohistmc.banner.mixin.world.level.block;

import com.mohistmc.banner.injection.world.level.block.InjectionChestBlock;
import net.minecraft.world.level.block.ChestBlock;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ChestBlock.class)
public class MixinChestBlock implements InjectionChestBlock {
}
