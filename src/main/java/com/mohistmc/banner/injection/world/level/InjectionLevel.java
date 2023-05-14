package com.mohistmc.banner.injection.world.level;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.dimension.LevelStem;
import org.bukkit.craftbukkit.v1_19_R3.CraftServer;
import org.bukkit.craftbukkit.v1_19_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_19_R3.block.CapturedBlockState;
import org.bukkit.entity.SpawnCategory;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.spigotmc.SpigotWorldConfig;

import java.util.List;
import java.util.Map;

public interface InjectionLevel {

    default boolean bridge$preventPoiUpdated() {
        return false;
    }

    default void banner$setPreventPoiUpdated(boolean preventPoiUpdated) {
    }

    default org.bukkit.generator.BiomeProvider bridge$biomeProvider() {
        return null;
    }

    default void banner$setBiomeProvider(org.bukkit.generator.BiomeProvider biomeProvider) {
    }

    default org.bukkit.World.Environment bridge$environment() {
        return null;
    }

    default void banner$setEnvironment(org.bukkit.World.Environment environment) {
    }

    default org.bukkit.generator.ChunkGenerator bridge$generator() {
        return null;
    }

    default void banner$setGenerator(org.bukkit.generator.ChunkGenerator generator) {
    }

    default boolean bridge$pvpMode() {
        return false;
    }

    default void banner$setPvpMode(boolean pvpMode) {
    }

    default boolean bridge$captureBlockStates() {
        return false;
    }

    default void banner$setCaptureBlockStates(boolean captureState) {
    }

    default boolean bridge$captureTreeGeneration() {
        return false;
    }

    default void banner$setCaptureTreeGeneration(boolean treeGeneration) {
    }

    default Map<BlockPos, CapturedBlockState> bridge$capturedBlockStates() {
        return null;
    }

    default Map<BlockPos, BlockEntity> bridge$capturedTileEntities() {
        return null;
    }

    default void banner$setCapturedTileEntities(Map<BlockPos, BlockEntity> tileEntities) {
    }

    default void banner$setCapturedBlockStates(Map<BlockPos, CapturedBlockState> capturedBlockStates) {
    }

    default List<ItemEntity> bridge$captureDrops() {
        return null;
    }

    default void banner$setCaptureDrops(List<ItemEntity> captureDrops) {

    }

    default it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap<SpawnCategory> bridge$ticksPerSpawnCategory() {
        return null;
    }

    default boolean bridge$populating() {
        return false;
    }

    default void banner$setPopulating(boolean populating) {

    }

    default boolean bridge$KeepSpawnInMemory() {
        return false;
    }

    default void banner$setKeepSpawnInMemory(boolean keepSpawnInMemory) {
    }

    default ResourceKey<LevelStem> getTypeKey(){
        return null;
    }

    default CraftWorld getWorld() {
        return null;
    }

    default CraftServer getCraftServer() {
        return null;
    }

    default void notifyAndUpdatePhysics(BlockPos blockposition, LevelChunk chunk, BlockState oldBlock, BlockState newBlock, BlockState actualBlock, int i, int j) {
    }

    default BlockEntity getBlockEntity(BlockPos blockposition, boolean validate) {
        return null;
    }

    default SpigotWorldConfig bridge$spigotConfig() {
        return null;
    }

    default void banner$setSpigotConfig(SpigotWorldConfig spigotWorldConfig) {
    }

    default CreatureSpawnEvent.SpawnReason getAddEntityReason() {
        return null;
    }
}
