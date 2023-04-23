package com.mohistmc.banner.mixin.world.level.chunk;

import com.mohistmc.banner.injection.world.level.chunk.InjectionChunkAccess;
import net.minecraft.core.Registry;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkAccess;
import org.bukkit.craftbukkit.v1_19_R3.persistence.DirtyCraftPersistentDataContainer;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ChunkAccess.class)
public class MixinChunkAccess implements InjectionChunkAccess {

    // CraftBukkit start - SPIGOT-6814: move to IChunkAccess to account for 1.17 to 1.18 chunk upgrading.
    private static final org.bukkit.craftbukkit.v1_19_R3.persistence.CraftPersistentDataTypeRegistry DATA_TYPE_REGISTRY
            = new org.bukkit.craftbukkit.v1_19_R3.persistence.CraftPersistentDataTypeRegistry();
    public org.bukkit.craftbukkit.v1_19_R3.persistence.DirtyCraftPersistentDataContainer persistentDataContainer
            = new org.bukkit.craftbukkit.v1_19_R3.persistence.DirtyCraftPersistentDataContainer(DATA_TYPE_REGISTRY);
    // CraftBukkit end
    public Registry<Biome> biomeRegistry;

    @Override
    public DirtyCraftPersistentDataContainer bridge$persistentDataContainer() {
        return persistentDataContainer;
    }

    @Override
    public Registry<Biome> bridge$biomeRegistry() {
        return biomeRegistry;
    }
}
