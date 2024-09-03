package com.mohistmc.banner.mixin.world.level.levelgen;

import com.mohistmc.banner.asm.annotation.CreateConstructor;
import com.mohistmc.banner.asm.annotation.ShadowConstructor;
import com.mohistmc.banner.injection.world.level.levelgen.InjectionFlatLevelSource;
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

    @ShadowConstructor
    public void banner$constructor$super(FlatLevelGeneratorSettings flatLevelGeneratorSettings) {
        throw new RuntimeException();
    }

    @CreateConstructor
    public void banner$constructor(FlatLevelGeneratorSettings settings, BiomeSource biomeSource) {
        banner$constructor$super(settings);
        banner$biomeSource = banner$biomeSource == null ? new FixedBiomeSource(settings.getBiome()) : banner$biomeSource;
        this.settings = settings;
    }

    @Override
    public void banner$setBiomeSource(BiomeSource biomeSource) {
        this.banner$biomeSource = biomeSource;
    }
}
