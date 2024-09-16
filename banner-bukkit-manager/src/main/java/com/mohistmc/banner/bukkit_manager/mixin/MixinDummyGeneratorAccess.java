package com.mohistmc.banner.bukkit_manager.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.WorldGenLevel;
import org.bukkit.craftbukkit.util.DummyGeneratorAccess;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(DummyGeneratorAccess.class)
public abstract class MixinDummyGeneratorAccess implements WorldGenLevel {

    @Override
    public void pushAddEntityReason(CreatureSpawnEvent.SpawnReason reason) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public CreatureSpawnEvent.SpawnReason getAddEntityReason() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean addFreshEntityWithPassengers(Entity entity, CreatureSpawnEvent.SpawnReason reason) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
