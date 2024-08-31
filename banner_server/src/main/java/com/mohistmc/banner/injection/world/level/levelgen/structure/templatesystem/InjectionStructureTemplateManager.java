package com.mohistmc.banner.injection.world.level.levelgen.structure.templatesystem;

import java.util.Optional;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

public interface InjectionStructureTemplateManager {

    default Optional<StructureTemplate> loadFromResource0(ResourceLocation id) {
        return Optional.empty();
    }
}
