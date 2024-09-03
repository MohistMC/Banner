package com.mohistmc.banner.mixin.server.players;

import com.llamalad7.mixinextras.injector.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Local;
import com.mohistmc.banner.BannerServer;
import com.mohistmc.banner.bukkit.pluginfix.LuckPerms;
import com.mohistmc.banner.fabric.BukkitRegistry;
import com.mohistmc.banner.injection.server.players.InjectionPlayerList;
import com.mohistmc.banner.util.I18n;
import com.mojang.authlib.GameProfile;
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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.LayeredRegistryAccess;
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
import net.minecraft.network.protocol.game.ClientboundSetDefaultSpawnPositionPacket;
import net.minecraft.network.protocol.game.ClientboundSetExperiencePacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateMobEffectPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.RegistryLayer;
import net.minecraft.server.bossevents.CustomBossEvents;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
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
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.BorderChangeListener;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.PlayerDataStorage;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Location;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.command.ColouredConsoleSender;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.util.CraftChatMessage;
import org.bukkit.craftbukkit.util.CraftLocation;
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
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Banner - TODO fix inject method
@Mixin(PlayerList.class)
public abstract class MixinPlayerList implements InjectionPlayerList {

    @Mutable
    @Shadow @Final public List<ServerPlayer> players;
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
    @Shadow
    public int maxPlayers;

    @Shadow public abstract boolean canBypassPlayerLimit(GameProfile profile);

    @Shadow public abstract void sendLevelInfo(ServerPlayer player, ServerLevel level);

    @Shadow public abstract void sendPlayerPermissionLevel(ServerPlayer player);

    @Shadow public abstract void sendAllPlayerInfo(ServerPlayer player);

    @Shadow @Final public PlayerDataStorage playerIo;
    @Shadow @Final private static Logger LOGGER;
    @Shadow protected abstract void save(ServerPlayer player);

    @Shadow @Final private Map<UUID, ServerStatsCounter> stats;

    @Shadow public abstract ServerPlayer getPlayerForLogin(GameProfile gameProfile, ClientInformation clientInformation);

    @Shadow public abstract void sendActivePlayerEffects(ServerPlayer serverPlayer);

    private CraftServer cserver;

    private static final AtomicReference<String> PROFILE_NAMES = new AtomicReference<>();

    @Inject(method = "<init>", at = @At(value = "FIELD", target = "Lnet/minecraft/server/players/PlayerList;bans:Lnet/minecraft/server/players/UserBanList;"))
    public void banner$init(MinecraftServer minecraftServer, LayeredRegistryAccess<RegistryLayer> layeredRegistryAccess, PlayerDataStorage playerDataStorage, int i, CallbackInfo ci) {
        this.players = new CopyOnWriteArrayList<>();
        minecraftServer.banner$setServer(this.cserver =
                new CraftServer((DedicatedServer) minecraftServer, ((PlayerList) (Object) this)));
        BannerServer.LOGGER.info(I18n.as("registry.begin"));
        BukkitRegistry.registerAll((DedicatedServer) minecraftServer);
        minecraftServer.banner$setConsole(ColouredConsoleSender.getInstance());
    }

    @Inject(method = "placeNewPlayer", at = @At(value = "INVOKE",
            target = "Ljava/util/Optional;flatMap(Ljava/util/function/Function;)Ljava/util/Optional;"))
    public void banner$print(Connection connection, ServerPlayer serverPlayer, CommonListenerCookie commonListenerCookie, CallbackInfo ci, @Local String string, @Local Optional optional) {
        // CraftBukkit start - Better rename detection
        if (optional.isPresent()) {
            CompoundTag nbttagcompound = (CompoundTag) optional.get();
            if (nbttagcompound.contains("bukkit")) {
                CompoundTag bukkit =  nbttagcompound.getCompound("bukkit");
                PROFILE_NAMES.set(bukkit.contains("lastKnownName", 8) ? bukkit.getString("lastKnownName") : string);
            }
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
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerLevel;getLevelData()Lnet/minecraft/world/level/storage/LevelData;"))
    private void banner$callSpawnEvent(Connection connection, ServerPlayer serverPlayer, CommonListenerCookie commonListenerCookie, CallbackInfo ci, @Local(ordinal = 1) ServerLevel serverLevel2) {
        // Spigot start - spawn location event
        org.bukkit.entity.Player spawnPlayer = serverPlayer.getBukkitEntity();
        org.spigotmc.event.player.PlayerSpawnLocationEvent ev = new org.spigotmc.event.player.PlayerSpawnLocationEvent(spawnPlayer, spawnPlayer.getLocation()); // Paper use our duplicate event
        cserver.getPluginManager().callEvent(ev);

        Location loc = ev.getSpawnLocation();
        serverLevel2 = ((CraftWorld) loc.getWorld()).getHandle();

        serverPlayer.spawnIn(serverLevel2);
        serverPlayer.gameMode.setLevel((ServerLevel) serverPlayer.level());
        serverPlayer.absMoveTo(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
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
                    target = "Lnet/minecraft/server/players/PlayerList;broadcastSystemMessage(Lnet/minecraft/network/chat/Component;Z)V")
    )
    private void banner$playerJoin(Connection connection, ServerPlayer serverPlayer, CommonListenerCookie commonListenerCookie, CallbackInfo ci, @Local MutableComponent mutableComponent) {
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
            cancellable = true)
    private void banner$joinEvent(Connection connection, ServerPlayer player, CommonListenerCookie commonListenerCookie, CallbackInfo ci) {
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
        if (banner$joinMsg.get() != null && !banner$joinMsg.get().isEmpty()) {
            for (Component line : CraftChatMessage.fromString(banner$joinMsg.get())) {
                server.getPlayerList().broadcastSystemMessage(line, false);
            }
        }
        // CraftBukkit end

        // CraftBukkit start - sendAll above replaced with this loop
        ClientboundPlayerInfoUpdatePacket packet = ClientboundPlayerInfoUpdatePacket.createPlayerInitializing(List.of(player));

        for (ServerPlayer serverPlayer : this.players) {
            ServerPlayer entityplayer1 = (ServerPlayer) serverPlayer;

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

        player.refreshEntityData(player); // CraftBukkit - BungeeCord#2321, send complete data to self on spawn
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

    public String quitMsg;

    /**
     * @author wdog5
     * @reason bukkit
     */
    @Overwrite
    public void remove(ServerPlayer player) {
        ServerLevel serverLevel = player.serverLevel();
        player.awardStat(Stats.LEAVE_GAME);
        // CraftBukkit start - Quitting must be before we do final save of data, in case plugins need to modify it
        // See SPIGOT-5799, SPIGOT-6145
        if (player.containerMenu != player.inventoryMenu) {
            player.closeContainer();
        }

        PlayerQuitEvent playerQuitEvent = new PlayerQuitEvent(player.getBukkitEntity(), player.bridge$kickLeaveMessage() != null ? player.bridge$kickLeaveMessage() : "\u00A7e" + player.getScoreboardName() + " left the game");
        cserver.getPluginManager().callEvent(playerQuitEvent);
        LuckPerms.perCache.remove(player.getBukkitEntity().getUniqueId());
        player.getBukkitEntity().disconnect(playerQuitEvent.getQuitMessage());
        player.doTick(); // SPIGOT-924
        // CraftBukkit end
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
        ServerPlayer serverPlayer = this.playersByUUID.get(uUID);
        if (serverPlayer == player) {
            this.playersByUUID.remove(uUID);
            // this.stats.remove(uUID);
            // this.advancements.remove(uUID);
        }
        // CraftBukkit start
        ClientboundPlayerInfoRemovePacket packet = new ClientboundPlayerInfoRemovePacket(List.of(serverPlayer.getUUID()));
        for (ServerPlayer entityplayer2 : players) {
            if (entityplayer2.getBukkitEntity().canSee(player.getBukkitEntity())) {
                entityplayer2.connection.send(packet);
            } else {
                entityplayer2.getBukkitEntity().onEntityRemove(player);
            }
        }
        // This removes the scoreboard (and player reference) for the specific player in the manager
        cserver.getScoreboardManager().removePlayer(player.getBukkitEntity());
        // CraftBukkit end
        this.quitMsg = playerQuitEvent.getQuitMessage();
    }

    public String bridge$quiltMsg() {
        return quitMsg;
    }

    @Unique
    public AtomicReference<ServerPlayer> entity = new AtomicReference<>(null);

    @Override
    public ServerPlayer player() {
        return entity.get();
    }

    /**
     * @author Mgazul
     * @reason bukkit
     */
    @Overwrite
    public Component canPlayerLogin(SocketAddress socketaddress, GameProfile gameProfile) {
        ServerPlayer serverPlayer = getPlayerForLogin(gameProfile, ClientInformation.createDefault());
        entity.set(serverPlayer);
        org.bukkit.entity.Player player = serverPlayer.getBukkitEntity();
        String hostname = ((java.net.InetSocketAddress) socketaddress).getHostName() + ":" + ((java.net.InetSocketAddress) socketaddress).getPort();
        PlayerLoginEvent event = new PlayerLoginEvent(player, hostname, ((java.net.InetSocketAddress) socketaddress).getAddress());

        if (getBans().isBanned(gameProfile) && !getBans().get(gameProfile).hasExpired()) {
            UserBanListEntry userbanlistentry = this.bans.get(gameProfile);
            MutableComponent mutablecomponent1 = Component.translatable("multiplayer.disconnect.banned.reason", userbanlistentry.getReason());
            if (userbanlistentry.getExpires() != null) {
                mutablecomponent1.append(Component.translatable("multiplayer.disconnect.banned.expiration", BAN_DATE_FORMAT.format(userbanlistentry.getExpires())));
            }

            event.disallow(PlayerLoginEvent.Result.KICK_BANNED, org.spigotmc.SpigotConfig.whitelistMessage); // Spigot
        } else if (!this.isWhiteListed(gameProfile)) {
            MutableComponent mutablecomponent1 = Component.translatable("multiplayer.disconnect.not_whitelisted");
            event.disallow(PlayerLoginEvent.Result.KICK_WHITELIST, CraftChatMessage.fromComponent(mutablecomponent1));
        } else if (getIpBans().isBanned(socketaddress) && !getIpBans().get(socketaddress).hasExpired()) {
            IpBanListEntry ipbanlistentry = this.ipBans.get(socketaddress);
            MutableComponent mutablecomponent1 = Component.translatable("multiplayer.disconnect.banned_ip.reason", ipbanlistentry.getReason());
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
            return Component.literal(event.getKickMessage());
        }
        // Banner start - TODO
        /*
        if (!LuckPerms.perCache.containsKey(player.getUniqueId())) {
            LuckPerms.perCache.put(player.getUniqueId(), ((CraftPlayer)player).perm);
        }*/
        // Banner end
        return null;
    }

    @Inject(method = "getPlayerForLogin", at = @At("HEAD"), cancellable = true)
    private void banner$getPlayerForLogin(GameProfile gameProfile,
                                          ClientInformation clientInformation,
                                          CallbackInfoReturnable<ServerPlayer> ci) {
        if(entity.get() != null) {
            ci.setReturnValue(entity.getAndSet(null));
        }
    }


    private Location banner$loc = null;
    private transient PlayerRespawnEvent.RespawnReason banner$respawnReason;
    public ServerLevel banner$worldserver = null;
    public AtomicBoolean avoidSuffocation = new AtomicBoolean(true);

    // Banner start - Fix mixin by apoli
    public org.bukkit.World fromWorld;
    public PlayerRespawnEvent respawnEvent;
    public ServerLevel worldserver1;
    public LevelData worlddata;
    public ServerPlayer entityplayer_vanilla;
    // Banner end


    @Override
    public ServerPlayer respawn(ServerPlayer entityplayer, ServerLevel worldserver, boolean flag, Location location, boolean avoidSuffocation, Entity.RemovalReason entity_removalreason, PlayerRespawnEvent.RespawnReason reason) {
        this.banner$loc = location;
        this.banner$worldserver = worldserver;
        this.banner$respawnReason = reason;
        this.avoidSuffocation.set(avoidSuffocation);
        return respawn(entityplayer, flag, null, reason);
    }

    @Override
    public ServerPlayer respawn(ServerPlayer entityplayer, boolean flag, Entity.RemovalReason entity_removalreason, PlayerRespawnEvent.RespawnReason reason) {
        return this.respawn(entityplayer, this.server.getLevel(entityplayer.getRespawnDimension()), flag, null, true, entity_removalreason, reason);
    }

    /**
     * @author wdog5
     * @reason bukkit
     */
    @Overwrite
    public ServerPlayer respawn(ServerPlayer entityplayer, boolean flag, Entity.RemovalReason entity_removalreason) {
        entityplayer.stopRiding(); // CraftBukkit
        this.players.remove(entityplayer);
        entityplayer.serverLevel().removePlayerImmediately(entityplayer, entity_removalreason);
        // CraftBukkit start
        // Banner Start - remain origin code
        DimensionTransition dimensiontransition = entityplayer.findRespawnPositionAndUseSpawnBlock(flag, DimensionTransition.DO_NOTHING);
        ServerLevel worldserver = dimensiontransition.newLevel();
        ServerPlayer entityplayer1 = new ServerPlayer(this.server, worldserver, entityplayer.getGameProfile(), entityplayer.clientInformation());
        // Banner end
        //
        //ServerPlayer entityplayer1 = entityplayer;
        Level fromWorld = entityplayer.level();
        entityplayer.wonGame = false;
        // CraftBukkit end

        entityplayer1.connection = entityplayer.connection;
        entityplayer1.restoreFrom(entityplayer, flag);
        entityplayer1.setId(entityplayer.getId());
        entityplayer1.setMainArm(entityplayer.getMainArm());
        // CraftBukkit - not required, just copies old location into reused entity
        // Banner Start - remain origin code
        if (!dimensiontransition.missingRespawnBlock()) {
            entityplayer1.copyRespawnPosition(entityplayer);
        }
        // Banner end
        // CraftBukkit end

        Iterator iterator = entityplayer.getTags().iterator();

        while (iterator.hasNext()) {
            String s = (String) iterator.next();

            entityplayer1.addTag(s);
        }

        // CraftBukkit start - fire PlayerRespawnEvent
        // DimensionTransition dimensiontransition;
        if (banner$loc == null) {
            dimensiontransition = entityplayer.findRespawnPositionAndUseSpawnBlock(flag, DimensionTransition.DO_NOTHING);

            if (!flag) entityplayer.reset(); // SPIGOT-4785
        } else {
            dimensiontransition = new DimensionTransition(((CraftWorld) banner$loc.getWorld()).getHandle(), CraftLocation.toVec3D(banner$loc), Vec3.ZERO, banner$loc.getYaw(), banner$loc.getPitch(), DimensionTransition.DO_NOTHING);
        }
        entityplayer1.spawnIn(worldserver);
        entityplayer1.unsetRemoved();
        entityplayer1.setShiftKeyDown(false);
        Vec3 vec3d = dimensiontransition.pos();

        entityplayer1.forceSetPositionRotation(vec3d.x, vec3d.y, vec3d.z, dimensiontransition.yRot(), dimensiontransition.xRot());
        // CraftBukkit end
        if (dimensiontransition.missingRespawnBlock()) {
            entityplayer1.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.NO_RESPAWN_BLOCK_AVAILABLE, 0.0F));
            entityplayer1.pushChangeSpawnCause(PlayerSpawnChangeEvent.Cause.RESET);
            entityplayer1.setRespawnPosition(null, null, 0f, false, false, PlayerSpawnChangeEvent.Cause.RESET); // CraftBukkit - SPIGOT-5988: Clear respawn location when obstructed
        }

        int i = flag ? 1 : 0;

        entityplayer1.connection.send(new ClientboundRespawnPacket(entityplayer1.createCommonSpawnInfo(worldserver1), (byte) i));
        entityplayer1.connection.teleport(CraftLocation.toBukkit(entityplayer1.position(), worldserver1.getWorld(), entityplayer1.getYRot(), entityplayer1.getXRot())); // CraftBukkit
        entityplayer1.connection.send(new ClientboundSetDefaultSpawnPositionPacket(worldserver.getSharedSpawnPos(), worldserver.getSharedSpawnAngle()));
        entityplayer1.connection.send(new ClientboundChangeDifficultyPacket(worlddata.getDifficulty(), worlddata.isDifficultyLocked()));
        entityplayer1.connection.send(new ClientboundSetExperiencePacket(entityplayer1.experienceProgress, entityplayer1.totalExperience, entityplayer1.experienceLevel));
        this.sendActivePlayerEffects(entityplayer1);
        this.sendLevelInfo(entityplayer1, worldserver);
        this.sendPlayerPermissionLevel(entityplayer1);
        if (!entityplayer.connection.isDisconnected()) {
            worldserver.addRespawnedPlayer(entityplayer1);
            this.players.add(entityplayer1);
            this.playersByUUID.put(entityplayer1.getUUID(), entityplayer1);
        }
        // entityplayer1.initInventoryMenu();
        entityplayer1.setHealth(entityplayer1.getHealth());
        if (!flag) {
            BlockPos blockposition = BlockPos.containing(dimensiontransition.pos());
            BlockState iblockdata = worldserver.getBlockState(blockposition);

            if (iblockdata.is(Blocks.RESPAWN_ANCHOR)) {
                entityplayer1.connection.send(new ClientboundSoundPacket(SoundEvents.RESPAWN_ANCHOR_DEPLETE, SoundSource.BLOCKS, (double) blockposition.getX(), (double) blockposition.getY(), (double) blockposition.getZ(), 1.0F, 1.0F, worldserver.getRandom().nextLong()));
            }
        }
        // Added from changeDimension
        sendAllPlayerInfo(entityplayer); // Update health, etc...
        entityplayer.onUpdateAbilities();
        for (MobEffectInstance mobEffect : entityplayer.getActiveEffects()) {
            entityplayer.connection.send(new ClientboundUpdateMobEffectPacket(entityplayer.getId(), mobEffect, false)); // blend = false
        }

        // Fire advancement trigger
        entityplayer.triggerDimensionChangeTriggers(worldserver);

        // Don't fire on respawn
        if (fromWorld != worldserver) {
            PlayerChangedWorldEvent event = new PlayerChangedWorldEvent(entityplayer.getBukkitEntity(), fromWorld.getWorld());
            server.bridge$server().getPluginManager().callEvent(event);
        }

        // Save player file again if they were disconnected
        if (entityplayer.connection.isDisconnected()) {
            this.save(entityplayer);
        }
        // CraftBukkit end

        return entityplayer1;
    }

    @Inject(method = "placeNewPlayer", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl;send(Lnet/minecraft/network/protocol/Packet;)V",
            ordinal = 1))
    private void banner$sendSupported(Connection connection, ServerPlayer serverPlayer, CommonListenerCookie commonListenerCookie, CallbackInfo ci) {
        serverPlayer.getBukkitEntity().sendSupportedChannels();
    }

    @Inject(method = "sendPlayerPermissionLevel(Lnet/minecraft/server/level/ServerPlayer;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;getCommands()Lnet/minecraft/commands/Commands;"))
    private void banner$calculatePerms(ServerPlayer player, int permLevel, CallbackInfo ci) {
        player.getBukkitEntity().recalculatePermissions();
    }

    @Redirect(method = "sendAllPlayerInfo", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;resetSentInfo()V"))
    private void banner$useScaledHealth(ServerPlayer playerEntity) {
        playerEntity.getBukkitEntity().updateScaledHealth(); // CraftBukkit - Update scaled health on respawn and worldchange
        playerEntity.refreshEntityData(playerEntity);// CraftBukkkit - SPIGOT-7218: sync metadata
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
            final ServerPlayer target = this.players.get(i);

            target.connection.send(new ClientboundPlayerInfoUpdatePacket(EnumSet.of(ClientboundPlayerInfoUpdatePacket.Action.UPDATE_LATENCY), this.players.stream().filter(new Predicate<>() {
                @Override
                public boolean test(ServerPlayer input) {
                    return target.getBukkitEntity().canSee(input.getBukkitEntity());
                }
            }).collect(Collectors.toList())));
            // CraftBukkit end
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

    @Redirect(method = "sendLevelInfo",
            at = @At(value = "INVOKE",
            target = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl;send(Lnet/minecraft/network/protocol/Packet;)V",
                    ordinal = 3))
    private void banner$cancelSendPacket0(ServerGamePacketListenerImpl instance, Packet<?> packet) { }

    @Redirect(method = "sendLevelInfo",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl;send(Lnet/minecraft/network/protocol/Packet;)V",
                    ordinal = 4))
    private void banner$cancelSendPacket1(ServerGamePacketListenerImpl instance, Packet<?> packet) { }

    @Redirect(method = "sendLevelInfo",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl;send(Lnet/minecraft/network/protocol/Packet;)V",
                    ordinal = 5))
    private void banner$cancelSendPacket2(ServerGamePacketListenerImpl instance, Packet<?> packet) { }

    @Inject(method = "sendLevelInfo",
            at = @At(value = "INVOKE",
            target = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl;send(Lnet/minecraft/network/protocol/Packet;)V",
            ordinal = 3))
    private void banner$setWeatherType(ServerPlayer player, ServerLevel level, CallbackInfo ci) {
        // CraftBukkit start - handle player weather
        player.setPlayerWeather(org.bukkit.WeatherType.DOWNFALL, false);
        player.updateWeather(-level.rainLevel, level.rainLevel, -level.thunderLevel, level.thunderLevel);
    }

    private AtomicReference<ServerPlayer> banner$worldBorderPlayer = new AtomicReference<>();

    @Inject(method = "sendLevelInfo", at = @At("HEAD"))
    private void banner$getWorldBorderPlayer(ServerPlayer player, ServerLevel level, CallbackInfo ci) {
        banner$worldBorderPlayer.set(player);
    }

    @Redirect(method = "sendLevelInfo", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/server/level/ServerLevel;getWorldBorder()Lnet/minecraft/world/level/border/WorldBorder;"))
    private WorldBorder banner$useBukkitWorldBorder(ServerLevel instance) {
        return banner$worldBorderPlayer.get().level().getWorldBorder();
    }

    @Inject(method = "removeAll", at = @At("HEAD"), cancellable = true)
    private void banner$removeSafety(CallbackInfo ci) {
        for (ServerPlayer player : this.players) {
            player.connection.disconnect(CraftChatMessage.fromStringOrEmpty(this.server.bridge$server().getShutdownMessage())); // CraftBukkit - add custom shutdown message
        }
        ci.cancel();
    }
}
