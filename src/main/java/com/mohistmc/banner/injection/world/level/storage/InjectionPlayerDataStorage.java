package com.mohistmc.banner.injection.world.level.storage;

import net.minecraft.nbt.CompoundTag;

import java.io.File;

public interface InjectionPlayerDataStorage {

    default CompoundTag getPlayerData(String s) {
        return null;
    }

    default File getPlayerDir() {
        return null;
    }
}
