package com.mohistmc.banner.injection.world.level.storage;

import java.io.File;
import java.util.Optional;
import net.minecraft.nbt.CompoundTag;

public interface InjectionPlayerDataStorage {

    default CompoundTag getPlayerData(String s) {
        throw new IllegalStateException("Not implemented");
    }

    default File getPlayerDir() {
        throw new IllegalStateException("Not implemented");
    }

    default Optional<CompoundTag> load(String name, String s1, String s) { // name, uuid, extension
        return Optional.empty();
    }

    default Optional<CompoundTag> load(String name, String uuid) {
        return Optional.empty();
    }

    default void backup(String name, String s1, String s) {
        throw new IllegalStateException("Not implemented");
    }
}
