package com.mohistmc.banner.bukkit_manager.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelAccessor;
import org.bukkit.craftbukkit.util.BlockStateListPopulator;
import org.bukkit.craftbukkit.util.DummyGeneratorAccess;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(BlockStateListPopulator.class)
public abstract class MixinBlockStateListPopulator extends DummyGeneratorAccess {
    @Shadow
    @Final
    private LevelAccessor world;

    @Override
    public CreatureSpawnEvent.SpawnReason getAddEntityReason() {
       return world.getAddEntityReason();
    }

    @Override
    public boolean addFreshEntityWithPassengers(Entity entity, CreatureSpawnEvent.SpawnReason reason) {
        return world.addFreshEntity(entity, reason);
    }

    @Override
    public void pushAddEntityReason(CreatureSpawnEvent.SpawnReason reason) {
        world.pushAddEntityReason(reason);
    }
}
