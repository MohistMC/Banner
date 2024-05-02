package com.mohistmc.banner.injection.world.level.chunk;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.world.level.biome.Biome;
import org.bukkit.craftbukkit.persistence.DirtyCraftPersistentDataContainer;

public interface InjectionChunkAccess {

    default DirtyCraftPersistentDataContainer bridge$persistentDataContainer() {
        return null;
    }

    default void setBiome(int i, int j, int k, Holder<Biome> biome) {

    }

    default Registry<Biome> bridge$biomeRegistry() {
        return null;
    }
}
