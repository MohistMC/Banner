package com.mohistmc.banner.mixin.world.level.entity;

import com.mohistmc.banner.injection.world.level.entity.InjectionPersistentEntitySectionManager;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.entity.EntityAccess;
import net.minecraft.world.level.entity.EntitySection;
import net.minecraft.world.level.entity.EntitySectionStorage;
import net.minecraft.world.level.entity.PersistentEntitySectionManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Mixin(PersistentEntitySectionManager.class)
public abstract class MixinPersistentEntitySectionManager<T extends EntityAccess> implements AutoCloseable,InjectionPersistentEntitySectionManager {

    @Shadow @Final
    EntitySectionStorage<T> sectionStorage;

    @Shadow @Final private Long2ObjectMap<PersistentEntitySectionManager.ChunkLoadStatus> chunkLoadStatuses;

    @Override
    public List<Entity> getEntities(ChunkPos chunkCoordIntPair) {
        return sectionStorage.getExistingSectionsInChunk(chunkCoordIntPair.toLong()).flatMap(EntitySection::getEntities).map(entity -> (Entity) entity).collect(Collectors.toList());
    }

    @Override
    public boolean isPending(long pair) {
        return chunkLoadStatuses.get(pair) == PersistentEntitySectionManager.ChunkLoadStatus.PENDING;
    }

    @Override
    public boolean storeChunkSections(long i, Consumer consumer, boolean callEvent) {
        // CraftBukkit start - add boolean for event call
        return storeChunkSections(i, consumer, false);
    }
}
