package com.mohistmc.banner.mixin.world.level.chunk;

import com.mohistmc.banner.injection.world.level.chunk.InjectionChunkGenerator;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.generator.CraftLimitedRegion;
import org.bukkit.craftbukkit.generator.structure.CraftStructure;
import org.bukkit.craftbukkit.util.RandomSourceWrapper;
import org.bukkit.generator.BlockPopulator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;
import java.util.function.Predicate;

@Mixin(ChunkGenerator.class)
public abstract class MixinChunkGenerator implements InjectionChunkGenerator {

    // @formatter:off
    @Shadow public abstract void applyBiomeDecoration(WorldGenLevel p_187712_, ChunkAccess p_187713_, StructureManager p_187714_);
    @Shadow
    @Mutable
    public BiomeSource biomeSource;
    // @formatter:on

    @Shadow
    private static int fetchReferences(StructureManager structureManager, ChunkAccess chunk, SectionPos sectionPos, Structure structure) {return 0;}

    @Inject(method = "applyBiomeDecoration", at = @At("RETURN"))
    private void banner$addBukkitDecoration(WorldGenLevel level, ChunkAccess chunkAccess, StructureManager manager, CallbackInfo ci) {
        this.addDecorations(level, chunkAccess, manager);
    }

    // Banner start - fix mixin conflict
    BoundingBox box;
    org.bukkit.event.world.AsyncStructureSpawnEvent event;
    // Banner end

    /**
     * @author wdog5
     * @reason bukkit
     */
    @Overwrite
    private boolean tryGenerateStructure(StructureSet.StructureSelectionEntry structureSelectionEntry, StructureManager structureManager, RegistryAccess registryAccess, RandomState random, StructureTemplateManager structureTemplateManager, long seed, ChunkAccess chunk, ChunkPos chunkPos, SectionPos sectionPos) {
        Structure structure = (Structure)structureSelectionEntry.structure().value();
        int i = fetchReferences(structureManager, chunk, sectionPos, structure);
        HolderSet<Biome> holderSet = structure.biomes();
        Objects.requireNonNull(holderSet);
        Predicate<Holder<Biome>> predicate = holderSet::contains;
        StructureStart structureStart = structure.generate(registryAccess, ((ChunkGenerator) (Object) this), this.biomeSource, random, structureTemplateManager, seed, chunkPos, i, chunk, predicate);
        if (structureStart.isValid()) {
            structureManager.setStartForStructure(sectionPos, structure, structureStart, chunk);
            box = structureStart.getBoundingBox();
            event = new org.bukkit.event.world.AsyncStructureSpawnEvent((((LevelAccessor) structureManager.level).getMinecraftWorld()).getWorld(), CraftStructure.minecraftToBukkit(structure), new org.bukkit.util.BoundingBox(box.minX(), box.minY(), box.minZ(), box.maxX(), box.maxY(), box.maxZ()), chunkPos.x, chunkPos.z);
            Bukkit.getPluginManager().callEvent(event);
            if (event.isCancelled()) {
                return true;
            }
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void applyBiomeDecoration(WorldGenLevel level, ChunkAccess chunkAccess, StructureManager structureFeatureManager, boolean vanilla) {
        if (vanilla) {
            this.applyBiomeDecoration(level, chunkAccess, structureFeatureManager);
        } else {
            this.addDecorations(level, chunkAccess, structureFeatureManager);
        }
    }

    @Override
    public void addDecorations(WorldGenLevel region, ChunkAccess chunk, StructureManager structureManager) {
        org.bukkit.World world =  ((LevelAccessor) region).getMinecraftWorld().getWorld();
        // only call when a populator is present (prevents unnecessary entity conversion)
        if (!world.getPopulators().isEmpty()) {
            CraftLimitedRegion limitedRegion = new CraftLimitedRegion(region, chunk.getPos());
            int x = chunk.getPos().x;
            int z = chunk.getPos().z;
            for (BlockPopulator populator : world.getPopulators()) {
                WorldgenRandom random = new WorldgenRandom(new LegacyRandomSource(region.getSeed()));
                random.setDecorationSeed(region.getSeed(), x, z);
                populator.populate(world, new RandomSourceWrapper.RandomWrapper(random), x, z, limitedRegion);
            }
            limitedRegion.saveEntities();
            limitedRegion.breakLink();
        }
    }
}
