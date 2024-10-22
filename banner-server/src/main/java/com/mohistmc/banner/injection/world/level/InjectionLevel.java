package com.mohistmc.banner.injection.world.level;

import com.mohistmc.banner.config.BannerWorldConfig;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.dimension.LevelStem;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.block.CapturedBlockState;
import org.bukkit.entity.SpawnCategory;
import org.spigotmc.SpigotWorldConfig;

public interface InjectionLevel {

    default boolean bridge$preventPoiUpdated() {
        throw new IllegalStateException("Not implemented");
    }

    default void banner$setPreventPoiUpdated(boolean preventPoiUpdated) {
        throw new IllegalStateException("Not implemented");
    }

    default org.bukkit.generator.BiomeProvider bridge$biomeProvider() {
        throw new IllegalStateException("Not implemented");
    }

    default void banner$setBiomeProvider(org.bukkit.generator.BiomeProvider biomeProvider) {
        throw new IllegalStateException("Not implemented");
    }

    default org.bukkit.World.Environment bridge$environment() {
        throw new IllegalStateException("Not implemented");
    }

    default void banner$setEnvironment(org.bukkit.World.Environment environment) {
        throw new IllegalStateException("Not implemented");
    }

    default org.bukkit.generator.ChunkGenerator bridge$generator() {
        throw new IllegalStateException("Not implemented");
    }

    default void banner$setGenerator(org.bukkit.generator.ChunkGenerator generator) {
        throw new IllegalStateException("Not implemented");
    }

    default boolean bridge$pvpMode() {
        throw new IllegalStateException("Not implemented");
    }

    default void banner$setPvpMode(boolean pvpMode) {
        throw new IllegalStateException("Not implemented");
    }

    default boolean bridge$captureBlockStates() {
        throw new IllegalStateException("Not implemented");
    }

    default void banner$setCaptureBlockStates(boolean captureState) {
        throw new IllegalStateException("Not implemented");
    }

    default boolean bridge$captureTreeGeneration() {
        throw new IllegalStateException("Not implemented");
    }

    default void banner$setCaptureTreeGeneration(boolean treeGeneration) {
        throw new IllegalStateException("Not implemented");
    }

    default Map<BlockPos, CapturedBlockState> bridge$capturedBlockStates() {
        throw new IllegalStateException("Not implemented");
    }

    default Map<BlockPos, BlockEntity> bridge$capturedTileEntities() {
        throw new IllegalStateException("Not implemented");
    }

    default void banner$setCapturedTileEntities(Map<BlockPos, BlockEntity> tileEntities) {
        throw new IllegalStateException("Not implemented");
    }

    default void banner$setCapturedBlockStates(Map<BlockPos, CapturedBlockState> capturedBlockStates) {
        throw new IllegalStateException("Not implemented");
    }

    default List<ItemEntity> bridge$captureDrops() {
        throw new IllegalStateException("Not implemented");
    }

    default void banner$setCaptureDrops(List<ItemEntity> captureDrops) {
        throw new IllegalStateException("Not implemented");
    }

    default it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap<SpawnCategory> bridge$ticksPerSpawnCategory() {
        throw new IllegalStateException("Not implemented");
    }

    default boolean bridge$populating() {
        throw new IllegalStateException("Not implemented");
    }

    default void banner$setPopulating(boolean populating) {
        throw new IllegalStateException("Not implemented");
    }

    default boolean bridge$keepSpawnInMemory() {
        throw new IllegalStateException("Not implemented");
    }

    default void banner$setKeepSpawnInMemory(boolean keepSpawnInMemory) {
        throw new IllegalStateException("Not implemented");
    }

    default ResourceKey<LevelStem> getTypeKey(){
        throw new IllegalStateException("Not implemented");
    }

    default CraftWorld getWorld() {
        throw new IllegalStateException("Not implemented");
    }

    default CraftServer getCraftServer() {
        throw new IllegalStateException("Not implemented");
    }

    default void notifyAndUpdatePhysics(BlockPos blockposition, LevelChunk chunk, BlockState oldBlock, BlockState newBlock, BlockState actualBlock, int i, int j) {
        throw new IllegalStateException("Not implemented");
    }

    default BlockEntity getBlockEntity(BlockPos blockposition, boolean validate) {
        throw new IllegalStateException("Not implemented");
    }

    default SpigotWorldConfig bridge$spigotConfig() {
        throw new IllegalStateException("Not implemented");
    }

    default void banner$setSpigotConfig(SpigotWorldConfig spigotWorldConfig) {
        throw new IllegalStateException("Not implemented");
    }

    default BannerWorldConfig bridge$bannerConfig() {
        throw new IllegalStateException("Not implemented");
    }

    default void banner$setBannerConfig(BannerWorldConfig bannerWorldConfig) {
        throw new IllegalStateException("Not implemented");
    }

    default CraftWorld banner$initWorld(LevelStem levelStem) {
        throw new IllegalStateException("Not implemented");
    }
}
