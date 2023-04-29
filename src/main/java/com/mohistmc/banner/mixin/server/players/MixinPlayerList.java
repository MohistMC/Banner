package com.mohistmc.banner.mixin.server.players;

import com.mohistmc.banner.BannerServer;
import com.mohistmc.banner.injection.server.players.InjectionPlayerList;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.RegistryLayer;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.players.GameProfileCache;
import net.minecraft.server.players.PlayerList;
import net.minecraft.stats.ServerStatsCounter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.PlayerDataStorage;
import org.bukkit.craftbukkit.v1_19_R3.CraftServer;
import org.bukkit.craftbukkit.v1_19_R3.command.ColouredConsoleSender;
import org.bukkit.plugin.PluginLoadOrder;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

// TODO fix inject method
@Mixin(PlayerList.class)
public abstract class MixinPlayerList implements InjectionPlayerList {

    @Mutable
    @Shadow @Final public List<ServerPlayer> players;

    @Shadow @Nullable public abstract CompoundTag load(ServerPlayer player);
    @Shadow @Final private Map<UUID, ServerPlayer> playersByUUID;

    @Shadow protected abstract void save(ServerPlayer player);

    @Shadow public abstract void broadcastSystemMessage(Component message, boolean bypassHiddenChat);

    @Shadow @Nullable public abstract ServerPlayer getPlayer(UUID playerUUID);

    @Shadow @Final private MinecraftServer server;

    @Shadow @Final private static Logger LOGGER;
    private CraftServer cserver;
    private static final ThreadLocal<String> PROFILE_NAMES = new ThreadLocal<>();

    @Inject(method = "<init>", at = @At(value = "TAIL"))
    public void init(MinecraftServer minecraftServer, LayeredRegistryAccess<RegistryLayer> layeredRegistryAccess, PlayerDataStorage playerDataStorage, int i, CallbackInfo ci) {
        this.players = new CopyOnWriteArrayList<>();
        MinecraftServer banner$server = (DedicatedServer) minecraftServer;
        this.cserver = new CraftServer((DedicatedServer) banner$server, (PlayerList) (Object) this);
        banner$server.banner$setServer(cserver);
        banner$server.banner$setConsole(ColouredConsoleSender.getInstance());
        org.spigotmc.SpigotConfig.init((java.io.File) banner$server.bridge$options().valueOf("spigot-settings"));
        org.spigotmc.SpigotConfig.registerCommands();
        LOGGER.info(" _____       ___   __   _   __   _   _____   _____   ");
        LOGGER.info("|  _  \\     /   | |  \\ | | |  \\ | | | ____| |  _  \\  ");
        LOGGER.info("| |_| |    / /| | |   \\| | |   \\| | | |__   | |_| |  ");
        LOGGER.info("|  _  {   / / | | | |\\   | | |\\   | |  __|  |  _  /  ");
        LOGGER.info("| |_| |  / /  | | | | \\  | | | \\  | | |___  | | \\ \\  ");
        LOGGER.info("|_____/ /_/   |_| |_|  \\_| |_|  \\_| |_____| |_|  \\_\\ ");
        BannerServer.LOGGER.info("Loading Bukkit plugins...");
        this.cserver.loadPlugins();
        this.cserver.enablePlugins(PluginLoadOrder.STARTUP);
    }

    @Inject (method = "placeNewPlayer", at = @At (value = "INVOKE",
            target = "Lnet/minecraft/server/level/ServerPlayer;setLevel(Lnet/minecraft/server/level/ServerLevel;)V"),
            locals = LocalCapture.CAPTURE_FAILHARD)
    private void banner$print(Connection netManager, ServerPlayer player, CallbackInfo ci, GameProfile gameProfile, GameProfileCache gameProfileCache, Optional optional, String string, CompoundTag compoundTag, ResourceKey resourceKey, ServerLevel serverLevel, ServerLevel serverLevel2) {
        if (compoundTag != null && compoundTag.contains("bukkit")) {
            CompoundTag bukkit = compoundTag.getCompound("bukkit");
            PROFILE_NAMES.set(bukkit.contains("lastKnownName", 8) ? bukkit.getString("lastKnownName") : string);
        }
    }

    @Inject(method = "placeNewPlayer", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl;send(Lnet/minecraft/network/protocol/Packet;)V",
            ordinal = 1), locals = LocalCapture.CAPTURE_FAILHARD)
    private void banner$sendSupported(Connection netManager, ServerPlayer player, CallbackInfo ci, GameProfile gameProfile, GameProfileCache gameProfileCache, Optional optional, String string, CompoundTag compoundTag, ResourceKey resourceKey, ServerLevel serverLevel, ServerLevel serverLevel2, String string2, LevelData levelData, ServerGamePacketListenerImpl serverGamePacketListenerImpl) {
        player.getBukkitEntity().sendSupportedChannels();
    }

    @ModifyVariable(method = "placeNewPlayer", at = @At (value = "INVOKE",
            target = "Lnet/minecraft/server/level/ServerPlayer;setLevel(Lnet/minecraft/server/level/ServerLevel;)V"),
            index = 6, ordinal = 0)
    private String banner$renameDetection(String name) {
        String val = PROFILE_NAMES.get();
        if (val != null) {
            PROFILE_NAMES.set(null);
            return val;
        }
        return name;
    }

    @Override
    public void broadcastAll(Packet<?> packet, net.minecraft.world.entity.player.Player entityhuman) {
        for (ServerPlayer entityplayer : this.players) {
            if (!(entityhuman instanceof ServerPlayer) || entityplayer.getBukkitEntity().canSee(((ServerPlayer) entityhuman).getBukkitEntity())) {
                entityplayer.connection.send(packet);
            }
        }
    }

    @Override
    public void broadcastAll(Packet<?> packet, Level world) {
        for (int i = 0; i < world.players().size(); ++i) {
            ((ServerPlayer) world.players().get(i)).connection.send(packet);
        }
    }

    @Override
    public void broadcastMessage(Component[] components) {
        for (Component component : components) {
            broadcastSystemMessage(component, false);
        }
    }

    @Override
    public ServerStatsCounter getPlayerStats(ServerPlayer entityhuman) {
        ServerStatsCounter serverstatisticmanager = entityhuman.getStats();
        return serverstatisticmanager == null ? this.getPlayerStats(entityhuman.getUUID(), entityhuman.getName().getString()) : serverstatisticmanager;
    }

    @Override
    public ServerStatsCounter getPlayerStats(UUID uuid, String displayName) {
        ServerStatsCounter serverstatisticmanager;
        ServerPlayer entityhuman = this.getPlayer(uuid);
        ServerStatsCounter serverStatisticsManager = serverstatisticmanager = entityhuman == null ? null : entityhuman.getStats();
        if (serverstatisticmanager == null) {
            File file2;
            File file = this.server.getWorldPath(LevelResource.PLAYER_STATS_DIR).toFile();
            File file1 = new File(file, uuid + ".json");
            if (!file1.exists() && (file2 = new File(file, displayName + ".json")).exists() && file2.isFile()) {
                file2.renameTo(file1);
            }
            serverstatisticmanager = new ServerStatsCounter(this.server, file1);
        }
        return serverstatisticmanager;
    }

}
