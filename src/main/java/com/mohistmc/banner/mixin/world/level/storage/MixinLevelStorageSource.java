package com.mohistmc.banner.mixin.world.level.storage;

import com.mohistmc.banner.injection.world.level.storage.InjectionLevelStorageSource;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(LevelStorageSource.class)
public class MixinLevelStorageSource implements InjectionLevelStorageSource {
}
