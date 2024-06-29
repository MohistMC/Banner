package com.mohistmc.banner.injection.world.level.levelgen.structure;

import org.bukkit.craftbukkit.v1_20_R1.persistence.DirtyCraftPersistentDataContainer;
import org.bukkit.event.world.AsyncStructureGenerateEvent;

public interface InjectionStructureStart {

    default org.bukkit.event.world.AsyncStructureGenerateEvent.Cause bridge$generationEventCause() {
        throw new RuntimeException("Not implemented!");
    }

    default org.bukkit.craftbukkit.v1_20_R1.persistence.DirtyCraftPersistentDataContainer bridge$persistentDataContainer() {
        throw new RuntimeException("Not implemented!");
    }

    default void banner$setGenerationEventCause(AsyncStructureGenerateEvent.Cause generationEventCause) {
        throw new RuntimeException("Not implemented!");
    }

    default void banner$setPersistentDataContainer(DirtyCraftPersistentDataContainer persistentDataContainer) {
        throw new RuntimeException("Not implemented!");
    }
}
