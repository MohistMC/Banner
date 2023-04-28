package com.mohistmc.banner.mixin.world.level.storage;

import com.mohistmc.banner.injection.world.level.storage.InjectionLevelStorageAccess;
import com.mohistmc.banner.injection.world.level.storage.InjectionLevelStorageSource;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.io.IOException;

@Mixin(LevelStorageSource.class)
public abstract class MixinLevelStorageSource implements InjectionLevelStorageSource {

    @Shadow public abstract LevelStorageSource.LevelStorageAccess createAccess(String saveName) throws IOException;

    @Override
    public LevelStorageSource.LevelStorageAccess createAccess(String saveName, ResourceKey<LevelStem> dimensionType) throws IOException {
        LevelStorageSource.LevelStorageAccess save = createAccess(saveName);
        ((InjectionLevelStorageAccess) save).bridge$setDimType(dimensionType);
        return save;
    }
}
