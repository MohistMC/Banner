package com.mohistmc.banner.mixin.world.level.storage;

import com.mohistmc.banner.injection.world.level.storage.InjectionPlayerDataStorage;
import net.minecraft.world.level.storage.PlayerDataStorage;
import org.spongepowered.asm.mixin.Mixin;

// TODO fix inject method
@Mixin(PlayerDataStorage.class)
public class MixinPlayerDataStorage implements InjectionPlayerDataStorage {
}
