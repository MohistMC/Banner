package com.mohistmc.banner.injection.world.level.levelgen.structure.templatesystem;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import java.util.Optional;

public interface InjectionStructureTemplateManager {

    default Optional<StructureTemplate> loadFromResource0(ResourceLocation id) {
        return Optional.empty();
    }
}
