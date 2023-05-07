package com.mohistmc.banner.mixin.server.players;

import com.google.common.collect.Lists;
import com.mohistmc.banner.BannerServer;
import com.mohistmc.banner.bukkit.BukkitCaptures;
import com.mohistmc.banner.injection.server.players.InjectionPlayerList;
import com.mojang.authlib.GameProfile;
import io.izzel.arclight.mixin.Eject;
import net.minecraft.core.BlockPos;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.*;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.RegistryLayer;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.network.ServerLoginPacketListenerImpl;
import net.minecraft.server.players.*;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.ServerStatsCounter;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
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
import org.bukkit.craftbukkit.v1_19_R3.CraftServer;
import org.bukkit.craftbukkit.v1_19_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_19_R3.command.ColouredConsoleSender;
import org.bukkit.craftbukkit.v1_19_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_19_R3.util.CraftChatMessage;
import org.bukkit.entity.Player;
import org.bukkit.event.player.*;
import org.bukkit.plugin.PluginLoadOrder;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.spigotmc.SpigotConfig;
import org.spigotmc.event.player.PlayerSpawnLocationEvent;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.io.File;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;

// Banner - TODO fix inject method
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

    @Shadow @Final private PlayerDataStorage playerIo;

    @Shadow public abstract ServerPlayer getPlayerForLogin(GameProfile profile);

    private CraftServer cserver;
    private static final AtomicReference<String> PROFILE_NAMES = new AtomicReference<>();

    @Inject(method = "<init>", at = @At(value = "TAIL"))
    public void init(MinecraftServer minecraftServer, LayeredRegistryAccess<RegistryLayer> layeredRegistryAccess, PlayerDataStorage playerDataStorage, int i, CallbackInfo ci) {
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

    @Redirect(method = "placeNewPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;getLevel(Lnet/minecraft/resources/ResourceKey;)Lnet/minecraft/server/level/ServerLevel;"))
    private ServerLevel banner$spawnLocationEvent(MinecraftServer minecraftServer, ResourceKey<Level> dimension, Connection netManager, ServerPlayer playerIn) {
        CraftPlayer player = playerIn.getBukkitEntity();
        PlayerSpawnLocationEvent event = new PlayerSpawnLocationEvent(player, player.getLocation());
        cserver.getPluginManager().callEvent(event);
        Location loc = event.getSpawnLocation();
        ServerLevel world = ((CraftWorld) loc.getWorld()).getHandle();
        playerIn.setLevel(world);
        playerIn.absMoveTo(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
        return world;
    }

    @Redirect(method = "placeNewPlayer", at = @At(value = "FIELD", target = "Lnet/minecraft/server/players/PlayerList;viewDistance:I"))
    private int banner$spigotViewDistance(PlayerList playerList, Connection netManager, ServerPlayer playerIn) {
        return playerIn.getLevel().bridge$spigotConfig().viewDistance;
    }

    @Redirect(method = "placeNewPlayer", at = @At(value = "FIELD", target = "Lnet/minecraft/server/players/PlayerList;simulationDistance:I"))
    private int banner$spigotSimDistance(PlayerList instance, Connection netManager, ServerPlayer playerIn) {
        return playerIn.getLevel().bridge$spigotConfig().simulationDistance;
    }

    @Eject(method = "placeNewPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/players/PlayerList;broadcastSystemMessage(Lnet/minecraft/network/chat/Component;Z)V"))
    private void banner$playerJoin(PlayerList playerList, Component component, boolean flag, CallbackInfo ci, Connection netManager, ServerPlayer playerIn) {
        PlayerJoinEvent playerJoinEvent = new PlayerJoinEvent(playerIn.getBukkitEntity(), CraftChatMessage.fromComponent(component));
        this.players.add(playerIn);
        this.playersByUUID.put(playerIn.getUUID(), playerIn);
        this.cserver.getPluginManager().callEvent(playerJoinEvent);
        this.players.remove(playerIn);
        if (!playerIn.connection.connection.isConnected()) {
            ci.cancel();
            return;
        }
        String joinMessage = playerJoinEvent.getJoinMessage();
        if (joinMessage != null && joinMessage.length() > 0) {
            for (Component line : CraftChatMessage.fromString(joinMessage)) {
                this.server.getPlayerList().broadcastSystemMessage(line, flag);
            }
        }
    }

    @Redirect(method = "placeNewPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;addNewPlayer(Lnet/minecraft/server/level/ServerPlayer;)V"))
    private void banner$addNewPlayer(ServerLevel instance, ServerPlayer player) {
        if (player.level == instance && !instance.players().contains(player)) {
            instance.addNewPlayer(player);
        }
    }

    @ModifyVariable(method = "placeNewPlayer", ordinal = 1, at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/server/level/ServerLevel;addNewPlayer(Lnet/minecraft/server/level/ServerPlayer;)V"))
    private ServerLevel banner$handleWorldChanges(ServerLevel value, Connection connection, ServerPlayer player) {
        return player.getLevel();
    }

    @Inject(method = "save", cancellable = true, at = @At("HEAD"))
    private void banner$returnIfNotPersist(ServerPlayer playerIn, CallbackInfo ci) {
        if (!playerIn.bridge$persist()) {
            ci.cancel();
        }
    }

    @Inject(method = "remove", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/players/PlayerList;save(Lnet/minecraft/server/level/ServerPlayer;)V"))
    private void banner$playerQuitPre(ServerPlayer playerIn, CallbackInfo ci) {
        this.remove(playerIn);
    }

    @Override
    public String remove(ServerPlayer playerIn) {
        if (playerIn.inventoryMenu != playerIn.containerMenu) {
            playerIn.getBukkitEntity().closeInventory();
        }
        var quitMessage = BukkitCaptures.getQuitMessage();
        PlayerQuitEvent playerQuitEvent = new PlayerQuitEvent(playerIn.getBukkitEntity(), quitMessage != null ? quitMessage : "\u00A7e" + playerIn.getScoreboardName() + " left the game");
        cserver.getPluginManager().callEvent(playerQuitEvent);
        playerIn.getBukkitEntity().disconnect(playerQuitEvent.getQuitMessage());
        BukkitCaptures.captureQuitMessage(playerQuitEvent.getQuitMessage());
        cserver.getScoreboardManager().removePlayer(playerIn.getBukkitEntity());
        return playerQuitEvent.getQuitMessage(); // CraftBukkit
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
    public ServerPlayer canPlayerLogin(SocketAddress socketAddress, GameProfile gameProfile, ServerLoginPacketListenerImpl handler) {
        UUID uuid = UUIDUtil.getOrCreatePlayerUUID(gameProfile);
        List<ServerPlayer> list = Lists.newArrayList();
        for (ServerPlayer entityplayer : this.players) {
            if (entityplayer.getUUID().equals(uuid)) {
                list.add(entityplayer);
            }
        }
        for (ServerPlayer entityplayer : list) {
            this.save(entityplayer);
            entityplayer.connection.disconnect(Component.translatable("multiplayer.disconnect.duplicate_login"));
        }
        ServerPlayer entity = new ServerPlayer(this.server, this.server.getLevel(Level.OVERWORLD), gameProfile);
        Player player = entity.getBukkitEntity();

        String hostname = handler == null ? "" : handler.connection.bridge$hostname();

        PlayerLoginEvent event = new PlayerLoginEvent(player, hostname, ((InetSocketAddress) socketAddress).getAddress());
        if (this.getBans().isBanned(gameProfile) && !this.getBans().get(gameProfile).hasExpired()) {
            UserBanListEntry gameprofilebanentry = this.bans.get(gameProfile);
            var chatmessage = Component.translatable("multiplayer.disconnect.banned.reason", gameprofilebanentry.getReason());
            if (gameprofilebanentry.getExpires() != null) {
                chatmessage.append(Component.translatable("multiplayer.disconnect.banned.expiration", BAN_DATE_FORMAT.format(gameprofilebanentry.getExpires())));
            }
            event.disallow(PlayerLoginEvent.Result.KICK_BANNED, CraftChatMessage.fromComponent(chatmessage));
        } else if (!this.isWhiteListed(gameProfile)) {
            event.disallow(PlayerLoginEvent.Result.KICK_WHITELIST, SpigotConfig.whitelistMessage);
        } else if (this.getIpBans().isBanned(socketAddress) && !this.getIpBans().get(socketAddress).hasExpired()) {
            IpBanListEntry ipbanentry = this.ipBans.get(socketAddress);
            var chatmessage = Component.translatable("multiplayer.disconnect.banned_ip.reason", ipbanentry.getReason());
            if (ipbanentry.getExpires() != null) {
                chatmessage.append(Component.translatable("multiplayer.disconnect.banned_ip.expiration", BAN_DATE_FORMAT.format(ipbanentry.getExpires())));
            }
            event.disallow(PlayerLoginEvent.Result.KICK_BANNED, CraftChatMessage.fromComponent(chatmessage));
        } else if (this.players.size() >= this.maxPlayers && !this.canBypassPlayerLimit(gameProfile)) {
            event.disallow(PlayerLoginEvent.Result.KICK_FULL, SpigotConfig.serverFullMessage);
        }
        this.cserver.getPluginManager().callEvent(event);
        if (event.getResult() != PlayerLoginEvent.Result.ALLOWED) {
            if (handler != null) {
                handler.disconnect(CraftChatMessage.fromStringOrNull(event.getKickMessage()));
            }
            return null;
        }
        return entity;
    }

    @Override
    public ServerPlayer respawn(ServerPlayer playerIn, ServerLevel worldIn, boolean flag, Location location, boolean avoidSuffocation, PlayerRespawnEvent.RespawnReason respawnReason) {
        playerIn.stopRiding();
        this.players.remove(playerIn);
        playerIn.getLevel().removePlayerImmediately(playerIn, Entity.RemovalReason.DISCARDED);
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
        playerIn.connection.send(new ClientboundRespawnPacket(serverWorld.dimensionTypeId(), serverWorld.dimension(), BiomeManager.obfuscateSeed(serverWorld.getSeed()), playerIn.gameMode.getGameModeForPlayer(), playerIn.gameMode.getPreviousGameModeForPlayer(), serverWorld.isDebug(), serverWorld.isFlat(), (byte) (flag ? 1 : 0), playerIn.getLastDeathLocation()));
        playerIn.connection.send(new ClientboundSetChunkCacheRadiusPacket((serverWorld.bridge$spigotConfig().viewDistance)));
        playerIn.connection.send(new ClientboundSetSimulationDistancePacket(serverWorld.bridge$spigotConfig().simulationDistance));
        playerIn.setLevel(serverWorld);
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
        playerIn.getLevel().removePlayerImmediately(playerIn, Entity.RemovalReason.DISCARDED);
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

        LevelData iworldinfo = serverplayerentity.level.getLevelData();
        serverplayerentity.connection.send(new ClientboundRespawnPacket(serverplayerentity.level.dimensionTypeId(), serverplayerentity.level.dimension(), BiomeManager.obfuscateSeed(serverplayerentity.getLevel().getSeed()), serverplayerentity.gameMode.getGameModeForPlayer(), serverplayerentity.gameMode.getPreviousGameModeForPlayer(), serverplayerentity.getLevel().isDebug(), serverplayerentity.getLevel().isFlat(), (byte) (conqueredEnd ? 1 : 0), serverplayerentity.getLastDeathLocation()));
        serverplayerentity.connection.send(new ClientboundSetChunkCacheRadiusPacket(serverWorld.bridge$spigotConfig().viewDistance));
        serverplayerentity.connection.send(new ClientboundSetSimulationDistancePacket(serverWorld.bridge$spigotConfig().simulationDistance));
        serverplayerentity.setLevel(serverWorld);
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

    @Inject(method = "sendPlayerPermissionLevel(Lnet/minecraft/server/level/ServerPlayer;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;getCommands()Lnet/minecraft/commands/Commands;"))
    private void banner$calculatePerms(ServerPlayer player, int permLevel, CallbackInfo ci) {
        player.getBukkitEntity().recalculatePermissions();
    }

    @Redirect(method = "sendAllPlayerInfo", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;resetSentInfo()V"))
    private void banner$useScaledHealth(ServerPlayer playerEntity) {
        playerEntity.getBukkitEntity().updateScaledHealth();
        playerEntity.getEntityData().refresh(playerEntity);
        int i = playerEntity.level.getGameRules().getBoolean(GameRules.RULE_REDUCEDDEBUGINFO) ? 22 : 23;
        playerEntity.connection.send(new ClientboundEntityEventPacket(playerEntity, (byte) i));
        float immediateRespawn = playerEntity.level.getGameRules().getBoolean(GameRules.RULE_DO_IMMEDIATE_RESPAWN) ? 1.0f : 0.0f;
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
