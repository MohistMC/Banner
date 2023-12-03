package com.mohistmc.banner.mixin.world.level.chunk;

import com.mohistmc.banner.injection.world.level.chunk.InjectionLevelChunkSection;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.PalettedContainer;
import net.minecraft.world.level.chunk.PalettedContainerRO;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(LevelChunkSection.class)
public abstract class MixinLevelChunkSection implements InjectionLevelChunkSection {

    @Shadow private PalettedContainerRO<Holder<Biome>> biomes;
    @Shadow public PalettedContainer<BlockState> states;
    @Shadow public abstract void recalcBlockCounts();

    public void banner$constructor(PalettedContainer<BlockState> pStates, PalettedContainerRO<Holder<Biome>> pBiomes) {
        throw new RuntimeException();
    }

    public void LevelChunkSection(Registry<Biome> biomeRegistry) {
        throw new RuntimeException();
    }

    public void LevelChunkSection(PalettedContainer<BlockState> pStates, PalettedContainer<Holder<Biome>> pBiomes) {
        banner$constructor(pStates, pBiomes);
    }

    @Override
    public void setBiome(int i, int j, int k, Holder<Biome> biome) {
        ((PalettedContainer<Holder<Biome>>) this.biomes).set(i, j, k, biome);
    }
}
