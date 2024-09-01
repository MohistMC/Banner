package com.mohistmc.banner.mixin.world.level.levelgen.structure;

import com.mohistmc.banner.injection.world.level.levelgen.structure.InjectionStructureStart;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import org.bukkit.craftbukkit.persistence.DirtyCraftPersistentDataContainer;
import org.bukkit.event.world.AsyncStructureGenerateEvent;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(StructureStart.class)
public abstract class MixinStructureStart implements InjectionStructureStart {

    // CraftBukkit start
    private static final org.bukkit.craftbukkit.persistence.CraftPersistentDataTypeRegistry DATA_TYPE_REGISTRY = new org.bukkit.craftbukkit.persistence.CraftPersistentDataTypeRegistry();
    public org.bukkit.craftbukkit.persistence.DirtyCraftPersistentDataContainer persistentDataContainer = new org.bukkit.craftbukkit.persistence.DirtyCraftPersistentDataContainer(DATA_TYPE_REGISTRY);
    public org.bukkit.event.world.AsyncStructureGenerateEvent.Cause generationEventCause = org.bukkit.event.world.AsyncStructureGenerateEvent.Cause.WORLD_GENERATION;
    // CraftBukkit end

    @Override
    public org.bukkit.event.world.AsyncStructureGenerateEvent.Cause bridge$generationEventCause() {
        return generationEventCause;
    }

    @Override
    public org.bukkit.craftbukkit.persistence.DirtyCraftPersistentDataContainer bridge$persistentDataContainer() {
        return persistentDataContainer;
    }

    @Override
    public void banner$setGenerationEventCause(AsyncStructureGenerateEvent.Cause generationEventCause) {
        this.generationEventCause = generationEventCause;
    }

    @Override
    public void banner$setPersistentDataContainer(DirtyCraftPersistentDataContainer persistentDataContainer) {
        this.persistentDataContainer = persistentDataContainer;
    }
}
