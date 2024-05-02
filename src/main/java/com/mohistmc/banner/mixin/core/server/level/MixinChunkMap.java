package com.mohistmc.banner.mixin.core.server.level;

import com.mohistmc.banner.bukkit.BukkitCallbackExecutor;
import com.mohistmc.banner.injection.server.level.InjectionChunkMap;
import com.mojang.datafixers.DataFixer;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.function.Supplier;
import java.util.stream.Stream;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.util.thread.BlockableEventLoop;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.LightChunkGetter;
import net.minecraft.world.level.chunk.storage.ChunkStorage;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.entity.ChunkStatusUpdateListener;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.bukkit.craftbukkit.generator.CustomChunkGenerator;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChunkMap.class)
public abstract class MixinChunkMap extends ChunkStorage implements InjectionChunkMap {

    // @formatter:off
    @Shadow protected abstract void tick();
    @Shadow @Mutable public ChunkGenerator generator;
    @Shadow @Final public ServerLevel level;
    @Shadow @Final @Mutable private RandomState randomState;
    // @formatter:on

    public final BukkitCallbackExecutor callbackExecutor = new BukkitCallbackExecutor();

    public MixinChunkMap(Path path, DataFixer dataFixer, boolean bl) {
        super(path, dataFixer, bl);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void banner$updateRandom(ServerLevel serverLevel, LevelStorageSource.LevelStorageAccess levelStorageAccess, DataFixer dataFixer, StructureTemplateManager structureTemplateManager, Executor executor, BlockableEventLoop blockableEventLoop, LightChunkGetter lightChunkGetter, ChunkGenerator chunkGenerator, ChunkProgressListener chunkProgressListener, ChunkStatusUpdateListener chunkStatusUpdateListener, Supplier supplier, int i, boolean bl, CallbackInfo ci) {
        this.setChunkGenerator(this.generator);
    }

    public void setChunkGenerator(ChunkGenerator generator) {
        this.generator = generator;
        if (generator instanceof CustomChunkGenerator custom) {
            generator = custom.getDelegate();
        }
        if (generator instanceof NoiseBasedChunkGenerator noisebasedchunkgenerator) {
            this.randomState = RandomState.create(noisebasedchunkgenerator.generatorSettings().value(), this.level.registryAccess().lookupOrThrow(Registries.NOISE), this.level.getSeed());
        } else {
            this.randomState = RandomState.create(NoiseGeneratorSettings.dummy(), this.level.registryAccess().lookupOrThrow(Registries.NOISE), this.level.getSeed());
        }
    }

    @Redirect(method = "upgradeChunkTag", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;dimension()Lnet/minecraft/resources/ResourceKey;"))
    private ResourceKey<LevelStem> banner$useTypeKey(ServerLevel serverWorld) {
        return serverWorld.getTypeKey();
    }

    @Redirect(method = "postLoadProtoChunk", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/EntityType;loadEntitiesRecursive(Ljava/util/List;Lnet/minecraft/world/level/Level;)Ljava/util/stream/Stream;"))
    private static Stream<Entity> banner$resetChunkMap(List<? extends Tag> tags, Level level) {
        // CraftBukkit start - these are spawned serialized (DefinedStructure) and we don't call an add event below at the moment due to ordering complexities
        return EntityType.loadEntitiesRecursive(tags, level).filter((entity) -> {
            boolean needsRemoval = false;
            net.minecraft.server.dedicated.DedicatedServer server = level.getCraftServer().getServer();
            if (!server.areNpcsEnabled() && entity instanceof net.minecraft.world.entity.npc.Npc) {
                entity.discard();
                needsRemoval = true;
            }
            if (!server.isSpawningAnimals() && (entity instanceof net.minecraft.world.entity.animal.Animal || entity instanceof net.minecraft.world.entity.animal.WaterAnimal)) {
                entity.discard();
                needsRemoval = true;
            }
            return !needsRemoval;
        });
        // CraftBukkit end
    }

    @Override
    public BukkitCallbackExecutor bridge$callbackExecutor() {
        return callbackExecutor;
    }
}
