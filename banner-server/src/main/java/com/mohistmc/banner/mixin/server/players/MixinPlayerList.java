package com.mohistmc.banner.mixin.server.players;

import com.llamalad7.mixinextras.sugar.Local;
import com.mohistmc.banner.BannerMod;
import com.mohistmc.banner.bukkit.BukkitSnapshotCaptures;
import com.mohistmc.banner.fabric.BukkitRegistry;
import com.mohistmc.banner.injection.server.players.InjectionPlayerList;
import com.mohistmc.banner.util.Blackhole;
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

import io.izzel.arclight.mixin.Decorate;
import io.izzel.arclight.mixin.DecorationOps;
import io.izzel.arclight.mixin.Eject;
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
import net.minecraft.network.protocol.game.ClientboundSetChunkCacheRadiusPacket;
import net.minecraft.network.protocol.game.ClientboundSetDefaultSpawnPositionPacket;
import net.minecraft.network.protocol.game.ClientboundSetExperiencePacket;
import net.minecraft.network.protocol.game.ClientboundSetSimulationDistancePacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.RegistryLayer;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
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
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
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
import org.spigotmc.event.player.PlayerSpawnLocationEvent;
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
    @Shadow public abstract void sendAllPlayerInfo(ServerPlayer player);

    @Shadow @Final public PlayerDataStorage playerIo;
    @Shadow protected abstract void save(ServerPlayer player);

    @Shadow @Final private Map<UUID, ServerStatsCounter> stats;

    @Shadow public abstract ServerPlayer getPlayerForLogin(GameProfile gameProfile, ClientInformation clientInformation);

    @Shadow public abstract ServerPlayer respawn(ServerPlayer serverPlayer, boolean bl, Entity.RemovalReason removalReason);

    @Shadow public abstract void sendActivePlayerEffects(ServerPlayer serverPlayer);

    @Shadow public abstract void sendLevelInfo(ServerPlayer serverPlayer, ServerLevel serverLevel);

    @Shadow public abstract void sendPlayerPermissionLevel(ServerPlayer serverPlayer);

    private CraftServer cserver;

    @Inject(method = "<init>", at = @At(value = "FIELD", target = "Lnet/minecraft/server/players/PlayerList;bans:Lnet/minecraft/server/players/UserBanList;"))
    public void banner$init(MinecraftServer minecraftServer, LayeredRegistryAccess<RegistryLayer> layeredRegistryAccess, PlayerDataStorage playerDataStorage, int i, CallbackInfo ci) {
        this.players = new CopyOnWriteArrayList<>();
        minecraftServer.banner$setServer(this.cserver =
                new CraftServer((DedicatedServer) minecraftServer, ((PlayerList) (Object) this)));
        BannerMod.LOGGER.info(I18n.as("registry.begin"));
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
                string = bukkit.contains("lastKnownName", 8) ? bukkit.getString("lastKnownName") : string;
            }
        }
    }

    @Redirect(method = "placeNewPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;getLevel(Lnet/minecraft/resources/ResourceKey;)Lnet/minecraft/server/level/ServerLevel;"))
    private ServerLevel banner$spawnLocationEvent(MinecraftServer minecraftServer, ResourceKey<Level> dimension, Connection netManager, ServerPlayer playerIn) {
        CraftPlayer player =  playerIn.getBukkitEntity();
        PlayerSpawnLocationEvent event = new PlayerSpawnLocationEvent(player, player.getLocation());
        cserver.getPluginManager().callEvent(event);
        Location loc = event.getSpawnLocation();
        ServerLevel world = ((CraftWorld) loc.getWorld()).getHandle();
        playerIn.setServerLevel(world);
        playerIn.absMoveTo(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
        return world;
    }

    @Redirect(method = "placeNewPlayer", at = @At(value = "FIELD", target = "Lnet/minecraft/server/players/PlayerList;viewDistance:I"))
    private int banner$spigotViewDistance(PlayerList playerList, Connection netManager, ServerPlayer playerIn) {
        return playerIn.serverLevel().bridge$spigotConfig().viewDistance;
    }

    @Redirect(method = "placeNewPlayer", at = @At(value = "FIELD", target = "Lnet/minecraft/server/players/PlayerList;simulationDistance:I"))
    private int banner$spigotSimDistance(PlayerList instance, Connection netManager, ServerPlayer playerIn) {
        return playerIn.serverLevel().bridge$spigotConfig().simulationDistance;
    }

    @Eject(method = "placeNewPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/players/PlayerList;broadcastSystemMessage(Lnet/minecraft/network/chat/Component;Z)V"))
    private void banner$playerJoin(PlayerList playerList, Component component, boolean flag, CallbackInfo ci, Connection netManager, ServerPlayer playerIn) {
        PlayerJoinEvent playerJoinEvent = new PlayerJoinEvent(playerIn.getBukkitEntity(), CraftChatMessage.fromComponent(component));
        this.players.add(playerIn);
        this.playersByUUID.put(playerIn.getUUID(), playerIn);
        this.cserver.getPluginManager().callEvent(playerJoinEvent);
        this.players.remove(playerIn);
        if (!playerIn.connection.isAcceptingMessages()) {
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
        if (player.level() == instance && !instance.players().contains(player)) {
            instance.addNewPlayer(player);
        }
    }

    @ModifyVariable(method = "placeNewPlayer", ordinal = 1, at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/server/level/ServerLevel;addNewPlayer(Lnet/minecraft/server/level/ServerPlayer;)V"))
    private ServerLevel banner$handleWorldChanges(ServerLevel value, Connection connection, ServerPlayer player) {
        return player.serverLevel();
    }

    @Inject(method = "save", cancellable = true, at = @At("HEAD"))
    private void banner$returnIfNotPersist(ServerPlayer playerIn, CallbackInfo ci) {
        if (!playerIn.bridge$persist()) {
            ci.cancel();
        }
    }

    public String quitMsg;

    @Inject(method = "remove", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/players/PlayerList;save(Lnet/minecraft/server/level/ServerPlayer;)V"))
    private void banner$playerQuitPre(ServerPlayer playerIn, CallbackInfo ci) {
        if (playerIn.inventoryMenu != playerIn.containerMenu) {
             playerIn.getBukkitEntity().closeInventory();
        }
        var quitMessage = BukkitSnapshotCaptures.getQuitMessage();
        quitMsg = quitMessage;
        PlayerQuitEvent playerQuitEvent = new PlayerQuitEvent(playerIn.getBukkitEntity(), quitMessage != null ? quitMessage : "\u00A7e" + playerIn.getScoreboardName() + " left the game");
        cserver.getPluginManager().callEvent(playerQuitEvent);
        playerIn.getBukkitEntity().disconnect(playerQuitEvent.getQuitMessage());
        // playerIn.doTick();
        BukkitSnapshotCaptures.captureQuitMessage(playerQuitEvent.getQuitMessage());
        cserver.getScoreboardManager().removePlayer(playerIn.getBukkitEntity());
    }

    public String bridge$quiltMsg() {
        return quitMsg;
    }

    /**
     * @author Mgazul
     * @reason bukkit
     */
    @Overwrite
    public Component canPlayerLogin(SocketAddress socketaddress, GameProfile gameProfile) {
        ServerPlayer serverPlayer = getPlayerForLogin(gameProfile, ClientInformation.createDefault());
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

    private Location banner$loc = null;
    private transient PlayerRespawnEvent.RespawnReason banner$respawnReason;
    public ServerLevel banner$worldserver = null;
    public AtomicBoolean avoidSuffocation = new AtomicBoolean(true);

    @Inject(method = "respawn", at = @At("HEAD"))
    private void banner$stopRiding(ServerPlayer serverPlayer, boolean bl, Entity.RemovalReason removalReason, CallbackInfoReturnable<ServerPlayer> cir) {
        serverPlayer.stopRiding();
    }

    @Decorate(method = "respawn", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;findRespawnPositionAndUseSpawnBlock(ZLnet/minecraft/world/level/portal/DimensionTransition$PostDimensionTransition;)Lnet/minecraft/world/level/portal/DimensionTransition;"))
    private DimensionTransition banner$respawnPoint(ServerPlayer instance, boolean bl, DimensionTransition.PostDimensionTransition postDimensionTransition) throws Throwable {
        var location = banner$loc;
        var respawnReason = banner$respawnReason == null ? PlayerRespawnEvent.RespawnReason.DEATH : banner$respawnReason;
        DimensionTransition dimensiontransition;
        if (location == null) {
            //instance.pushRespawnReason(respawnReason);
            dimensiontransition = (DimensionTransition) DecorationOps.callsite().invoke(instance, bl, postDimensionTransition);
        } else {
            dimensiontransition = new DimensionTransition(((CraftWorld) location.getWorld()).getHandle(), CraftLocation.toVec3D(location), Vec3.ZERO, location.getYaw(), location.getPitch(), DimensionTransition.DO_NOTHING);
        }
        if (dimensiontransition == null) {
            // Banner start - fix #321
            instance.unsetRemoved();
            instance.serverLevel().addRespawnedPlayer(instance);
            this.players.add(instance);
            // Banner end

            return (DimensionTransition) DecorationOps.cancel().invoke(instance);
        }
        return dimensiontransition;
    }

    @Decorate(method = "respawn", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;restoreFrom(Lnet/minecraft/server/level/ServerPlayer;Z)V"))
    private void banner$restoreInv(ServerPlayer newPlayer, ServerPlayer oldPlayer, boolean bl, ServerPlayer serverPlayer, boolean conqueredEnd) throws Throwable {
        DecorationOps.callsite().invoke(newPlayer, oldPlayer, bl);
        if (!conqueredEnd) {  // keep inventory here since inventory dropped at ServerPlayerEntity#onDeath
            newPlayer.getInventory().replaceWith(oldPlayer.getInventory());
            newPlayer.experienceLevel = oldPlayer.experienceLevel;
            newPlayer.totalExperience = oldPlayer.totalExperience;
            newPlayer.experienceProgress = oldPlayer.experienceProgress;
            newPlayer.setScore(oldPlayer.getScore());
        }
    }

    @Decorate(method = "respawn", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl;teleport(DDDFF)V"))
    private void banner$respawnPackets(ServerGamePacketListenerImpl instance, double d, double e, double f, float g, float h, @io.izzel.arclight.mixin.Local(ordinal = -1) ServerPlayer player) throws Throwable {
        player.connection.send(new ClientboundSetChunkCacheRadiusPacket(player.serverLevel().bridge$spigotConfig().viewDistance));
        player.connection.send(new ClientboundSetSimulationDistancePacket(player.serverLevel().bridge$spigotConfig().simulationDistance));
        player.connection.teleport(new Location(player.serverLevel().getWorld(), player.getX(), player.getY(), player.getZ(), player.getYRot(), player.getXRot()));
        if (Blackhole.actuallyFalse()) {
            DecorationOps.callsite().invoke(instance, d, e, f, g, h);
        }
    }

    @Inject(method = "respawn", at = @At("RETURN"))
    private void banner$postRespawn(ServerPlayer serverPlayer, boolean bl, Entity.RemovalReason removalReason, CallbackInfoReturnable<ServerPlayer> cir) {
        banner$loc = null;
        banner$respawnReason = null;
        var fromWorld = serverPlayer.serverLevel();
        var newPlayer = cir.getReturnValue();
        this.sendAllPlayerInfo(newPlayer);
        newPlayer.onUpdateAbilities();
        newPlayer.triggerDimensionChangeTriggers(fromWorld);
        if (fromWorld != newPlayer.serverLevel()) {
            PlayerChangedWorldEvent event = new PlayerChangedWorldEvent(newPlayer.getBukkitEntity(), fromWorld.getWorld());
            Bukkit.getPluginManager().callEvent(event);
        }
        if (newPlayer.connection.banner$isDisconnected()) {
            this.save(newPlayer);
        }
    }

    @Override
    public ServerPlayer respawn(ServerPlayer playerIn, boolean flag, Entity.RemovalReason removalReason, PlayerRespawnEvent.RespawnReason respawnReason, Location location) {
        if (true) { // TODO remove on next update
            banner$respawnReason = respawnReason;
            banner$loc = location;
            return this.respawn(playerIn, flag, removalReason);
        }
        playerIn.stopRiding();
        this.players.remove(playerIn);
        playerIn.serverLevel().removePlayerImmediately(playerIn, removalReason);
        //playerIn.revive();
        World fromWorld =  playerIn.getBukkitEntity().getWorld();
        playerIn.wonGame = false;
        /*
        playerIn.copyFrom(playerIn, flag);
        playerIn.setEntityId(playerIn.getEntityId());
        playerIn.setPrimaryHand(playerIn.getPrimaryHand());
        for (String s : playerIn.getTags()) {
            playerIn.addTag(s);
        }
        */
        DimensionTransition dimensiontransition;
        if (location == null) {
            //playerIn.pushRespawnReason(respawnReason);
            dimensiontransition = playerIn.findRespawnPositionAndUseSpawnBlock(flag, DimensionTransition.DO_NOTHING);
            if (!flag) {
                 playerIn.reset(); // SPIGOT-4785
            }
        } else {
            dimensiontransition = new DimensionTransition(((CraftWorld) location.getWorld()).getHandle(), CraftLocation.toVec3D(location), Vec3.ZERO, location.getYaw(), location.getPitch(), DimensionTransition.DO_NOTHING);
        }
        // Spigot Start
        if (dimensiontransition == null) {
            return playerIn;
        }
        ServerLevel serverWorld = ((CraftWorld) location.getWorld()).getHandle();
        playerIn.setServerLevel(serverWorld);
        playerIn.unsetRemoved();
        playerIn.setShiftKeyDown(false);
        playerIn.moveTo(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
        playerIn.connection.resetPosition();
        if (dimensiontransition.missingRespawnBlock()) {
            playerIn.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.NO_RESPAWN_BLOCK_AVAILABLE, 0.0F));
            playerIn.pushChangeSpawnCause(PlayerSpawnChangeEvent.Cause.RESET);
            playerIn.setRespawnPosition(null, null, 0f, false, false); // CraftBukkit - SPIGOT-5988: Clear respawn location when obstructed
        }
        LevelData worlddata = serverWorld.getLevelData();
        playerIn.connection.send(new ClientboundRespawnPacket(playerIn.createCommonSpawnInfo(serverWorld), (byte) (flag ? 1 : 0)));
        playerIn.connection.send(new ClientboundSetChunkCacheRadiusPacket(serverWorld.bridge$spigotConfig().viewDistance));
        playerIn.connection.send(new ClientboundSetSimulationDistancePacket(serverWorld.bridge$spigotConfig().simulationDistance));
        playerIn.connection.teleport(new Location(serverWorld.getWorld(), playerIn.getX(), playerIn.getY(), playerIn.getZ(), playerIn.getYRot(), playerIn.getXRot()));
        playerIn.connection.send(new ClientboundSetDefaultSpawnPositionPacket(serverWorld.getSharedSpawnPos(), serverWorld.getSharedSpawnAngle()));
        playerIn.connection.send(new ClientboundChangeDifficultyPacket(worlddata.getDifficulty(), worlddata.isDifficultyLocked()));
        playerIn.connection.send(new ClientboundSetExperiencePacket(playerIn.experienceProgress, playerIn.totalExperience, playerIn.experienceLevel));
        this.sendActivePlayerEffects(playerIn);
        this.sendLevelInfo(playerIn, serverWorld);
        this.sendPlayerPermissionLevel(playerIn);
        if (!playerIn.connection.banner$isDisconnected()) {
            serverWorld.addRespawnedPlayer(playerIn);
            this.players.add(playerIn);
            this.playersByUUID.put(playerIn.getUUID(), playerIn);
        }
        playerIn.setHealth(playerIn.getHealth());
        if (!flag) {
            BlockPos blockposition = BlockPos.containing(dimensiontransition.pos());
            BlockState iblockdata = serverWorld.getBlockState(blockposition);

            if (iblockdata.is(Blocks.RESPAWN_ANCHOR)) {
                playerIn.connection.send(new ClientboundSoundPacket(SoundEvents.RESPAWN_ANCHOR_DEPLETE, SoundSource.BLOCKS, (double) blockposition.getX(), (double) blockposition.getY(), (double) blockposition.getZ(), 1.0F, 1.0F, serverWorld.getRandom().nextLong()));
            }
        }
        this.sendAllPlayerInfo(playerIn);
        playerIn.onUpdateAbilities();
        playerIn.triggerDimensionChangeTriggers(((CraftWorld) fromWorld).getHandle());
        if (fromWorld != playerIn.serverLevel()) {
            PlayerChangedWorldEvent event = new PlayerChangedWorldEvent(playerIn.getBukkitEntity(), fromWorld);
            Bukkit.getPluginManager().callEvent(event);
        }
        if (playerIn.connection.banner$isDisconnected()) {
            this.save(playerIn);
        }
        return playerIn;
    }

    @Override
    public ServerPlayer respawn(ServerPlayer entityplayer, ServerLevel worldserver, boolean flag, Location location, boolean avoidSuffocation, Entity.RemovalReason entity_removalreason, PlayerRespawnEvent.RespawnReason reason) {
        this.banner$loc = location;
        this.banner$worldserver = worldserver;
        this.banner$respawnReason = reason;
        this.avoidSuffocation.set(avoidSuffocation);
        return this.respawn(entityplayer, flag, entity_removalreason, reason, null);
    }

    @Override
    public ServerPlayer respawn(ServerPlayer entityplayer, boolean flag, Entity.RemovalReason entity_removalreason, PlayerRespawnEvent.RespawnReason reason) {
        return this.respawn(entityplayer, this.server.getLevel(entityplayer.getRespawnDimension()), flag, null, true, entity_removalreason, reason);
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
