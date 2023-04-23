package com.mohistmc.banner.mixin.world.level.levelgen.structure.templatesystem;

import com.mohistmc.banner.injection.world.level.levelgen.structure.templatesystem.InjectionStructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.bukkit.craftbukkit.v1_19_R3.persistence.CraftPersistentDataContainer;
import org.bukkit.craftbukkit.v1_19_R3.persistence.CraftPersistentDataTypeRegistry;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(StructureTemplate.class)
public class MixinStructureTemplate implements InjectionStructureTemplate {

    private static final CraftPersistentDataTypeRegistry DATA_TYPE_REGISTRY = new CraftPersistentDataTypeRegistry();
    public CraftPersistentDataContainer persistentDataContainer = new CraftPersistentDataContainer(DATA_TYPE_REGISTRY);

    @Override
    public CraftPersistentDataContainer bridge$persistentDataContainer() {
        return persistentDataContainer;
    }
}
