package com.mohistmc.banner.mixin.world.level.chunk.storage;

import com.mohistmc.banner.injection.world.level.chunk.InjectionRegionFileStorage;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.storage.RegionFile;
import net.minecraft.world.level.chunk.storage.RegionFileStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.io.IOException;

@Mixin(RegionFileStorage.class)
public abstract class MixinRegionFileStorage implements InjectionRegionFileStorage {

    // @formatter:off
    @Shadow protected abstract RegionFile getRegionFile(ChunkPos pos) throws IOException;
    // @formatter:on

    @Override
    public boolean chunkExists(ChunkPos pos) throws IOException {
        RegionFile regionFile = getRegionFile(pos);
        return regionFile != null && regionFile.hasChunk(pos);
    }

}
