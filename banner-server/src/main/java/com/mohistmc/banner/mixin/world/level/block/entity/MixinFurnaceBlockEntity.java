package com.mohistmc.banner.mixin.world.level.block.entity;

import com.mohistmc.banner.injection.world.level.block.entity.InjectionFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.FurnaceBlockEntity;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(FurnaceBlockEntity.class)
public class MixinFurnaceBlockEntity implements InjectionFurnaceBlockEntity {
}
