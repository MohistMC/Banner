package com.mohistmc.banner.mixin.world.level.block.entity;

import com.mohistmc.banner.injection.world.level.block.entity.InjectionBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.bukkit.craftbukkit.v1_19_R3.persistence.CraftPersistentDataContainer;
import org.bukkit.craftbukkit.v1_19_R3.persistence.CraftPersistentDataTypeRegistry;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(BlockEntity.class)
public class MixinBlockEntity implements InjectionBlockEntity {

    private static final CraftPersistentDataTypeRegistry DATA_TYPE_REGISTRY = new CraftPersistentDataTypeRegistry();
    public CraftPersistentDataContainer persistentDataContainer;

    @Override
    public CraftPersistentDataContainer bridge$persistentDataContainer() {
        return persistentDataContainer;
    }

    @Override
    public void banner$setPersistentDataContainer(CraftPersistentDataContainer persistentDataContainer) {
        this.persistentDataContainer = persistentDataContainer;
    }
}
