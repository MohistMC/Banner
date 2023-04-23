package com.mohistmc.banner.injection.world.level.levelgen.structure.templatesystem;

import org.bukkit.craftbukkit.v1_19_R3.persistence.CraftPersistentDataContainer;

public interface InjectionStructureTemplate {

    default CraftPersistentDataContainer bridge$persistentDataContainer() {
        return null;
    }
}
