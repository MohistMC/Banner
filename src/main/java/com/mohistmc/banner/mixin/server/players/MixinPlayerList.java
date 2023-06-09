package com.mohistmc.banner.mixin.server.players;

import com.google.common.collect.Lists;
import com.llamalad7.mixinextras.injector.WrapWithCondition;
import com.mohistmc.banner.fabric.FabricInjectBukkit;
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
import net.minecraft.network.protocol.game.*;
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
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.ServerStatsCounter;
import net.minecraft.stats.Stats;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
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
import org.bukkit.entity.Player;
import org.bukkit.event.player.*;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.*;
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
        FabricInjectBukkit.init();
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
        org.spigotmc.event.player.PlayerSpawnLocationEvent ev = new org.spigotmc.event.player.PlayerSpawnLocationEvent(spawnPlayer, spawnPlayer.getLocation());
        cserver.getPluginManager().callEvent(ev);

        Location loc = ev.getSpawnLocation();
        serverLevel2 = ((CraftWorld) loc.getWorld()).getHandle();

        player.spawnIn(serverLevel2);
        player.gameMode.setLevel((ServerLevel) player.level());
        player.absMoveTo(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
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

    private AtomicReference<ServerLevel> banner$level = new AtomicReference<>();

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

    private final AtomicReference<ServerPlayer> banner$savePlayer = new AtomicReference<>();

    @Inject(method = "save", at = @At("HEAD"), cancellable = true)
    private void banner$setPlayerSaved(ServerPlayer player, CallbackInfo ci) {
        if (!player.getBukkitEntity().isPersistent() || player.connection == null) {
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
    public ServerPlayer respawn(ServerPlayer entityplayer, boolean flag, PlayerRespawnEvent.RespawnReason reason) {
        return this.respawn(entityplayer, this.server.getLevel(entityplayer.getRespawnDimension()), flag, null, true, reason);
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

    @Override
    public ServerPlayer respawn(ServerPlayer playerIn, ServerLevel worldIn, boolean flag, Location location, boolean avoidSuffocation, PlayerRespawnEvent.RespawnReason respawnReason) {
        playerIn.stopRiding();
        this.players.remove(playerIn);
        playerIn.serverLevel().removePlayerImmediately(playerIn, Entity.RemovalReason.DISCARDED);
        BlockPos pos = playerIn.getRespawnPosition();
        float f = playerIn.getRespawnAngle();
        boolean flag2 = playerIn.isRespawnForced();
        org.bukkit.World fromWorld = playerIn.getBukkitEntity().getWorld();
        playerIn.wonGame = false;
        /*
        playerIn.copyFrom(playerIn, flag);
        playerIn.setEntityId(playerIn.getEntityId());
        playerIn.setPrimaryHand(playerIn.getPrimaryHand());
        for (String s : playerIn.getTags()) {
            playerIn.addTag(s);
        }
        */
        boolean flag3 = false;
        if (location == null) {
            boolean isBedSpawn = false;
            ServerLevel spawnWorld = this.server.getLevel(playerIn.getRespawnDimension());
            if (spawnWorld != null) {
                Optional<Vec3> optional;
                if (pos != null) {
                    optional = net.minecraft.world.entity.player.Player.findRespawnPositionAndUseSpawnBlock(spawnWorld, pos, f, flag2, flag);
                } else {
                    optional = Optional.empty();
                }
                if (optional.isPresent()) {
                    BlockState iblockdata = spawnWorld.getBlockState(pos);
                    boolean flag4 = iblockdata.is(Blocks.RESPAWN_ANCHOR);
                    Vec3 vec3d = optional.get();
                    float f2;
                    if (!iblockdata.is(BlockTags.BEDS) && !flag4) {
                        f2 = f;
                    } else {
                        Vec3 vec3d2 = Vec3.atBottomCenterOf(pos).subtract(vec3d).normalize();
                        f2 = (float) Mth.wrapDegrees(Mth.atan2(vec3d2.z, vec3d2.x) * 57.2957763671875 - 90.0);
                    }
                    // playerIn.setLocationAndAngles(vec3d.x, vec3d.y, vec3d.z, f2, 0.0f);
                    playerIn.setRespawnPosition(spawnWorld.dimension(), pos, f2, flag2, false);
                    flag3 = (!flag && flag4);
                    isBedSpawn = true;
                    location = new Location(spawnWorld.getWorld(), vec3d.x, vec3d.y, vec3d.z);
                } else if (pos != null) {
                    playerIn.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.NO_RESPAWN_BLOCK_AVAILABLE, 0.0f));
                    playerIn.setRespawnPosition(Level.OVERWORLD, null, 0f, false, false); // CraftBukkit - SPIGOT-5988: Clear respawn location when obstructed
                }
            }
            if (location == null) {
                spawnWorld = this.server.getLevel(Level.OVERWORLD);
                pos =  playerIn.getSpawnPoint(spawnWorld);
                location = new Location(spawnWorld.getWorld(), pos.getX() + 0.5f, pos.getY() + 0.1f, pos.getZ() + 0.5f);
            }
            Player respawnPlayer = playerIn.getBukkitEntity();
            PlayerRespawnEvent respawnEvent = new PlayerRespawnEvent(respawnPlayer, location, isBedSpawn && !flag3, flag3, respawnReason);
            this.cserver.getPluginManager().callEvent(respawnEvent);
            if (playerIn.connection.isDisconnected()) {
                return playerIn;
            }
            location = respawnEvent.getRespawnLocation();
            if (!flag) {
                playerIn.reset();
            }
        } else {
            location.setWorld(worldIn.getWorld());
        }
        ServerLevel serverWorld = ((CraftWorld) location.getWorld()).getHandle();
        playerIn.moveTo(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
        playerIn.connection.resetPosition();
        while (avoidSuffocation && !serverWorld.noCollision(playerIn) && playerIn.getY() < serverWorld.getMaxBuildHeight()) {
            playerIn.setPos(playerIn.getX(), playerIn.getY() + 1.0, playerIn.getZ());
        }
        LevelData worlddata = serverWorld.getLevelData();
        byte b = (byte) (flag ? 1 : 0);
        playerIn.connection.send(new ClientboundRespawnPacket(playerIn.level().dimensionTypeId(), playerIn.level().dimension(), BiomeManager.obfuscateSeed(playerIn.serverLevel().getSeed()), playerIn.gameMode.getGameModeForPlayer(), playerIn.gameMode.getPreviousGameModeForPlayer(), playerIn.level().isDebug(), playerIn.serverLevel().isFlat(), (byte)b, playerIn.getLastDeathLocation(), playerIn.getPortalCooldown()));
        playerIn.connection.send(new ClientboundSetChunkCacheRadiusPacket((serverWorld.bridge$spigotConfig().viewDistance)));
        playerIn.connection.send(new ClientboundSetSimulationDistancePacket(serverWorld.bridge$spigotConfig().simulationDistance));
        playerIn.setServerLevel(serverWorld);
        playerIn.connection.teleport(new Location(serverWorld.getWorld(), playerIn.getX(), playerIn.getY(), playerIn.getZ(), playerIn.getYRot(), playerIn.getXRot()));
        playerIn.setShiftKeyDown(false);
        playerIn.connection.send(new ClientboundSetDefaultSpawnPositionPacket(serverWorld.getSharedSpawnPos(), serverWorld.getSharedSpawnAngle()));
        playerIn.connection.send(new ClientboundChangeDifficultyPacket(worlddata.getDifficulty(), worlddata.isDifficultyLocked()));
        playerIn.connection.send(new ClientboundSetExperiencePacket(playerIn.experienceProgress, playerIn.totalExperience, playerIn.experienceLevel));
        this.sendLevelInfo(playerIn, serverWorld);
        this.sendPlayerPermissionLevel(playerIn);
        if (!playerIn.connection.isDisconnected()) {
            serverWorld.addRespawnedPlayer(playerIn);
            this.players.add(playerIn);
            this.playersByUUID.put(playerIn.getUUID(), playerIn);
        }
        playerIn.setHealth(playerIn.getHealth());
        if (flag3) {
            playerIn.connection.send(new ClientboundSoundPacket(SoundEvents.RESPAWN_ANCHOR_DEPLETE, SoundSource.BLOCKS, pos.getX(), pos.getY(), pos.getZ(), 1.0f, 1.0f, serverWorld.random.nextLong()));
        }
        this.sendAllPlayerInfo(playerIn);
        playerIn.onUpdateAbilities();
        for (Object o1 : playerIn.getActiveEffects()) {
            MobEffectInstance mobEffect = (MobEffectInstance) o1;
            playerIn.connection.send(new ClientboundUpdateMobEffectPacket(playerIn.getId(), mobEffect));
        }
        playerIn.triggerDimensionChangeTriggers(((CraftWorld) fromWorld).getHandle());
        if (fromWorld != location.getWorld()) {
            PlayerChangedWorldEvent event = new PlayerChangedWorldEvent(playerIn.getBukkitEntity(), fromWorld);
            Bukkit.getPluginManager().callEvent(event);
        }
        if (playerIn.connection.isDisconnected()) {
            this.save(playerIn);
        }
        return playerIn;
    }

    private transient Location banner$loc;
    private transient Boolean banner$suffo;
    private transient PlayerRespawnEvent.RespawnReason banner$respawnReason;

    /**
     * @author wdog5
     * @reason
     */
    @Overwrite
    public ServerPlayer respawn(ServerPlayer playerIn, boolean conqueredEnd) {
        Location location = banner$loc;
        banner$loc = null;
        boolean avoidSuffocation = banner$suffo == null || banner$suffo;
        banner$suffo = null;
        var respawnReason = banner$respawnReason == null ? PlayerRespawnEvent.RespawnReason.DEATH : banner$respawnReason;
        banner$respawnReason = null;
        playerIn.stopRiding();
        this.players.remove(playerIn);
        playerIn.serverLevel().removePlayerImmediately(playerIn, Entity.RemovalReason.DISCARDED);
        BlockPos pos = playerIn.getRespawnPosition();
        float f = playerIn.getRespawnAngle();
        boolean flag2 = playerIn.isRespawnForced();

        org.bukkit.World fromWorld = playerIn.getBukkitEntity().getWorld();
        playerIn.wonGame = false;

        boolean flag3 = false;
        ServerLevel spawnWorld = this.server.getLevel(playerIn.getRespawnDimension());
        if (location == null) {
            boolean isBedSpawn = false;
            if (spawnWorld != null) {
                Optional<Vec3> optional;
                if (pos != null) {
                    optional = net.minecraft.world.entity.player.Player.findRespawnPositionAndUseSpawnBlock(spawnWorld, pos, f, flag2, conqueredEnd);
                } else {
                    optional = Optional.empty();
                }
                if (optional.isPresent()) {
                    BlockState iblockdata = spawnWorld.getBlockState(pos);
                    boolean flag4 = iblockdata.is(Blocks.RESPAWN_ANCHOR);
                    Vec3 vec3d = optional.get();
                    float f2;
                    if (!iblockdata.is(BlockTags.BEDS) && !flag4) {
                        f2 = f;
                    } else {
                        Vec3 vec3d2 = Vec3.atBottomCenterOf(pos).subtract(vec3d).normalize();
                        f2 = (float) Mth.wrapDegrees(Mth.atan2(vec3d2.z, vec3d2.x) * 57.2957763671875 - 90.0);
                    }
                    // playerIn.setLocationAndAngles(vec3d.x, vec3d.y, vec3d.z, f2, 0.0f);
                    playerIn.setRespawnPosition(spawnWorld.dimension(), pos, f2, flag2, false);
                    flag3 = (!flag2 && flag4);
                    isBedSpawn = true;
                    location = new Location(spawnWorld.getWorld(), vec3d.x, vec3d.y, vec3d.z);
                } else if (pos != null) {
                    playerIn.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.NO_RESPAWN_BLOCK_AVAILABLE, 0.0f));
                    playerIn.setRespawnPosition(Level.OVERWORLD, null, 0f, false, false);
                }
            }
            if (location == null) {
                spawnWorld = this.server.getLevel(Level.OVERWORLD);
                pos = playerIn.getSpawnPoint(spawnWorld);
                location = new Location(spawnWorld.getWorld(), pos.getX() + 0.5f, pos.getY() + 0.1f, pos.getZ() + 0.5f);
            }
            Player respawnPlayer = playerIn.getBukkitEntity();
            PlayerRespawnEvent respawnEvent = new PlayerRespawnEvent(respawnPlayer, location, isBedSpawn && !flag3, flag3, respawnReason);
            this.cserver.getPluginManager().callEvent(respawnEvent);
            if (playerIn.connection.isDisconnected()) {
                return playerIn;
            }
            location = respawnEvent.getRespawnLocation();
            if (!conqueredEnd) {
                playerIn.reset();
            }
        } else {
            location.setWorld(spawnWorld.getWorld());
        }

        ServerLevel serverWorld = ((CraftWorld) location.getWorld()).getHandle();

        ServerPlayer serverplayerentity = new ServerPlayer(this.server, serverWorld, playerIn.getGameProfile());

        // Forward to new player instance
        if ((Object) playerIn instanceof Mob) {
            ((Mob) (Object) playerIn).dropLeash(true, false);
        }
        playerIn.connection.player = serverplayerentity;

        serverplayerentity.connection = playerIn.connection;
        serverplayerentity.restoreFrom(playerIn, conqueredEnd);
        serverplayerentity.setRespawnPosition(playerIn.getRespawnDimension(), playerIn.getRespawnPosition(),
                playerIn.getRespawnAngle(), playerIn.isRespawnForced(), false);
        if (!conqueredEnd) {  // keep inventory here since inventory dropped at ServerPlayerEntity#onDeath
            serverplayerentity.getInventory().replaceWith(playerIn.getInventory());
        }
        serverplayerentity.setId(playerIn.getId());
        serverplayerentity.setMainArm(playerIn.getMainArm());

        for (String s : playerIn.getTags()) {
            serverplayerentity.addTag(s);
        }

        serverplayerentity.moveTo(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
        serverplayerentity.connection.resetPosition();

        while (avoidSuffocation && !serverWorld.noCollision(serverplayerentity) && serverplayerentity.getY() < serverWorld.getMaxBuildHeight()) {
            serverplayerentity.setPos(serverplayerentity.getX(), serverplayerentity.getY() + 1.0D, serverplayerentity.getZ());
        }

        LevelData iworldinfo = serverplayerentity.level().getLevelData();
        byte b = (byte) (conqueredEnd ? 1 : 0);
        playerIn.connection.send(new ClientboundRespawnPacket(playerIn.level().dimensionTypeId(), playerIn.level().dimension(), BiomeManager.obfuscateSeed(playerIn.serverLevel().getSeed()), playerIn.gameMode.getGameModeForPlayer(), playerIn.gameMode.getPreviousGameModeForPlayer(), playerIn.level().isDebug(), playerIn.serverLevel().isFlat(), (byte)b, playerIn.getLastDeathLocation(), playerIn.getPortalCooldown()));
        serverplayerentity.connection.send(new ClientboundSetChunkCacheRadiusPacket(serverWorld.bridge$spigotConfig().viewDistance));
        serverplayerentity.connection.send(new ClientboundSetSimulationDistancePacket(serverWorld.bridge$spigotConfig().simulationDistance));
        serverplayerentity.setServerLevel(serverWorld);
        serverplayerentity.connection.teleport(new Location(serverWorld.getWorld(), serverplayerentity.getX(), serverplayerentity.getY(), serverplayerentity.getZ(), serverplayerentity.getYRot(), serverplayerentity.getXRot()));
        serverplayerentity.setShiftKeyDown(false);
        serverplayerentity.connection.send(new ClientboundSetDefaultSpawnPositionPacket(serverWorld.getSharedSpawnPos(), serverWorld.getSharedSpawnAngle()));
        serverplayerentity.connection.send(new ClientboundChangeDifficultyPacket(iworldinfo.getDifficulty(), iworldinfo.isDifficultyLocked()));
        serverplayerentity.connection.send(new ClientboundSetExperiencePacket(serverplayerentity.experienceProgress, serverplayerentity.totalExperience, serverplayerentity.experienceLevel));
        this.sendLevelInfo(serverplayerentity, serverWorld);
        this.sendPlayerPermissionLevel(serverplayerentity);
        if (!serverplayerentity.connection.isDisconnected()) {
            serverWorld.addRespawnedPlayer(serverplayerentity);
            this.players.add(serverplayerentity);
            this.playersByUUID.put(serverplayerentity.getUUID(), serverplayerentity);
        }
        serverplayerentity.initInventoryMenu();
        serverplayerentity.setHealth(serverplayerentity.getHealth());
        if (flag2) {
            serverplayerentity.connection.send(new ClientboundSoundPacket(SoundEvents.RESPAWN_ANCHOR_DEPLETE, SoundSource.BLOCKS, (double) pos.getX(), (double) pos.getY(), (double) pos.getZ(), 1.0F, 1.0F, serverWorld.random.nextLong()));
        }
        this.sendAllPlayerInfo(serverplayerentity);
        serverplayerentity.onUpdateAbilities();
        for (Object o1 : serverplayerentity.getActiveEffects()) {
            MobEffectInstance mobEffect = (MobEffectInstance) o1;
            serverplayerentity.connection.send(new ClientboundUpdateMobEffectPacket(serverplayerentity.getId(), mobEffect));
        }
        serverplayerentity.triggerDimensionChangeTriggers(((CraftWorld) fromWorld).getHandle());
        if (fromWorld != location.getWorld()) {
            PlayerChangedWorldEvent event = new PlayerChangedWorldEvent(serverplayerentity.getBukkitEntity(), fromWorld);
            Bukkit.getPluginManager().callEvent(event);
        }
        if (serverplayerentity.connection.isDisconnected()) {
            this.save(serverplayerentity);
        }
        return serverplayerentity;
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

}
