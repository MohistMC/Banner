package com.mohistmc.banner.fabric;

import com.mojang.serialization.Lifecycle;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.storage.DerivedLevelData;
import net.minecraft.world.level.storage.PrimaryLevelData;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraft.world.level.timers.TimerQueue;

public class BannerDerivedWorldInfo extends PrimaryLevelData {

    private final DerivedLevelData derivedWorldInfo;

    public BannerDerivedWorldInfo(LevelSettings levelSettings, WorldOptions worldOptions, SpecialWorldProperty specialWorldProperty, Lifecycle lifecycle, DerivedLevelData derivedLevelData) {
        super(levelSettings, worldOptions, specialWorldProperty, lifecycle);
        this.derivedWorldInfo = derivedLevelData;
    }

    @Override
    public float getSpawnAngle() {
        return derivedWorldInfo.getSpawnAngle();
    }

    @Override
    public long getGameTime() {
        return derivedWorldInfo.getGameTime();
    }

    @Override
    public long getDayTime() {
        return derivedWorldInfo.getDayTime();
    }

    @Override
    public String getLevelName() {
        return derivedWorldInfo.getLevelName();
    }

    @Override
    public int getClearWeatherTime() {
        return derivedWorldInfo.getClearWeatherTime();
    }

    @Override
    public void setClearWeatherTime(int time) {
        derivedWorldInfo.setClearWeatherTime(time);
    }

    @Override
    public boolean isThundering() {
        return derivedWorldInfo.isThundering();
    }

    @Override
    public int getThunderTime() {
        return derivedWorldInfo.getThunderTime();
    }

    @Override
    public boolean isRaining() {
        return derivedWorldInfo.isRaining();
    }

    @Override
    public int getRainTime() {
        return derivedWorldInfo.getRainTime();
    }

    @Override
    public GameType getGameType() {
        return derivedWorldInfo.getGameType();
    }

    @Override
    public void setGameTime(long time) {
        derivedWorldInfo.setGameTime(time);
    }

    @Override
    public void setDayTime(long time) {
        derivedWorldInfo.setDayTime(time);
    }

    @Override
    public void setSpawn(BlockPos spawnPoint, float angle) {
        derivedWorldInfo.setSpawn(spawnPoint, angle);
    }

    @Override
    public void setThundering(boolean thunderingIn) {
        derivedWorldInfo.setThundering(thunderingIn);
    }

    @Override
    public void setThunderTime(int time) {
        derivedWorldInfo.setThunderTime(time);
    }

    @Override
    public void setRaining(boolean isRaining) {
        derivedWorldInfo.setRaining(isRaining);
    }

    @Override
    public void setRainTime(int time) {
        derivedWorldInfo.setRainTime(time);
    }

    @Override
    public void setGameType(GameType type) {
        derivedWorldInfo.setGameType(type);
    }

    @Override
    public boolean isHardcore() {
        return derivedWorldInfo.isHardcore();
    }

    @Override
    public boolean isInitialized() {
        return derivedWorldInfo.isInitialized();
    }

    @Override
    public void setInitialized(boolean initializedIn) {
        derivedWorldInfo.setInitialized(initializedIn);
    }

    @Override
    public GameRules getGameRules() {
        return derivedWorldInfo.getGameRules();
    }

    @Override
    public WorldBorder.Settings getWorldBorder() {
        return derivedWorldInfo.getWorldBorder();
    }

    @Override
    public void setWorldBorder(WorldBorder.Settings serializer) {
        derivedWorldInfo.setWorldBorder(serializer);
    }

    @Override
    public Difficulty getDifficulty() {
        return derivedWorldInfo.getDifficulty();
    }

    @Override
    public boolean isDifficultyLocked() {
        return derivedWorldInfo.isDifficultyLocked();
    }

    @Override
    public TimerQueue<MinecraftServer> getScheduledEvents() {
        return derivedWorldInfo.getScheduledEvents();
    }

    @Override
    public int getWanderingTraderSpawnDelay() {
        return derivedWorldInfo.getWanderingTraderSpawnDelay();
    }

    @Override
    public void setWanderingTraderSpawnDelay(int delay) {
        derivedWorldInfo.setWanderingTraderSpawnDelay(delay);
    }

    @Override
    public int getWanderingTraderSpawnChance() {
        return derivedWorldInfo.getWanderingTraderSpawnChance();
    }

    @Override
    public void setWanderingTraderSpawnChance(int chance) {
        derivedWorldInfo.setWanderingTraderSpawnChance(chance);
    }

    @Override
    public void setWanderingTraderId(UUID id) {
        derivedWorldInfo.setWanderingTraderId(id);
    }

    public static BannerDerivedWorldInfo wrap(DerivedLevelData worldInfo) {
        return new BannerDerivedWorldInfo(worldSettings(worldInfo), generatorSettings(worldInfo), specialWorldProperty(worldInfo), lifecycle(worldInfo), worldInfo);
    }

    private static LevelSettings worldSettings(ServerLevelData worldInfo) {
        if (worldInfo instanceof PrimaryLevelData) {
            return ((PrimaryLevelData) worldInfo).settings;
        } else {
            return worldSettings(((DerivedLevelData) worldInfo).wrapped);
        }
    }

    private static WorldOptions generatorSettings(ServerLevelData worldInfo) {
        if (worldInfo instanceof PrimaryLevelData) {
            return ((PrimaryLevelData) worldInfo).worldGenOptions();
        } else {
            return generatorSettings(((DerivedLevelData) worldInfo).wrapped);
        }
    }

    private static SpecialWorldProperty specialWorldProperty(ServerLevelData serverLevelData) {
        if (serverLevelData instanceof PrimaryLevelData) {
            return ((PrimaryLevelData) serverLevelData).isFlatWorld() ?
                    SpecialWorldProperty.FLAT : (
                    ((PrimaryLevelData) serverLevelData).isDebugWorld() ? SpecialWorldProperty.DEBUG : SpecialWorldProperty.NONE
            );
        } else {
            return specialWorldProperty(((DerivedLevelData) serverLevelData).wrapped);
        }
    }

    private static Lifecycle lifecycle(ServerLevelData worldInfo) {
        if (worldInfo instanceof PrimaryLevelData) {
            return ((PrimaryLevelData) worldInfo).worldGenSettingsLifecycle();
        } else {
            return lifecycle(((DerivedLevelData) worldInfo).wrapped);
        }
    }
}
