package com.mohistmc.banner.mixin.server.players;

import com.google.common.collect.Lists;
import com.mohistmc.banner.injection.server.players.InjectionPlayerList;
import com.mojang.authlib.GameProfile;
import net.minecraft.ChatFormatting;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.network.protocol.status.ServerStatus;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.RegistryLayer;
import net.minecraft.server.bossevents.CustomBossEvents;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.network.ServerLoginPacketListenerImpl;
import net.minecraft.server.players.*;
import net.minecraft.stats.ServerStatsCounter;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.level.storage.PlayerDataStorage;
import org.bukkit.craftbukkit.v1_19_R3.CraftServer;
import org.bukkit.craftbukkit.v1_19_R3.command.ColouredConsoleSender;
import org.bukkit.craftbukkit.v1_19_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_19_R3.util.CraftChatMessage;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.net.SocketAddress;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

// Banner - TODO fix inject method
@Mixin(PlayerList.class)
public abstract class MixinPlayerList implements InjectionPlayerList {

    @Mutable @Shadow @Final public List<ServerPlayer> players;
    @Shadow @Final private MinecraftServer server;

    @Shadow @Final private static Logger LOGGER;
    @Shadow @Final public PlayerDataStorage playerIo;
    private CraftServer cserver;
    private static final AtomicReference<String> PROFILE_NAMES = new AtomicReference<>();
    private static final AtomicReference<String> banner$joinMessage =new AtomicReference<>();
    private final AtomicReference<String> banner$string2 = new AtomicReference<>();

    @Inject(method = "<init>", at = @At(value = "TAIL"))
    public void banner$init(MinecraftServer minecraftServer, LayeredRegistryAccess<RegistryLayer> layeredRegistryAccess, PlayerDataStorage playerDataStorage, int i, CallbackInfo ci) {
        this.players = new CopyOnWriteArrayList<>();
        MinecraftServer banner$server = (DedicatedServer) minecraftServer;
        this.cserver = new CraftServer((DedicatedServer) banner$server, (PlayerList) (Object) this);
        banner$server.banner$setServer(cserver);
        banner$server.banner$setConsole(ColouredConsoleSender.getInstance());
        org.spigotmc.SpigotConfig.init((java.io.File) banner$server.bridge$options().valueOf("spigot-settings"));
        org.spigotmc.SpigotConfig.registerCommands();
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

    @Redirect(method = "placeNewPlayer", at = @At(value = "INVOKE", target = "Lorg/slf4j/Logger;info(Ljava/lang/String;[Ljava/lang/Object;)V"))
    private void banner$moveDownLogger(Logger instance, String s, Object[] objects) {} // CraftBukkit - Moved message to after join

    @Inject(method = "placeNewPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl;send(Lnet/minecraft/network/protocol/Packet;)V", ordinal = 0))
    private void banner$sendChannel(Connection netManager, ServerPlayer player, CallbackInfo ci) {
        player.getBukkitEntity().sendSupportedChannels(); // CraftBukkit
    }

    @Redirect(method = "placeNewPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/players/PlayerList;broadcastSystemMessage(Lnet/minecraft/network/chat/Component;Z)V"))
    private void banner$cancelMessage(PlayerList instance, Component message, boolean bypassHiddenChat) {}

    @Inject(method = "placeNewPlayer",
            at = @At(value = "INVOKE",
            target = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl;teleport(DDDFF)V",
            shift = At.Shift.BEFORE),
            locals = LocalCapture.CAPTURE_FAILHARD)
    private void banner$addMessage(Connection netManager, ServerPlayer player, CallbackInfo ci, GameProfile gameProfile, GameProfileCache gameProfileCache, Optional optional, String string, CompoundTag compoundTag, ResourceKey resourceKey, ServerLevel serverLevel, ServerLevel serverLevel2, String string2, LevelData levelData, ServerGamePacketListenerImpl serverGamePacketListenerImpl, GameRules gameRules, boolean bl, boolean bl2, MutableComponent mutableComponent) {
        mutableComponent.withStyle(ChatFormatting.YELLOW);
        String joinMessage = CraftChatMessage.fromComponent(mutableComponent);
        banner$joinMessage.set(joinMessage);
    }

    @Redirect(method = "placeNewPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/players/PlayerList;broadcastAll(Lnet/minecraft/network/protocol/Packet;)V"))
    private void banner$cancelText(PlayerList instance, Packet<?> packet) {}

    @Inject(method = "placeNewPlayer",
            at = @At(value = "INVOKE",
            target = "Lnet/minecraft/server/players/PlayerList;sendLevelInfo(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/server/level/ServerLevel;)V",
            shift = At.Shift.BEFORE), cancellable = true)
    private void banner$fireJoinEvent(Connection netManager, ServerPlayer player, CallbackInfo ci) {
        // CraftBukkit start
        CraftPlayer bukkitPlayer = player.getBukkitEntity();

        // Ensure that player inventory is populated with its viewer
        player.containerMenu.transferTo(player.containerMenu, bukkitPlayer);

        PlayerJoinEvent playerJoinEvent = new PlayerJoinEvent(bukkitPlayer, banner$joinMessage.get());
        cserver.getPluginManager().callEvent(playerJoinEvent);

        if (!player.connection.isAcceptingMessages()) {
            ci.cancel();
        }

        banner$joinMessage.set(playerJoinEvent.getJoinMessage());

        if (banner$joinMessage.get() != null && banner$joinMessage.get().length() > 0) {
            for (Component line : CraftChatMessage.fromString(banner$joinMessage.get())) {
                server.getPlayerList().broadcastSystemMessage(line, false);
            }
        }
        // CraftBukkit end

        // CraftBukkit start - sendAll above replaced with this loop
        ClientboundPlayerInfoUpdatePacket packet = ClientboundPlayerInfoUpdatePacket.createPlayerInitializing(List.of(player));

        for (int i = 0; i < this.players.size(); ++i) {
            ServerPlayer entityplayer1 = (ServerPlayer) this.players.get(i);

            if (entityplayer1.getBukkitEntity().canSee(bukkitPlayer)) {
                entityplayer1.connection.send(packet);
            }

            if (!bukkitPlayer.canSee(entityplayer1.getBukkitEntity())) {
                continue;
            }

            player.connection.send(ClientboundPlayerInfoUpdatePacket.createPlayerInitializing(List.of(entityplayer1)));
        }
        player.banner$setSentListPacket(true);
        // CraftBukkit end

        player.getEntityData().refresh(player); // CraftBukkit - BungeeCord#2321, send complete data to self on spawn
    }

    @Redirect(method = "placeNewPlayer",
            at = @At(value = "INVOKE",
            target = "Lnet/minecraft/server/level/ServerLevel;addNewPlayer(Lnet/minecraft/server/level/ServerPlayer;)V"))
    private void banner$cancelAddPlayer(ServerLevel instance, ServerPlayer player) {}

    @Redirect(method = "placeNewPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;getCustomBossEvents()Lnet/minecraft/server/bossevents/CustomBossEvents;"))
    private CustomBossEvents banner$cancelBossEvent(MinecraftServer instance){
        return null;
    }

    @Inject(method = "placeNewPlayer",
            at = @At(value = "INVOKE",
            target = "Lnet/minecraft/server/MinecraftServer;getServerResourcePack()Ljava/util/Optional;",
            shift = At.Shift.BEFORE), locals = LocalCapture.CAPTURE_FAILHARD)
    private void banner$addJoinCheck(Connection netManager, ServerPlayer player, CallbackInfo ci, GameProfile gameProfile, GameProfileCache gameProfileCache, Optional optional, String string, CompoundTag compoundTag, ResourceKey resourceKey, ServerLevel serverLevel, ServerLevel serverLevel2, String string2, LevelData levelData, ServerGamePacketListenerImpl serverGamePacketListenerImpl) {
        // CraftBukkit start - Only add if the player wasn't moved in the event
        if (player.level == serverLevel2 && !serverLevel2.players().contains(player)) {
            serverLevel2.addNewPlayer(player);
            this.server.getCustomBossEvents().onPlayerConnect(player);
        }
        serverLevel2 = player.getLevel(); // CraftBukkit - Update in case join event changed it
    }

    @Redirect(method = "placeNewPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/EntityType;loadEntityRecursive(Lnet/minecraft/nbt/CompoundTag;Lnet/minecraft/world/level/Level;Ljava/util/function/Function;)Lnet/minecraft/world/entity/Entity;"))
    private Entity banner$cancelEntityType(CompoundTag compound, Level level, Function<Entity, Entity> entityFunction) {
        return null;
    }

    @Inject(method = "placeNewPlayer",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/nbt/CompoundTag;getCompound(Ljava/lang/String;)Lnet/minecraft/nbt/CompoundTag;",
                    ordinal = 1),
            locals = LocalCapture.CAPTURE_FAILHARD)
    private void banner$addEntityTypeCheck(Connection netManager, ServerPlayer player, CallbackInfo ci, GameProfile gameProfile, GameProfileCache gameProfileCache, Optional optional, String string, CompoundTag compoundTag, ResourceKey resourceKey, ServerLevel serverLevel, ServerLevel serverLevel2, String string2, LevelData levelData, ServerGamePacketListenerImpl serverGamePacketListenerImpl, GameRules gameRules, boolean bl, boolean bl2, MutableComponent mutableComponent, ServerStatus serverStatus, CompoundTag compoundTag2) {
        ServerLevel finalWorldServer = serverLevel2;
        Entity entity = EntityType.loadEntityRecursive(compoundTag2.getCompound("Entity"), finalWorldServer, (entity1) -> {
            return !finalWorldServer.addWithUUID(entity1) ? null : entity1;
        });
    }

    @Inject(method = "placeNewPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;loadGameTypes(Lnet/minecraft/nbt/CompoundTag;)V"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void banner$setString(Connection netManager, ServerPlayer player, CallbackInfo ci, GameProfile gameProfile, GameProfileCache gameProfileCache, Optional optional, String string, CompoundTag compoundTag, ResourceKey resourceKey, ServerLevel serverLevel, ServerLevel serverLevel2, String string2, LevelData levelData) {
        banner$string2.set(string2);
    }

    @Inject(method = "placeNewPlayer", at = @At("TAIL"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void banner$addLogger(Connection netManager, ServerPlayer player, CallbackInfo ci) {
        LOGGER.info("{}[{}] logged in with entity id {} at ({}, {}, {})", new Object[]{player.getName().getString(), banner$string2.get(), player.getId(), player.getX(), player.getY(), player.getZ()});
    }

    @Redirect(method = "placeNewPlayer", at = @At(value = "FIELD", target = "Lnet/minecraft/server/players/PlayerList;viewDistance:I"))
    private int banner$spigotViewDistance(PlayerList playerList, Connection netManager, ServerPlayer playerIn) {
        return playerIn.getLevel().bridge$spigotConfig().viewDistance;
    }

    @Redirect(method = "placeNewPlayer", at = @At(value = "FIELD", target = "Lnet/minecraft/server/players/PlayerList;simulationDistance:I"))
    private int banner$spigotSimDistance(PlayerList instance, Connection netManager, ServerPlayer playerIn) {
        return playerIn.getLevel().bridge$spigotConfig().simulationDistance;
    }

    /**
     * @author wdog5
     * @reason
     */
    @Overwrite
    protected void save(ServerPlayer player) {
        if (!player.getBukkitEntity().isPersistent()) return; // CraftBukkit
        this.playerIo.save(player);
        ServerStatsCounter serverStatsCounter = (ServerStatsCounter)player.getStats();
        if (serverStatsCounter != null) {
            serverStatsCounter.save();
        }

        PlayerAdvancements playerAdvancements = (PlayerAdvancements) player.getAdvancements(); // CraftBukkit
        if (playerAdvancements != null) {
            playerAdvancements.save();
        }
    }

    @Inject(method = "remove", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;awardStat(Lnet/minecraft/resources/ResourceLocation;)V"))
    private void banner$quitEvent(ServerPlayer player, CallbackInfo ci) {
        remove(player);
    }

    @Redirect(method = "remove", at = @At(value = "FIELD", target = "Lnet/minecraft/server/players/PlayerList;playersByUUID:Ljava/util/Map;", ordinal = 1))
    private Map<UUID, ServerPlayer> banner$cancelRemoveInfo(PlayerList instance) {
        return null;
    }

    @Redirect(method = "remove", at = @At(value = "FIELD", target = "Lnet/minecraft/server/players/PlayerList;stats:Ljava/util/Map;"))
    private Map<UUID, ServerStatsCounter> banner$cancelRemove1(PlayerList instance) {
        return null;
    }

    @Redirect(method = "remove", at = @At(value = "FIELD", target = "Lnet/minecraft/server/players/PlayerList;advancements:Ljava/util/Map;"))
    private Map<UUID, PlayerAdvancements> banner$cancelRemove2(PlayerList instance) {
        return null;
    }

    @Redirect(method = "remove", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/players/PlayerList;broadcastAll(Lnet/minecraft/network/protocol/Packet;)V"))
    private void banner$cancelText2(PlayerList instance, Packet<?> packet) {}

    @Inject(method = "remove", at = @At("TAIL"))
    private void banner$addRemoveThings(ServerPlayer player, CallbackInfo ci) {
        ClientboundPlayerInfoRemovePacket packet = new ClientboundPlayerInfoRemovePacket(List.of(player.getUUID()));
        for (int i = 0; i < players.size(); i++) {
            ServerPlayer entityplayer2 = (ServerPlayer) this.players.get(i);

            if (entityplayer2.getBukkitEntity().canSee(player.getBukkitEntity())) {
                entityplayer2.connection.send(packet);
            } else {
                entityplayer2.getBukkitEntity().onEntityRemove(player);
            }
        }
        // This removes the scoreboard (and player reference) for the specific player in the manager
        cserver.getScoreboardManager().removePlayer(player.getBukkitEntity());
        // CraftBukkit end
    }

    @Override
    public String remove(ServerPlayer entityplayer) {
        // CraftBukkit start - Quitting must be before we do final save of data, in case plugins need to modify it
        // See SPIGOT-5799, SPIGOT-6145
        if (entityplayer.containerMenu != entityplayer.inventoryMenu) {
            entityplayer.closeContainer();
        }

        PlayerQuitEvent playerQuitEvent = new PlayerQuitEvent(entityplayer.getBukkitEntity(), entityplayer.bridge$kickLeaveMessage() != null ? entityplayer.bridge$kickLeaveMessage() : "\u00A7e" + entityplayer.getScoreboardName() + " left the game");
        cserver.getPluginManager().callEvent(playerQuitEvent);
        entityplayer.getBukkitEntity().disconnect(playerQuitEvent.getQuitMessage());

        entityplayer.doTick(); // SPIGOT-924
        // CraftBukkit end

        return playerQuitEvent.getQuitMessage();
    }

    @Override
    public ServerPlayer canPlayerLogin(SocketAddress socketAddress, GameProfile gameProfile, ServerLoginPacketListenerImpl handler) {
        // Moved from processLogin
        UUID uuid = UUIDUtil.getOrCreatePlayerUUID(gameProfile);
        List<ServerPlayer> list = Lists.newArrayList();

        ServerPlayer entityplayer;

        for (int i = 0; i < this.players.size(); ++i) {
            entityplayer = (ServerPlayer) this.players.get(i);
            if (entityplayer.getUUID().equals(uuid)) {
                list.add(entityplayer);
            }
        }

        Iterator iterator = list.iterator();

        while (iterator.hasNext()) {
            entityplayer = (ServerPlayer) iterator.next();
            save(entityplayer); // CraftBukkit - Force the player's inventory to be saved
            entityplayer.connection.disconnect(Component.translatable("multiplayer.disconnect.duplicate_login"));
        }

        // Instead of kicking then returning, we need to store the kick reason
        // in the event, check with plugins to see if it's ok, and THEN kick
        // depending on the outcome.
        SocketAddress socketaddress = handler.connection.getRemoteAddress();

        ServerPlayer entity = new ServerPlayer(this.server, this.server.getLevel(Level.OVERWORLD), gameProfile);
        org.bukkit.entity.Player player = entity.getBukkitEntity();
        PlayerLoginEvent event = new PlayerLoginEvent(player, handler.connection.bridge$hostname(), ((java.net.InetSocketAddress) socketaddress).getAddress());
        return entity;
    }
}
