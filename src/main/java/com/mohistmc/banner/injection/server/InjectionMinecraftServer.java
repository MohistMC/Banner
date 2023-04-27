package com.mohistmc.banner.injection.server;

import jline.console.ConsoleReader;
import joptsimple.OptionSet;
import net.minecraft.server.WorldLoader;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraft.world.level.storage.WorldData;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.RemoteConsoleCommandSender;
import org.bukkit.craftbukkit.v1_19_R3.CraftServer;

public interface InjectionMinecraftServer {

    default WorldLoader.DataLoadContext bridge$worldLoader() {
        return null;
    }

    default CraftServer bridge$server() {
        return null;
    }

    default void banner$setServer(CraftServer server) {
    }

    default OptionSet bridge$options() {
        return null;
    }

    default ConsoleCommandSender bridge$console() {
        return null;
    }

    default RemoteConsoleCommandSender bridge$remoteConsole() {
        return null;
    }

    default ConsoleReader bridge$reader() {
        return null;
    }

    default boolean bridge$forceTicks() {
        return false;
    }

    default boolean isDebugging() {
        return false;
    }

    default boolean hasStopped() {
        return false;
    }

    default void initWorld(ServerLevel serverWorld, ServerLevelData worldInfo, WorldData saveData, WorldOptions worldOptions) {
    }

    default void prepareLevels(ChunkProgressListener listener, ServerLevel serverWorld) {
    }

    default void addLevel(ServerLevel level) {
    }

    default void removeLevel(ServerLevel level) {
    }

    default void executeModerately() {
    }
}
