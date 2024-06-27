package com.mohistmc.banner.mixin.core.world.level.chunk;

import com.mohistmc.banner.injection.world.level.chunk.InjectionChunkGenerator;
import java.util.function.Predicate;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
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
import org.bukkit.craftbukkit.generator.CraftLimitedRegion;
import org.bukkit.craftbukkit.util.RandomSourceWrapper;
import org.bukkit.generator.BlockPopulator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

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

    @Inject(method = "tryGenerateStructure", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/StructureManager;setStartForStructure(Lnet/minecraft/core/SectionPos;Lnet/minecraft/world/level/levelgen/structure/Structure;Lnet/minecraft/world/level/levelgen/structure/StructureStart;Lnet/minecraft/world/level/chunk/StructureAccess;)V"), locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
    private void banner$fireEvent(StructureSet.StructureSelectionEntry structureSelectionEntry, StructureManager structureManager, RegistryAccess registryAccess, RandomState randomState, StructureTemplateManager structureTemplateManager, long l, ChunkAccess chunkAccess, ChunkPos chunkPos, SectionPos sectionPos0, CallbackInfoReturnable<Boolean> cir, SectionPos sectionPos1, Structure structure, int i, HolderSet holderSet, Predicate predicate, StructureStart structureStart) {
        // CraftBukkit start
        BoundingBox box = structureStart.getBoundingBox();
        org.bukkit.event.world.AsyncStructureSpawnEvent event = new org.bukkit.event.world.AsyncStructureSpawnEvent(structureManager.level.getMinecraftWorld().getWorld(), org.bukkit.craftbukkit.generator.structure.CraftStructure.minecraftToBukkit(structure), new org.bukkit.util.BoundingBox(box.minX(), box.minY(), box.minZ(), box.maxX(), box.maxY(), box.maxZ()), chunkPos.x, chunkPos.z);
        org.bukkit.Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            cir.setReturnValue(true);
        }
        // CraftBukkit end
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
