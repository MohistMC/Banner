package com.mohistmc.banner.mixin.server.level;

import com.mohistmc.banner.injection.server.level.InjectionServerLevel;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.PrimaryLevelData;
import org.spongepowered.asm.mixin.Mixin;

import java.util.UUID;

@Mixin(ServerLevel.class)
public class MixinServerLevel implements InjectionServerLevel {

    public LevelStorageSource.LevelStorageAccess convertable;
    public UUID uuid;
    public PrimaryLevelData serverLevelDataCB;

    @Override
    public PrimaryLevelData bridge$serverLevelDataCB() {
        return serverLevelDataCB;
    }

    @Override
    public LevelStorageSource.LevelStorageAccess bridge$convertable() {
        return convertable;
    }

    @Override
    public UUID bridge$uuid() {
        return uuid;
    }
}
