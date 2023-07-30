package com.mohistmc.banner.mixin.server.players;

import com.google.common.collect.Lists;
import com.llamalad7.mixinextras.injector.WrapWithCondition;
import com.mohistmc.banner.BannerMCStart;
import com.mohistmc.banner.BannerServer;
import com.mohistmc.banner.fabric.BukkitRegistry;
import com.mohistmc.banner.injection.server.players.InjectionPlayerList;
import com.mojang.authlib.GameProfile;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundChangeDifficultyPacket;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundRespawnPacket;
import net.minecraft.network.protocol.game.ClientboundSetBorderCenterPacket;
import net.minecraft.network.protocol.game.ClientboundSetBorderLerpSizePacket;
import net.minecraft.network.protocol.game.ClientboundSetBorderSizePacket;
import net.minecraft.network.protocol.game.ClientboundSetBorderWarningDelayPacket;
import net.minecraft.network.protocol.game.ClientboundSetBorderWarningDistancePacket;
import net.minecraft.network.protocol.game.ClientboundSetChunkCacheRadiusPacket;
import net.minecraft.network.protocol.game.ClientboundSetDefaultSpawnPositionPacket;
import net.minecraft.network.protocol.game.ClientboundSetExperiencePacket;
import net.minecraft.network.protocol.game.ClientboundSetSimulationDistancePacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateMobEffectPacket;
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
import net.minecraft.server.players.GameProfileCache;
import net.minecraft.server.players.IpBanList;
import net.minecraft.server.players.IpBanListEntry;
import net.minecraft.server.players.PlayerList;
import net.minecraft.server.players.UserBanList;
import net.minecraft.server.players.UserBanListEntry;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.ServerStatsCounter;
import net.minecraft.stats.Stats;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.BorderChangeListener;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.PlayerDataStorage;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_20_R1.CraftServer;
import org.bukkit.craftbukkit.v1_20_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_20_R1.command.ColouredConsoleSender;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_20_R1.util.CraftChatMessage;
import org.bukkit.craftbukkit.v1_20_R1.util.CraftLocation;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerSpawnChangeEvent;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.io.File;
import java.net.SocketAddress;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

// Banner - TODO fix inject method
@Mixin(PlayerList.class)
public abstract class MixinPlayerList implements InjectionPlayerList {

    @Mutable
    @Shadow @Final public List<ServerPlayer> players;

    @Shadow @Nullable public abstract CompoundTag load(ServerPlayer player);
    @Shadow @Final private Map<UUID, ServerPlayer> playersByUUID;

    @Shadow public abstract void broadcastSystemMessage(Component message, boolean bypassHiddenChat);

    @Shadow @Nullable public abstract ServerPlayer getPlayer(UUID playerUUID);

    @Shadow @Final private MinecraftServer server;

    @Shadow public abstract UserBanList getBans();

    @Shadow @Final private UserBanList bans;
    @Shadow @Final private static SimpleDateFormat BAN_DATE_FORMAT;

    @Shadow public abstract boolean isWhiteListed(GameProfile profile);

    @Shadow public abstract IpBanList getIpBans();

    @Shadow @Final private IpBanList ipBans;
    @Shadow @Final protected int maxPlayers;

    @Shadow public abstract boolean canBypassPlayerLimit(GameProfile profile);

    @Shadow public abstract void sendLevelInfo(ServerPlayer player, ServerLevel level);

    @Shadow public abstract void sendPlayerPermissionLevel(ServerPlayer player);

    @Shadow public abstract void sendAllPlayerInfo(ServerPlayer player);

    @Shadow @Final public PlayerDataStorage playerIo;
    @Shadow @Final private static Logger LOGGER;

    @Shadow public abstract ServerPlayer getPlayerForLogin(GameProfile profile);

    @Shadow protected abstract void save(ServerPlayer player);

    private CraftServer cserver;

    private static final AtomicReference<String> PROFILE_NAMES = new AtomicReference<>();

    @Inject(method = "<init>", at = @At(value = "FIELD", target = "Lnet/minecraft/server/players/PlayerList;bans:Lnet/minecraft/server/players/UserBanList;"))
    public void banner$init(MinecraftServer minecraftServer, LayeredRegistryAccess<RegistryLayer> layeredRegistryAccess, PlayerDataStorage playerDataStorage, int i, CallbackInfo ci) {
        this.players = new CopyOnWriteArrayList<>();
        minecraftServer.banner$setServer(this.cserver =
                new CraftServer((DedicatedServer) minecraftServer, ((PlayerList) (Object) this)));
        BannerServer.LOGGER.info(BannerMCStart.I18N.get("registry.begin"));
        BukkitRegistry.registerAll((DedicatedServer) minecraftServer);
        minecraftServer.banner$setConsole(ColouredConsoleSender.getInstance());
    }

    @Inject(method = "placeNewPlayer", at = @At (value = "INVOKE",
            target = "Lnet/minecraft/server/level/ServerPlayer;setServerLevel(Lnet/minecraft/server/level/ServerLevel;)V"),
            locals = LocalCapture.CAPTURE_FAILHARD)
    public void print(Connection netManager, ServerPlayer player, CallbackInfo ci,
                      GameProfile gameProfile, GameProfileCache gameProfileCache,
                      String string, CompoundTag compoundTag, ResourceKey resourceKey,
                      ServerLevel serverLevel, ServerLevel serverLevel2) {
        if (compoundTag != null && compoundTag.contains("bukkit")) {
            CompoundTag bukkit = compoundTag.getCompound("bukkit");
            PROFILE_NAMES.set(bukkit.contains("lastKnownName", 8) ? bukkit.getString("lastKnownName") : string);
        }
    }

    @ModifyVariable(method = "placeNewPlayer", at = @At (value = "INVOKE",
            target = "Lnet/minecraft/server/level/ServerPlayer;setServerLevel(Lnet/minecraft/server/level/ServerLevel;)V"),
            index = 6, ordinal = 0)
    private String banner$renameDetection(String name) {
        String val = PROFILE_NAMES.get();
        if (val != null) {
            PROFILE_NAMES.set(null);
            return val;
        }
        return name;
    }

    @Inject(method = "placeNewPlayer",
            locals = LocalCapture.CAPTURE_FAILHARD,
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerLevel;getLevelData()Lnet/minecraft/world/level/storage/LevelData;", shift = At.Shift.BEFORE))
    private void banner$callSpawnEvent(Connection netManager, ServerPlayer player, CallbackInfo ci,
                                       GameProfile gameProfile, GameProfileCache gameProfileCache,
                                       String string, CompoundTag compoundTag, ResourceKey resourceKey,
                                       ServerLevel serverLevel, ServerLevel serverLevel2, String string2) {
        // Spigot start - spawn location event
        org.bukkit.entity.Player spawnPlayer = player.getBukkitEntity();
        org.spigotmc.event.player.PlayerSpawnLocationEvent ev = new com.destroystokyo.paper.event.player.PlayerInitialSpawnEvent(spawnPlayer, spawnPlayer.getLocation()); // Paper use our duplicate event
        cserver.getPluginManager().callEvent(ev);

        Location loc = ev.getSpawnLocation();
        serverLevel2 = ((CraftWorld) loc.getWorld()).getHandle();

        player.spawnIn(serverLevel2);
        player.gameMode.setLevel((ServerLevel) player.level());
        // Paper start - set raw so we aren't fully joined to the world (not added to chunk or world)
        player.setPosRaw(loc.getX(), loc.getY(), loc.getZ());
        player.setRot(loc.getYaw(), loc.getPitch());
        // Paper end
    }

    @Redirect(method = "placeNewPlayer", at = @At(value = "FIELD", target = "Lnet/minecraft/server/players/PlayerList;viewDistance:I"))
    private int banner$spigotViewDistance(PlayerList playerList, Connection netManager, ServerPlayer playerIn) {
        return playerIn.serverLevel().bridge$spigotConfig().viewDistance;
    }

    @Redirect(method = "placeNewPlayer", at = @At(value = "FIELD", target = "Lnet/minecraft/server/players/PlayerList;simulationDistance:I"))
    private int banner$spigotSimDistance(PlayerList instance, Connection netManager, ServerPlayer playerIn) {
        return playerIn.serverLevel().bridge$spigotConfig().simulationDistance;
    }

    @Redirect(method = "placeNewPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/players/PlayerList;broadcastSystemMessage(Lnet/minecraft/network/chat/Component;Z)V"))
    private void banner$cancelMessage(PlayerList instance, Component message, boolean bypassHiddenChat) {
    }

    private AtomicReference<String> banner$joinMsg = new AtomicReference<>();

    @Inject(method = "placeNewPlayer",
            at = @At(value = "INVOKE",
            target = "Lnet/minecraft/server/players/PlayerList;broadcastSystemMessage(Lnet/minecraft/network/chat/Component;Z)V"),
            locals = LocalCapture.CAPTURE_FAILHARD)
    private void banner$playerJoin(Connection netManager, ServerPlayer player, CallbackInfo ci,
                                   GameProfile gameProfile, GameProfileCache gameProfileCache,
                                   String string, CompoundTag compoundTag, ResourceKey resourceKey,
                                   ServerLevel serverLevel, ServerLevel serverLevel2, String string2,
                                   LevelData levelData, ServerGamePacketListenerImpl serverGamePacketListenerImpl,
                                   GameRules gameRules, boolean bl, boolean bl2, MutableComponent mutableComponent) {
        // CraftBukkit start
        mutableComponent.withStyle(ChatFormatting.YELLOW);
        String joinMessage = CraftChatMessage.fromComponent(mutableComponent);
        banner$joinMsg.set(joinMessage);
    }

    @Redirect(method = "placeNewPlayer", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/server/players/PlayerList;broadcastAll(Lnet/minecraft/network/protocol/Packet;)V"))
    private void banner$cancelBroadcast(PlayerList instance, Packet<?> packet) {}

    @Inject(method = "placeNewPlayer", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/server/players/PlayerList;sendLevelInfo(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/server/level/ServerLevel;)V"),
            locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
    private void banner$joinEvent(Connection netManager, ServerPlayer player, CallbackInfo ci,
                                  GameProfile gameProfile, GameProfileCache gameProfileCache,
                                  String string, CompoundTag compoundTag, ResourceKey resourceKey,
                                  ServerLevel serverLevel, ServerLevel serverLevel2, String string2,
                                  LevelData levelData, ServerGamePacketListenerImpl serverGamePacketListenerImpl) {
        // CraftBukkit start
        CraftPlayer bukkitPlayer = player.getBukkitEntity();

        // Ensure that player inventory is populated with its viewer
        player.containerMenu.transferTo(player.containerMenu, bukkitPlayer);

        PlayerJoinEvent playerJoinEvent = new PlayerJoinEvent(bukkitPlayer, banner$joinMsg.get());
        cserver.getPluginManager().callEvent(playerJoinEvent);

        if (!player.connection.isAcceptingMessages()) {
            ci.cancel();
        }

        banner$joinMsg.set(playerJoinEvent.getJoinMessage());
        if (banner$joinMsg.get() != null && banner$joinMsg.get().length() > 0) {
            for (Component line : CraftChatMessage.fromString(banner$joinMsg.get())) {
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

    private static AtomicReference<ServerLevel> banner$level = new AtomicReference<>();

    @WrapWithCondition(method = "placeNewPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;addNewPlayer(Lnet/minecraft/server/level/ServerPlayer;)V"))
    private boolean banner$wrapAddNewPlayer(ServerLevel instance, ServerPlayer player) {
        banner$level.set(instance);
        return player.level() == instance && !instance.players().contains(player);
    }

    @WrapWithCondition(method = "placeNewPlayer", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/server/bossevents/CustomBossEvents;onPlayerConnect(Lnet/minecraft/server/level/ServerPlayer;)V"))
    private boolean banner$wrapAddNewPlayer0(CustomBossEvents instance, ServerPlayer player) {
        return player.level() == banner$level.get() && !banner$level.get().players().contains(player);
    }

    @ModifyVariable(method = "placeNewPlayer", ordinal = 1, at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/server/level/ServerLevel;addNewPlayer(Lnet/minecraft/server/level/ServerPlayer;)V"))
    private ServerLevel banner$handleWorldChanges(ServerLevel value, Connection connection, ServerPlayer player) {
        return player.serverLevel();// CraftBukkit - Update in case join event changed it
    }


    @Mixin(PlayerList.class)
    public static class LoadRecursive {
        @Redirect(method = "placeNewPlayer", at = @At(value = "INVOKE",
                target = "Lnet/minecraft/world/entity/EntityType;loadEntityRecursive(Lnet/minecraft/nbt/CompoundTag;Lnet/minecraft/world/level/Level;Ljava/util/function/Function;)Lnet/minecraft/world/entity/Entity;"))
        private Entity banner$loadRecursive(CompoundTag compound, Level level, Function<Entity, Entity> entityFunction) {
            // CraftBukkit start
            ServerLevel finalWorldServer = banner$level.get();
            finalWorldServer = finalWorldServer == null ? ((ServerLevel) level) : finalWorldServer;
            ServerLevel finalWorldServer1 = finalWorldServer;
            return EntityType.loadEntityRecursive(compound.getCompound("Entity"), finalWorldServer, (entityx) -> {
                return !finalWorldServer1.addWithUUID(entityx) ? null : entityx;
            });
        }

    }

    private final AtomicReference<ServerPlayer> banner$savePlayer = new AtomicReference<>();

    @Inject(method = "save", at = @At("HEAD"), cancellable = true)
    private void banner$setPlayerSaved(ServerPlayer player, CallbackInfo ci) {
        if (!player.getBukkitEntity().isPersistent()) {
            ci.cancel();
        }
        banner$savePlayer.set(player);
    }

    @Redirect(method = "save", at = @At(value = "INVOKE",
            target = "Ljava/util/Map;get(Ljava/lang/Object;)Ljava/lang/Object;", ordinal = 0))
    private Object banner$changeMap(Map instance, Object o) {
        return banner$savePlayer.get().getStats();
    }

    @Redirect(method = "save", at = @At(value = "INVOKE",
            target = "Ljava/util/Map;get(Ljava/lang/Object;)Ljava/lang/Object;", ordinal = 1))
    private Object banner$changeMap0(Map instance, Object o) {
        return banner$savePlayer.get().getAdvancements();
    }

    /**
     * @author wdog5
     * @reason bukkit
     */
    @Overwrite
    public void remove(ServerPlayer player) {
        ServerLevel serverLevel = player.serverLevel();
        player.awardStat(Stats.LEAVE_GAME);
        this.extra$remove(player);
        this.save(player);
        if (player.isPassenger()) {
            Entity entity = player.getRootVehicle();
            if (entity.hasExactlyOnePlayerPassenger()) {
                LOGGER.debug("Removing player mount");
                player.stopRiding();
                entity.getPassengersAndSelf().forEach((entityx) -> {
                    entityx.setRemoved(Entity.RemovalReason.UNLOADED_WITH_PLAYER);
                });
            }
        }

        player.unRide();
        serverLevel.removePlayerImmediately(player, Entity.RemovalReason.UNLOADED_WITH_PLAYER);
        player.getAdvancements().stopListening();
        this.players.remove(player);
        this.server.getCustomBossEvents().onPlayerDisconnect(player);
        UUID uUID = player.getUUID();
        ServerPlayer serverPlayer = (ServerPlayer)this.playersByUUID.get(uUID);
        if (serverPlayer == player) {
            this.playersByUUID.remove(uUID);
            // this.stats.remove(uUID);
            // this.advancements.remove(uUID);
        }
        // CraftBukkit start
        ClientboundPlayerInfoRemovePacket packet = new ClientboundPlayerInfoRemovePacket(List.of(serverPlayer.getUUID()));
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
    public String extra$remove(ServerPlayer playerIn) {
        // CraftBukkit start - Quitting must be before we do final save of data, in case plugins need to modify it
        // See SPIGOT-5799, SPIGOT-6145
        if (playerIn.containerMenu != playerIn.inventoryMenu) {
            playerIn.closeContainer();
        }

        PlayerQuitEvent playerQuitEvent = new PlayerQuitEvent(playerIn.getBukkitEntity(), playerIn.bridge$kickLeaveMessage() != null ?
                playerIn.bridge$kickLeaveMessage() : "\u00A7e" + playerIn.getScoreboardName() + " left the game");
        cserver.getPluginManager().callEvent(playerQuitEvent);
        playerIn.getBukkitEntity().disconnect(playerQuitEvent.getQuitMessage());
        playerIn.doTick(); // SPIGOT-924
        // CraftBukkit end
        return playerQuitEvent.getQuitMessage();
    }

    @Override
    public ServerPlayer getPlayerForLogin(GameProfile gameprofile, ServerPlayer player) {
        return this.getPlayerForLogin(gameprofile);
    }

    @Override
    public ServerPlayer canPlayerLogin(ServerLoginPacketListenerImpl handler, GameProfile gameProfile) {
        MutableComponent mutablecomponent1 = Component.empty();
        // Moved from processLogin
        UUID uuid = UUIDUtil.getOrCreatePlayerUUID(gameProfile);
        List<ServerPlayer> list = Lists.newArrayList();

        ServerPlayer entityplayer;

        for (ServerPlayer value : this.players) {
            entityplayer = (ServerPlayer) value;
            if (entityplayer.getUUID().equals(uuid)) {
                list.add(entityplayer);
            }
        }

        for (ServerPlayer serverPlayer : list) {
            entityplayer = serverPlayer;
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

        if (getBans().isBanned(gameProfile) && !getBans().get(gameProfile).hasExpired()) {
            UserBanListEntry userbanlistentry = this.bans.get(gameProfile);
            mutablecomponent1 = Component.translatable("multiplayer.disconnect.banned.reason", userbanlistentry.getReason());
            if (userbanlistentry.getExpires() != null) {
                mutablecomponent1.append(Component.translatable("multiplayer.disconnect.banned.expiration", BAN_DATE_FORMAT.format(userbanlistentry.getExpires())));
            }

            event.disallow(PlayerLoginEvent.Result.KICK_BANNED, org.spigotmc.SpigotConfig.whitelistMessage); // Spigot
        } else if (!this.isWhiteListed(gameProfile)) {
            mutablecomponent1 = Component.translatable("multiplayer.disconnect.not_whitelisted");
            event.disallow(PlayerLoginEvent.Result.KICK_WHITELIST, CraftChatMessage.fromComponent(mutablecomponent1));
        } else if (getIpBans().isBanned(socketaddress) && !getIpBans().get(socketaddress).hasExpired()) {
            IpBanListEntry ipbanlistentry = this.ipBans.get(socketaddress);
            mutablecomponent1 = Component.translatable("multiplayer.disconnect.banned_ip.reason", ipbanlistentry.getReason());
            if (ipbanlistentry.getExpires() != null) {
                mutablecomponent1.append(Component.translatable("multiplayer.disconnect.banned_ip.expiration", BAN_DATE_FORMAT.format(ipbanlistentry.getExpires())));
            }

            event.disallow(PlayerLoginEvent.Result.KICK_BANNED, CraftChatMessage.fromComponent(mutablecomponent1));
        } else {
            if (this.players.size() >= this.maxPlayers && !this.canBypassPlayerLimit(gameProfile)) {
                event.disallow(PlayerLoginEvent.Result.KICK_FULL, org.spigotmc.SpigotConfig.serverFullMessage); // Spigot
            }
        }

        cserver.getPluginManager().callEvent(event);
        if (event.getResult() != PlayerLoginEvent.Result.ALLOWED) {
            handler.disconnect(event.getKickMessage());
            return null;
        }
        return entity;
    }

    private transient Location banner$loc;
    private transient PlayerRespawnEvent.RespawnReason banner$respawnReason;
    public ServerLevel banner$worldserver;

    @Override
    public ServerPlayer respawn(ServerPlayer playerIn, ServerLevel worldIn, boolean flag, Location location, boolean avoidSuffocation, PlayerRespawnEvent.RespawnReason respawnReason) {
        this.banner$loc = location;
        this.banner$worldserver = worldIn;
        this.banner$respawnReason = respawnReason;
        return respawn(playerIn, flag);
    }


    @Override
    public ServerPlayer respawn(ServerPlayer entityplayer, boolean flag, PlayerRespawnEvent.RespawnReason reason) {
        return this.respawn(entityplayer, this.server.getLevel(entityplayer.getRespawnDimension()), flag, null, true, reason);
    }

    /**
     * @author wdog5
     * @reason bukkit
     */
    @Overwrite
    public ServerPlayer respawn(ServerPlayer playerIn, boolean conqueredEnd) {
        playerIn.stopRiding(); // CraftBukkit
        this.players.remove(playerIn);
        playerIn.serverLevel().removePlayerImmediately(playerIn, Entity.RemovalReason.DISCARDED);
        BlockPos blockposition = playerIn.getRespawnPosition();
        float f = playerIn.getRespawnAngle();
        boolean flag1 = playerIn.isRespawnForced();
        // CraftBukkit start
        // Banner start - remain original field to compat with carpet
        ServerLevel worldserver_vanilla = this.server.getLevel(playerIn.getRespawnDimension());
        Optional optional_vanilla;

        if (worldserver_vanilla != null && blockposition != null) {
            optional_vanilla = net.minecraft.world.entity.player.Player.findRespawnPositionAndUseSpawnBlock(worldserver_vanilla, blockposition, f, flag1, flag1);
        } else {
            optional_vanilla = Optional.empty();
        }

        ServerLevel worldserver_vanilla_1 = worldserver_vanilla != null && optional_vanilla.isPresent() ? worldserver_vanilla : this.server.overworld();
        ServerPlayer entityplayer_vanilla = new ServerPlayer(this.server, worldserver_vanilla_1, playerIn.getGameProfile());
        // Banner end

        ServerPlayer entityplayer1 = playerIn;
        org.bukkit.World fromWorld = playerIn.getBukkitEntity().getWorld();
        playerIn.wonGame = false;
        // CraftBukkit end

        entityplayer1.connection = playerIn.connection;
        entityplayer1.restoreFrom(playerIn, conqueredEnd);
        entityplayer1.setId(playerIn.getId());
        entityplayer1.setMainArm(playerIn.getMainArm());
        Iterator iterator = playerIn.getTags().iterator();

        while (iterator.hasNext()) {
            String s = (String) iterator.next();

            entityplayer1.addTag(s);
        }

        boolean flag2 = false;

        // CraftBukkit start - fire PlayerRespawnEvent
        if (banner$loc == null) {
            boolean isBedSpawn = false;
            ServerLevel worldserver1 = this.server.getLevel(playerIn.getRespawnDimension());
            if (worldserver1 != null) {
                Optional optional;

                if (blockposition != null) {
                    optional = net.minecraft.world.entity.player.Player.findRespawnPositionAndUseSpawnBlock(worldserver1, blockposition, f, flag1, conqueredEnd);
                } else {
                    optional = Optional.empty();
                }

                if (optional.isPresent()) {
                    BlockState iblockdata = worldserver1.getBlockState(blockposition);
                    boolean flag3 = iblockdata.is(Blocks.RESPAWN_ANCHOR);
                    Vec3 vec3d = (Vec3) optional.get();
                    float f1;

                    if (!iblockdata.is(BlockTags.BEDS) && !flag3) {
                        f1 = f;
                    } else {
                        Vec3 vec3d1 = Vec3.atBottomCenterOf(blockposition).subtract(vec3d).normalize();

                        f1 = (float) Mth.wrapDegrees(Mth.atan2(vec3d1.z, vec3d1.x) * 57.2957763671875D - 90.0D);
                    }

                    // entityplayer1.setRespawnPosition(worldserver1.dimension(), blockposition, f, flag1, false); // CraftBukkit - not required, just copies old location into reused entity
                    flag2 = !conqueredEnd && flag3;
                    isBedSpawn = true;
                    banner$loc = CraftLocation.toBukkit(vec3d, worldserver1.getWorld(), f1, 0.0F);
                } else if (blockposition != null) {
                    entityplayer1.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.NO_RESPAWN_BLOCK_AVAILABLE, 0.0F));
                    entityplayer1.setRespawnPosition(null, null, 0f, false, false, PlayerSpawnChangeEvent.Cause.RESET); // CraftBukkit - SPIGOT-5988: Clear respawn location when obstructed
                }
            }

            if (banner$loc == null) {
                worldserver1 = this.server.getLevel(Level.OVERWORLD);
                blockposition = entityplayer1.getSpawnPoint(worldserver1);
                banner$loc = CraftLocation.toBukkit(blockposition, worldserver1.getWorld()).add(0.5F, 0.1F, 0.5F);
            }

            Player respawnPlayer = entityplayer1.getBukkitEntity();
            PlayerRespawnEvent respawnEvent = new PlayerRespawnEvent(respawnPlayer, banner$loc, isBedSpawn && !flag2, flag2, banner$respawnReason);
            cserver.getPluginManager().callEvent(respawnEvent);
            // Spigot Start
            if (playerIn.connection.isDisconnected()) {
                return playerIn;
            }
            // Spigot End

            banner$loc = respawnEvent.getRespawnLocation();
            if (!conqueredEnd) { // keep inventory here since inventory dropped at ServerPlayerEntity#onDeath
                playerIn.reset(); // SPIGOT-4785
            }
        } else {
            if (banner$worldserver == null) banner$worldserver = this.server.getLevel(playerIn.getRespawnDimension());
            banner$loc.setWorld(banner$worldserver.getWorld());
        }
        ServerLevel worldserver1 = ((CraftWorld) banner$loc.getWorld()).getHandle();
        entityplayer1.moveTo(banner$loc.getX(), banner$loc.getY(), banner$loc.getZ(), banner$loc.getYaw(), banner$loc.getPitch());
        entityplayer1.connection.resetPosition();
        // CraftBukkit end

        while (!worldserver1.noCollision((Entity) entityplayer1) && entityplayer1.getY() < (double) worldserver1.getMaxBuildHeight()) {
            entityplayer1.setPos(entityplayer1.getX(), entityplayer1.getY() + 1.0D, entityplayer1.getZ());
        }

        int i = conqueredEnd ? 1 : 0;
        // CraftBukkit start
        LevelData worlddata = worldserver1.getLevelData();
        entityplayer1.connection.send(new ClientboundRespawnPacket(worldserver1.dimensionTypeId(), worldserver1.dimension(), BiomeManager.obfuscateSeed(worldserver1.getSeed()), entityplayer1.gameMode.getGameModeForPlayer(), entityplayer1.gameMode.getPreviousGameModeForPlayer(), worldserver1.isDebug(), worldserver1.isFlat(), (byte) i, entityplayer1.getLastDeathLocation(), entityplayer1.getPortalCooldown()));
        entityplayer1.connection.send(new ClientboundSetChunkCacheRadiusPacket((worldserver1.bridge$spigotConfig().viewDistance)));
        entityplayer1.connection.send(new ClientboundSetSimulationDistancePacket(worldserver1.bridge$spigotConfig().simulationDistance));
        entityplayer1.spawnIn(worldserver1);
        entityplayer1.unsetRemoved();
        entityplayer1.connection.teleport(CraftLocation.toBukkit(entityplayer1.position(), worldserver1.getWorld(), entityplayer1.getYRot(), entityplayer1.getXRot()));
        entityplayer1.setShiftKeyDown(false);

        // entityplayer1.connection.teleport(entityplayer1.getX(), entityplayer1.getY(), entityplayer1.getZ(), entityplayer1.getYRot(), entityplayer1.getXRot());
        entityplayer1.connection.send(new ClientboundSetDefaultSpawnPositionPacket(worldserver1.getSharedSpawnPos(), worldserver1.getSharedSpawnAngle()));
        entityplayer1.connection.send(new ClientboundChangeDifficultyPacket(worlddata.getDifficulty(), worlddata.isDifficultyLocked()));
        entityplayer1.connection.send(new ClientboundSetExperiencePacket(entityplayer1.experienceProgress, entityplayer1.totalExperience, entityplayer1.experienceLevel));
        this.sendLevelInfo(entityplayer1, worldserver1);
        this.sendPlayerPermissionLevel(entityplayer1);
        if (!playerIn.connection.isDisconnected()) {
            worldserver1.addRespawnedPlayer(entityplayer1);
            this.players.add(entityplayer1);
            this.playersByUUID.put(entityplayer1.getUUID(), entityplayer1);
        }
        // Banner start - add for carpet compat
        if (entityplayer_vanilla == null) {
            entityplayer1.initInventoryMenu();
        }
        // Banner end
        entityplayer1.setHealth(entityplayer1.getHealth());
        if (flag2) {
            entityplayer1.connection.send(new ClientboundSoundPacket(SoundEvents.RESPAWN_ANCHOR_DEPLETE, SoundSource.BLOCKS, (double) blockposition.getX(), (double) blockposition.getY(), (double) blockposition.getZ(), 1.0F, 1.0F, worldserver1.getRandom().nextLong()));
        }
        // Added from changeDimension
        sendAllPlayerInfo(playerIn); // Update health, etc...
        playerIn.onUpdateAbilities();
        for (MobEffectInstance mobEffect : playerIn.getActiveEffects()) {
            playerIn.connection.send(new ClientboundUpdateMobEffectPacket(playerIn.getId(), mobEffect));
        }

        // Fire advancement trigger
        playerIn.triggerDimensionChangeTriggers(((CraftWorld) fromWorld).getHandle());

        // Don't fire on respawn
        if (fromWorld != banner$loc.getWorld()) {
            PlayerChangedWorldEvent event = new PlayerChangedWorldEvent(playerIn.getBukkitEntity(), fromWorld);
            Bukkit.getPluginManager().callEvent(event);
        }

        // Save player file again if they were disconnected
        if (playerIn.connection.isDisconnected()) {
            this.save(playerIn);
        }
        // CraftBukkit end
        banner$loc = null;
        banner$respawnReason = null;
        banner$worldserver = null;
        return entityplayer1;
    }

    @Inject(method = "placeNewPlayer", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl;send(Lnet/minecraft/network/protocol/Packet;)V",
            ordinal = 1), locals = LocalCapture.CAPTURE_FAILHARD)
    private void banner$sendSupported(Connection netManager, ServerPlayer player, CallbackInfo ci,
                                      GameProfile gameProfile, GameProfileCache gameProfileCache,
                                      String string, CompoundTag compoundTag, ResourceKey resourceKey,
                                      ServerLevel serverLevel, ServerLevel serverLevel2, String string2,
                                      LevelData levelData, ServerGamePacketListenerImpl serverGamePacketListenerImpl) {
        player.getBukkitEntity().sendSupportedChannels();
    }

    @Inject(method = "sendPlayerPermissionLevel(Lnet/minecraft/server/level/ServerPlayer;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;getCommands()Lnet/minecraft/commands/Commands;"))
    private void banner$calculatePerms(ServerPlayer player, int permLevel, CallbackInfo ci) {
        player.getBukkitEntity().recalculatePermissions();
    }

    @Redirect(method = "sendAllPlayerInfo", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;resetSentInfo()V"))
    private void banner$useScaledHealth(ServerPlayer playerEntity) {
        playerEntity.getBukkitEntity().updateScaledHealth(); // CraftBukkit - Update scaled health on respawn and worldchange
        playerEntity.getEntityData().refresh(playerEntity);// CraftBukkkit - SPIGOT-7218: sync metadata
        int i = playerEntity.level().getGameRules().getBoolean(GameRules.RULE_REDUCEDDEBUGINFO) ? 22 : 23;
        playerEntity.connection.send(new ClientboundEntityEventPacket(playerEntity, (byte) i));
        float immediateRespawn = playerEntity.level().getGameRules().getBoolean(GameRules.RULE_DO_IMMEDIATE_RESPAWN) ? 1.0f : 0.0f;
        playerEntity.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.IMMEDIATE_RESPAWN, immediateRespawn));
    }

    @Override
    public CraftServer getCraftServer() {
        return this.cserver;
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

    @Redirect(method = "tick", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/server/players/PlayerList;broadcastAll(Lnet/minecraft/network/protocol/Packet;)V"))
    private void banner$castMsg(PlayerList instance, Packet<?> packet) {
        // CraftBukkit start
        for (int i = 0; i < this.players.size(); ++i) {
            final ServerPlayer target = (ServerPlayer) this.players.get(i);

            target.connection.send(new ClientboundPlayerInfoUpdatePacket(EnumSet.of(ClientboundPlayerInfoUpdatePacket.Action.UPDATE_LATENCY), this.players.stream().filter(new Predicate<ServerPlayer>() {
                @Override
                public boolean test(ServerPlayer input) {
                    return target.getBukkitEntity().canSee(input.getBukkitEntity());
                }
            }).collect(Collectors.toList())));
            // CraftBukkit end
        }
    }

    /**
     * @author wdog5
     * @reason bukkit
     */
    @Overwrite
    public ServerStatsCounter getPlayerStats(net.minecraft.world.entity.player.Player player) {
        return getPlayerStats((ServerPlayer) player);
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

    /**
     * @author wdog5
     * @reason functionally replaced
     */
    @Overwrite
    public PlayerAdvancements getPlayerAdvancements(ServerPlayer player) {
        UUID uUID = player.getUUID();
        PlayerAdvancements playerAdvancements = (PlayerAdvancements)player.getAdvancements();// CraftBukkit
        if (playerAdvancements == null) {
            Path path = this.server.getWorldPath(LevelResource.PLAYER_ADVANCEMENTS_DIR).resolve("" + uUID + ".json");
            playerAdvancements = new PlayerAdvancements(this.server.getFixerUpper(), ((PlayerList) (Object) this), this.server.getAdvancements(), path, player);
            // this.advancements.put(uUID, playerAdvancements);
        }

        playerAdvancements.setPlayer(player);
        return playerAdvancements;
    }

    /**
     * @author wdog5
     * @reason functionally replaced
     */
    @Overwrite
    public void addWorldborderListener(ServerLevel worldserver) {
        if (playerIo != null) return; // CraftBukkit
        worldserver.getWorldBorder().addListener(new BorderChangeListener() {
            @Override
            public void onBorderSizeSet(WorldBorder worldborder, double d0) {
                ((PlayerList) (Object) this).broadcastAll(new ClientboundSetBorderSizePacket(worldborder), worldborder.bridge$world()); // CraftBukkit
            }

            @Override
            public void onBorderSizeLerping(WorldBorder worldborder, double d0, double d1, long i) {
                ((PlayerList) (Object) this).broadcastAll(new ClientboundSetBorderLerpSizePacket(worldborder), worldborder.bridge$world()); // CraftBukkit
            }

            @Override
            public void onBorderCenterSet(WorldBorder worldborder, double d0, double d1) {
                ((PlayerList) (Object) this).broadcastAll(new ClientboundSetBorderCenterPacket(worldborder), worldborder.bridge$world()); // CraftBukkit
            }

            @Override
            public void onBorderSetWarningTime(WorldBorder worldborder, int i) {
                ((PlayerList) (Object) this).broadcastAll(new ClientboundSetBorderWarningDelayPacket(worldborder), worldborder.bridge$world()); // CraftBukkit
            }

            @Override
            public void onBorderSetWarningBlocks(WorldBorder worldborder, int i) {
                ((PlayerList) (Object) this).broadcastAll(new ClientboundSetBorderWarningDistancePacket(worldborder), worldborder.bridge$world()); // CraftBukkit
            }

            @Override
            public void onBorderSetDamagePerBlock(WorldBorder worldborder, double d0) {}

            @Override
            public void onBorderSetDamageSafeZOne(WorldBorder worldborder, double d0) {}
        });
    }
}
