package com.mohistmc.banner.mixin.world.level.levelgen;

import com.mohistmc.banner.injection.world.level.levelgen.InjectionFlatLevelSource;
import java.util.function.Function;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.FixedBiomeSource;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(FlatLevelSource.class)
public abstract class MixinFlatLevelSource implements InjectionFlatLevelSource {

    @Mutable
    @Shadow @Final private FlatLevelGeneratorSettings settings;

    private BiomeSource banner$biomeSource;

    public void banner$constructor$super(BiomeSource biomeSource,
                                         Function<Holder<Biome>, BiomeGenerationSettings> generationSettingsGetter, BiomeSource newBiomeSource) {
        throw new RuntimeException();
    }

    public void banner$constructor(FlatLevelGeneratorSettings settings, BiomeSource biomeSource) {
        banner$constructor$super(biomeSource, Util.memoize(settings::adjustGenerationSettings), biomeSource);
        banner$biomeSource = banner$biomeSource == null ? new FixedBiomeSource(settings.getBiome()) : banner$biomeSource;
        biomeSource = banner$biomeSource;
        this.settings = settings;
    }

    @Override
    public void banner$setBiomeSource(BiomeSource biomeSource) {
        this.banner$biomeSource = biomeSource;
    }
}
