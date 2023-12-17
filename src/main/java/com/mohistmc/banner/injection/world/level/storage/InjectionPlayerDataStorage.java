package com.mohistmc.banner.injection.world.level.storage;

import java.io.File;
import net.minecraft.nbt.CompoundTag;

public interface InjectionPlayerDataStorage {

    default CompoundTag getPlayerData(String s) {
        return null;
    }

    default File getPlayerDir() {
        return null;
    }
}
