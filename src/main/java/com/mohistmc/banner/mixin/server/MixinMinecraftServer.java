package com.mohistmc.banner.mixin.server;

import com.google.common.collect.Maps;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mohistmc.banner.BannerMCStart;
import com.mohistmc.banner.bukkit.BukkitCaptures;
import com.mohistmc.banner.bukkit.BukkitExtraConstants;
import com.mohistmc.banner.injection.server.InjectionMinecraftServer;
import com.mojang.datafixers.DataFixer;
import it.unimi.dsi.fastutil.longs.LongIterator;
import jline.console.ConsoleReader;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import net.minecraft.Util;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.ChatDecorator;
import net.minecraft.network.protocol.game.ClientboundSetTimePacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.*;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.server.level.progress.ChunkProgressListenerFactory;
import net.minecraft.server.network.ServerConnectionListener;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.players.PlayerList;
import net.minecraft.util.Unit;
import net.minecraft.util.thread.ReentrantBlockableEventLoop;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ForcedChunksSavedData;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraft.world.level.storage.WorldData;
import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.RemoteConsoleCommandSender;
import org.bukkit.craftbukkit.Main;
import org.bukkit.craftbukkit.v1_20_R1.CraftServer;
import org.bukkit.craftbukkit.v1_20_R1.SpigotTimings;
import org.bukkit.craftbukkit.v1_20_R1.util.CraftChatMessage;
import org.bukkit.craftbukkit.v1_20_R1.util.LazyPlayerSet;
import org.bukkit.event.player.AsyncPlayerChatPreviewEvent;
import org.bukkit.event.server.ServerLoadEvent;
import org.bukkit.event.world.WorldInitEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.plugin.PluginLoadOrder;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.lang.management.ManagementFactory;
import java.net.Proxy;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.BooleanSupplier;
import java.util.logging.Level;
import java.util.logging.Logger;

// Banner - TODO fix inject method
@Mixin(MinecraftServer.class)
public abstract class MixinMinecraftServer extends ReentrantBlockableEventLoop<TickTask> implements InjectionMinecraftServer {

    // @formatter:off
    @Shadow public MinecraftServer.ReloadableResources resources;

    @Shadow public Map<ResourceKey<net.minecraft.world.level.Level>, ServerLevel> levels;
    @Shadow @Final public static org.slf4j.Logger LOGGER;
    @Shadow private long nextTickTime;
    @Shadow public abstract boolean isSpawningMonsters();
    @Shadow public abstract boolean isSpawningAnimals();
    @Shadow private int tickCount;
    @Shadow public abstract PlayerList getPlayerList();
    @Shadow public abstract boolean isStopped();
    // @formatter:on

    @Shadow public ServerConnectionListener connection;

    @Shadow public abstract ServerLevel overworld();

    @Shadow protected abstract void updateMobSpawningFlags();

    // CraftBukkit start
    public WorldLoader.DataLoadContext worldLoader;
    public org.bukkit.craftbukkit.v1_20_R1.CraftServer server;
    public OptionSet options;
    public org.bukkit.command.ConsoleCommandSender console;
    public org.bukkit.command.RemoteConsoleCommandSender remoteConsole;
    public ConsoleReader reader;
    private static int currentTick = BukkitExtraConstants.currentTick;
    public java.util.Queue<Runnable> processQueue = BukkitExtraConstants.bridge$processQueue;
    public int autosavePeriod = BukkitExtraConstants.bridge$autosavePeriod;
    private boolean forceTicks;
    public Commands vanillaCommandDispatcher;
    private boolean hasStopped = false;
    private final Object stopLock = new Object();
    public final double[] recentTps = new double[3];
    // CraftBukkit end

    public MixinMinecraftServer(String string) {
        super(string);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void banner$initServer(Thread thread, LevelStorageSource.LevelStorageAccess levelStorageAccess, PackRepository packRepository, WorldStem worldStem, Proxy proxy, DataFixer dataFixer, Services services, ChunkProgressListenerFactory chunkProgressListenerFactory, CallbackInfo ci) {
        String[] arguments = ManagementFactory.getRuntimeMXBean().getInputArguments().toArray(new String[0]);
        OptionParser parser = new Main();
        try {
            options = parser.parse(arguments);
        } catch (Exception ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, ex.getLocalizedMessage());
        }
        Main.handleParser(parser, options);
        this.vanillaCommandDispatcher = worldStem.dataPackResources().getCommands();
        this.worldLoader = BukkitCaptures.getDataLoadContext();
        //Runtime.getRuntime().addShutdownHook(new ServerShutdownThread(((MinecraftServer) (Object) this)));
    }

    @Inject(method = "stopServer", at = @At(value = "INVOKE", remap = false, ordinal = 0, shift = At.Shift.AFTER, target = "Lorg/slf4j/Logger;info(Ljava/lang/String;)V"))
    public void banner$unloadPlugins(CallbackInfo ci) {
        if (this.server != null) {
            this.server.disablePlugins();
        }
    }

    @Inject(method = "stopServer", at = @At("HEAD"))
    private void banner$stop(CallbackInfo ci) {
        synchronized (this.stopLock) {
            if (this.hasStopped)
                return;
            this.hasStopped = true;
        }
    }

    @Inject(method = "stopServer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/players/PlayerList;removeAll()V"))
    private void banner$stopThread(CallbackInfo ci) {
        try { Thread.sleep(100); } catch (InterruptedException ex) {} // CraftBukkit - SPIGOT-625 - give server at least a chance to send packets
    }

    @ModifyConstant(method = "runServer", constant = @Constant(longValue = 15000L))
    private long banner$changeWarningValue(long constant) {
        return 30000L;
    }

    @Inject(method = "runServer", at = @At(value = "FIELD", target = "Lnet/minecraft/server/MinecraftServer;nextTickTime:J", shift = At.Shift.BEFORE))
    private void banner$currentTick(CallbackInfo ci) {
        BukkitExtraConstants.currentTick = (int) (System.currentTimeMillis() / 50); // CraftBukkit
    }

    @Inject(method = "getServerModName", at = @At(value = "HEAD"), remap = false, cancellable = true)
    private void banner$setServerModName(CallbackInfoReturnable<String> cir) {
        if (this.server != null) {
            cir.setReturnValue(server.getServer().getServerName());
        }
    }

    @Override
    public boolean hasStopped() {
        synchronized (stopLock) {
            return hasStopped;
        }
    }

    @Override
    public void banner$setServer(CraftServer server) {
        this.server = server;
    }

    private static MinecraftServer getServer() {
        return Bukkit.getServer() instanceof CraftServer ? ((CraftServer) Bukkit.getServer()).getServer() : null;
    }

    @Override
    public void addLevel(ServerLevel level) {
        Map<ResourceKey<net.minecraft.world.level.Level>, ServerLevel> oldLevels = this.levels;
        Map<ResourceKey<net.minecraft.world.level.Level>, ServerLevel> newLevels = Maps.newLinkedHashMap(oldLevels);
        newLevels.put(level.dimension(), level);
        this.levels = Collections.unmodifiableMap(newLevels);
    }

    @Override
    public void removeLevel(ServerLevel level) {
        Map<ResourceKey<net.minecraft.world.level.Level>, ServerLevel> oldLevels = this.levels;
        Map<ResourceKey<net.minecraft.world.level.Level>, ServerLevel> newLevels = Maps.newLinkedHashMap(oldLevels);
        newLevels.remove(level.dimension(), level);
        this.levels = Collections.unmodifiableMap(newLevels);
    }

    @Inject(method = "createLevels",
            at = @At(value = "INVOKE",
                    target = "Ljava/util/Map;put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"),
            locals = LocalCapture.CAPTURE_FAILHARD)
    private void banner$callInitEvent(ChunkProgressListener listener, CallbackInfo ci, ServerLevelData serverLevelData, boolean bl, Registry registry, WorldOptions worldOptions, long l, long m, List list, LevelStem levelStem, ServerLevel serverLevel) {
        this.server.getPluginManager().callEvent(new WorldInitEvent(serverLevel.getWorld()));
    }

    @Inject(method = "loadLevel", at = @At("TAIL"))
    private void banner$initPlugins(CallbackInfo ci) {
        this.server.enablePlugins(PluginLoadOrder.POSTWORLD);
        this.server.getPluginManager().callEvent(new ServerLoadEvent(ServerLoadEvent.LoadType.STARTUP));
        for (ServerLevel worldserver : ((MinecraftServer)(Object)this).getAllLevels()) {
            worldserver.entityManager.tick(); // SPIGOT-6526: Load pending entities so they are available to the API
            this.server.getPluginManager().callEvent(new WorldLoadEvent(worldserver.getWorld()));
            this.connection.acceptConnections();
        }
    }

    @Inject(method = "saveAllChunks", cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD, at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;overworld()Lnet/minecraft/server/level/ServerLevel;"))
    private void banner$skipSave(boolean suppressLog, boolean flush, boolean forced, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(!this.levels.isEmpty());
    }

    @Inject(method = "setInitialSpawn", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerChunkCache;getGenerator()Lnet/minecraft/world/level/chunk/ChunkGenerator;", shift = At.Shift.BEFORE), cancellable = true)
    private static void banner$spawnInit(ServerLevel level, ServerLevelData levelData, boolean generateBonusChest, boolean debug, CallbackInfo ci) {
        // CraftBukkit start
        if (level.bridge$generator() != null) {
            Random rand = new Random(level.getSeed());
            org.bukkit.Location spawn = level.bridge$generator().getFixedSpawnLocation(level.getWorld(), rand);

            if (spawn != null) {
                if (spawn.getWorld() != level.getWorld()) {
                    throw new IllegalStateException("Cannot set spawn point for " + levelData.getLevelName() + " to be in another world (" + spawn.getWorld().getName() + ")");
                } else {
                    levelData.setSpawn(new BlockPos(spawn.getBlockX(), spawn.getBlockY(), spawn.getBlockZ()), spawn.getYaw());
                    ci.cancel();
                }
            }
        }
    }

    @Override
    public void initWorld(ServerLevel serverWorld, ServerLevelData worldInfo, WorldData saveData, WorldOptions worldOptions) {
        if ((serverWorld.bridge$generator() != null)) {
            serverWorld.getWorld().getPopulators().addAll(
                    serverWorld.bridge$generator().getDefaultPopulators(
                            (serverWorld.getWorld())));
        }
    }

    /**
     * @author wdog5
     * @reason
     */
    @Overwrite
    public final void prepareLevels(ChunkProgressListener listener) {
        ServerLevel serverworld = this.overworld();
        this.forceTicks = true;
        LOGGER.info("Preparing start region for dimension {}", serverworld.dimension().location());
        BlockPos blockpos = serverworld.getSharedSpawnPos();
        listener.updateSpawnPos(new ChunkPos(blockpos));
        ServerChunkCache serverchunkprovider = serverworld.getChunkSource();
        this.nextTickTime = Util.getMillis();
        serverchunkprovider.addRegionTicket(TicketType.START, new ChunkPos(blockpos), 11, Unit.INSTANCE);

        while (serverchunkprovider.getTickingGenerated() < 441) {
            this.executeModerately();
        }

        this.executeModerately();

        for (ServerLevel serverWorld : this.levels.values()) {
            if (serverWorld.getWorld().getKeepSpawnInMemory()) {
                ForcedChunksSavedData forcedchunkssavedata = serverWorld.getDataStorage().get(ForcedChunksSavedData::load, "chunks");
                if (forcedchunkssavedata != null) {
                    LongIterator longiterator = forcedchunkssavedata.getChunks().iterator();

                    while (longiterator.hasNext()) {
                        long i = longiterator.nextLong();
                        ChunkPos chunkpos = new ChunkPos(i);
                        serverWorld.getChunkSource().updateChunkForced(chunkpos, true);
                    }
                }
            }
        }

        this.executeModerately();
        listener.stop();
        this.updateMobSpawningFlags();
        this.forceTicks = false;
    }

    @Override
    public void prepareLevels(ChunkProgressListener listener, ServerLevel serverWorld) {
        if (!serverWorld.getWorld().getKeepSpawnInMemory()) {
            return;
        }
        this.forceTicks = true;
        LOGGER.info(BannerMCStart.I18N.get("server.region.prepare"), serverWorld.dimension().location());
        BlockPos blockpos = serverWorld.getSharedSpawnPos();
        listener.updateSpawnPos(new ChunkPos(blockpos));
        ServerChunkCache serverchunkprovider = serverWorld.getChunkSource();
        this.nextTickTime = Util.getMillis();
        serverchunkprovider.addRegionTicket(TicketType.START, new ChunkPos(blockpos), 11, Unit.INSTANCE);

        while (serverchunkprovider.getTickingGenerated() < 441) {
            this.executeModerately();
        }

        this.executeModerately();

        ForcedChunksSavedData forcedchunkssavedata = serverWorld.getDataStorage().get(ForcedChunksSavedData::load, "chunks");
        if (forcedchunkssavedata != null) {
            LongIterator longiterator = forcedchunkssavedata.getChunks().iterator();

            while (longiterator.hasNext()) {
                long i = longiterator.nextLong();
                ChunkPos chunkpos = new ChunkPos(i);
                serverWorld.getChunkSource().updateChunkForced(chunkpos, true);
            }
        }
        this.executeModerately();
        listener.stop();
        // this.updateMobSpawningFlags();
        serverWorld.setSpawnSettings(this.isSpawningMonsters(), this.isSpawningAnimals());
        this.forceTicks = false;
    }

    @Override
    public void executeModerately() {
        this.runAllTasks();
        java.util.concurrent.locks.LockSupport.parkNanos("executing tasks", 1000L);
    }

    @Inject(method = "haveTime", cancellable = true, at = @At("HEAD"))
    private void banner$forceAheadOfTime(CallbackInfoReturnable<Boolean> cir) {
        if (this.forceTicks) cir.setReturnValue(true);
    }

    @Inject(method = "tickChildren", at = @At("HEAD"))
    private void banner$processStart(BooleanSupplier hasTimeLeft, CallbackInfo ci) {
        server.getScheduler().mainThreadHeartbeat(this.tickCount);
    }

    @Inject(method = "tickServer", at = @At("HEAD"))
    private void banner$useTimings(BooleanSupplier hasTimeLeft, CallbackInfo ci) {
        SpigotTimings.serverTickTimer.startTiming(); // Spigot
    }

    @Inject(method = "tickServer", at = @At(value = "INVOKE",
            target = "Lorg/slf4j/Logger;debug(Ljava/lang/String;)V",
            ordinal = 0,
            remap = false))
    private void banner$useTimings0(BooleanSupplier hasTimeLeft, CallbackInfo ci) {
        SpigotTimings.worldSaveTimer.startTiming(); // Spigot
    }

    @Inject(method = "tickServer", at = @At(value = "INVOKE",
            target = "Lorg/slf4j/Logger;debug(Ljava/lang/String;)V",
            ordinal = 1,
            shift = At.Shift.AFTER,
            remap = false))
    private void banner$useTimings1(BooleanSupplier hasTimeLeft, CallbackInfo ci) {
        SpigotTimings.worldSaveTimer.stopTiming(); // Spigot
    }

    @Inject(method = "tickServer", at = @At("TAIL"))
    private void banner$useTimings2(BooleanSupplier hasTimeLeft, CallbackInfo ci) {
        SpigotTimings.serverTickTimer.stopTiming(); // Spigot
        org.spigotmc.CustomTimingsHandler.tick(); // Spigot
    }

    @Inject(method = "tickChildren", at = @At("HEAD"))
    private void banner$addTimings(BooleanSupplier hasTimeLeft, CallbackInfo ci) {
        SpigotTimings.schedulerTimer.startTiming(); // Spigot
    }

    @Inject(method = "tickChildren",
            at = @At(value = "INVOKE",
            target = "Lnet/minecraft/util/profiling/ProfilerFiller;push(Ljava/lang/String;)V",
                    ordinal = 0))
    private void banner$addTimings0(BooleanSupplier hasTimeLeft, CallbackInfo ci) {
        this.server.getScheduler().mainThreadHeartbeat(this.tickCount); // CraftBukkit
        SpigotTimings.schedulerTimer.stopTiming(); // Spigot
    }

    @Inject(method = "tickChildren",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/server/ServerFunctionManager;tick()V"))
    private void banner$addTimings1(BooleanSupplier hasTimeLeft, CallbackInfo ci) {
        this.server.getScheduler().mainThreadHeartbeat(this.tickCount); // CraftBukkit
        SpigotTimings.commandFunctionsTimer.startTiming(); // Spigot
    }

    @Inject(method = "tickChildren",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/util/profiling/ProfilerFiller;popPush(Ljava/lang/String;)V",
                    ordinal = 0))
    private void banner$addTimings2(BooleanSupplier hasTimeLeft, CallbackInfo ci) {
        this.server.getScheduler().mainThreadHeartbeat(this.tickCount); // CraftBukkit
        SpigotTimings.commandFunctionsTimer.stopTiming(); // Spigot
    }

    @Inject(method = "tickChildren", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;getAllLevels()Ljava/lang/Iterable;"))
    private void banner$checkHeart(BooleanSupplier hasTimeLeft, CallbackInfo ci) {
        // CraftBukkit start
        // Run tasks that are waiting on processing
        SpigotTimings.processQueueTimer.startTiming(); // Spigot
        while (!processQueue.isEmpty()) {
            processQueue.remove().run();
        }
        SpigotTimings.processQueueTimer.stopTiming(); // Spigot

        SpigotTimings.timeUpdateTimer.startTiming(); // Spigot
        // Send time updates to everyone, it will get the right time from the world the player is in.
        if (this.tickCount % 20 == 0) {
            for (int i = 0; i < this.getPlayerList().players.size(); ++i) {
                ServerPlayer entityplayer = (ServerPlayer) this.getPlayerList().players.get(i);
                entityplayer.connection.send(new ClientboundSetTimePacket(entityplayer.level().getGameTime(), entityplayer.getPlayerTime(), entityplayer.level().getGameRules().getBoolean(GameRules.RULE_DAYLIGHT))); // Add support for per player time
            }
        }
        SpigotTimings.timeUpdateTimer.stopTiming(); // Spigot
    }

    @Inject(method = "tickChildren",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerLevel;tick(Ljava/util/function/BooleanSupplier;)V"),
            locals = LocalCapture.CAPTURE_FAILHARD)
    private void banner$addTimings3(BooleanSupplier hasTimeLeft, CallbackInfo ci,
                                    Iterator var2, ServerLevel serverLevel) {
        serverLevel.bridge$timings().doTick.startTiming(); // Spigot
    }

    @Inject(method = "tickChildren",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerLevel;tick(Ljava/util/function/BooleanSupplier;)V",
                    shift = At.Shift.AFTER),
            locals = LocalCapture.CAPTURE_FAILHARD)
    private void banner$addTimings4(BooleanSupplier hasTimeLeft, CallbackInfo ci,
                                    Iterator var2, ServerLevel serverLevel) {
        serverLevel.bridge$timings().doTick.stopTiming(); // Spigot
    }

    @Inject(method = "tickChildren",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/server/network/ServerConnectionListener;tick()V"))
    private void banner$addTimings4(BooleanSupplier hasTimeLeft, CallbackInfo ci) {
        SpigotTimings.connectionTimer.startTiming(); // Spigot
    }

    @Inject(method = "tickChildren",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/util/profiling/ProfilerFiller;popPush(Ljava/lang/String;)V",
                    ordinal = 2))
    private void banner$addTimings5(BooleanSupplier hasTimeLeft, CallbackInfo ci) {
        SpigotTimings.connectionTimer.stopTiming(); // Spigot
    }

    @Inject(method = "tickChildren",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/server/players/PlayerList;tick()V"))
    private void banner$addTimings6(BooleanSupplier hasTimeLeft, CallbackInfo ci) {
        SpigotTimings.playerListTimer.startTiming(); // Spigot
    }

    @Inject(method = "tickChildren",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/server/players/PlayerList;tick()V",
                    shift = At.Shift.AFTER))
    private void banner$addTimings7(BooleanSupplier hasTimeLeft, CallbackInfo ci) {
        SpigotTimings.playerListTimer.stopTiming(); // Spigot
    }

    @Inject(method = "tickChildren",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/util/profiling/ProfilerFiller;popPush(Ljava/lang/String;)V",
                    shift = At.Shift.AFTER,
                    ordinal = 3))
    private void banner$addTimings8(BooleanSupplier hasTimeLeft, CallbackInfo ci) {
        SpigotTimings.tickablesTimer.startTiming(); // Spigot
    }

    @Inject(method = "tickChildren",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/util/profiling/ProfilerFiller;pop()V",
                    ordinal = 3))
    private void banner$addTimings9(BooleanSupplier hasTimeLeft, CallbackInfo ci) {
        SpigotTimings.tickablesTimer.stopTiming(); // Spigot
    }

    // CraftBukkit start
    public final java.util.concurrent.ExecutorService chatExecutor = java.util.concurrent.Executors.newCachedThreadPool(
            new com.google.common.util.concurrent.ThreadFactoryBuilder().setDaemon(true).setNameFormat("Async Chat Thread - #%d").build());

    @ModifyReturnValue(method = "getChatDecorator", at = @At("RETURN"))
    private ChatDecorator banner$fireChatEvent(ChatDecorator decorator) {
        return (entityplayer, ichatbasecomponent) -> {
            // SPIGOT-7127: Console /say and similar
            if (entityplayer == null) {
                return CompletableFuture.completedFuture(ichatbasecomponent);
            }

            return CompletableFuture.supplyAsync(() -> {
                AsyncPlayerChatPreviewEvent event = new AsyncPlayerChatPreviewEvent(true, entityplayer.getBukkitEntity(), CraftChatMessage.fromComponent(ichatbasecomponent), new LazyPlayerSet(((MinecraftServer) (Object) this)));
                String originalFormat = event.getFormat(), originalMessage = event.getMessage();
                this.server.getPluginManager().callEvent(event);

                if (originalFormat.equals(event.getFormat()) && originalMessage.equals(event.getMessage()) && event.getPlayer().getName().equalsIgnoreCase(event.getPlayer().getDisplayName())) {
                    return ichatbasecomponent;
                }
                return CraftChatMessage.fromStringOrNull(String.format(event.getFormat(), event.getPlayer().getDisplayName(), event.getMessage()));
            }, chatExecutor);
        };
    }

    @Inject(method = "method_29440", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/server/packs/repository/PackRepository;setSelected(Ljava/util/Collection;)V"))
    private void banner$syncCommands(Collection collection, MinecraftServer.ReloadableResources reloadableResources,
                                     CallbackInfo ci) {
        this.server.syncCommands(); // SPIGOT-5884: Lost on reload
    }

    // Banner start
    @Override
    public WorldLoader.DataLoadContext bridge$worldLoader() {
        return worldLoader;
    }

    @Override
    public CraftServer bridge$server() {
        return server;
    }

    @Override
    public OptionSet bridge$options() {
        return options;
    }

    @Override
    public ConsoleCommandSender bridge$console() {
        return console;
    }

    @Override
    public RemoteConsoleCommandSender bridge$remoteConsole() {
        return remoteConsole;
    }

    @Override
    public ConsoleReader bridge$reader() {
        return reader;
    }

    @Override
    public boolean bridge$forceTicks() {
        return forceTicks;
    }

    @Override
    public boolean isDebugging() {
        return false;
    }

    @Override
    public void banner$setRemoteConsole(RemoteConsoleCommandSender remoteConsole) {
        this.remoteConsole = remoteConsole;
    }

    @Override
    public void banner$setConsole(ConsoleCommandSender console) {
        this.console = console;
    }

    // Banner end


    @Override
    public void bridge$queuedProcess(Runnable runnable) {
        processQueue.add(runnable);
    }

    @Override
    public Queue<Runnable> bridge$processQueue() {
        return processQueue;
    }

    @Override
    public void banner$setProcessQueue(Queue<Runnable> processQueue) {
        this.processQueue = processQueue;
    }


    @Override
    public Commands bridge$getVanillaCommands() {
        return this.vanillaCommandDispatcher;
    }

    @Override
    public java.util.concurrent.ExecutorService bridge$chatExecutor() {
        return chatExecutor;
    }

    @Override
    public boolean isSameThread() {
        return super.isSameThread() || this.isStopped(); // CraftBukkit - MC-142590
    }
}
