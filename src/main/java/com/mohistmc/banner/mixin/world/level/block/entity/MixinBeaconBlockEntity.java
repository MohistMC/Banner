package com.mohistmc.banner.mixin.world.level.block.entity;

import com.mohistmc.banner.injection.world.level.block.entity.InjectionBeaconBlockEntity;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(BeaconBlockEntity.class)
public class MixinBeaconBlockEntity implements InjectionBeaconBlockEntity {
}
