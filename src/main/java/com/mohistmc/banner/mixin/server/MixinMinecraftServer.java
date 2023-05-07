package com.mohistmc.banner.mixin.server;

import com.google.common.collect.Maps;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mohistmc.banner.bukkit.BukkitCaptures;
import com.mohistmc.banner.injection.server.InjectionMinecraftServer;
import it.unimi.dsi.fastutil.longs.LongIterator;
import net.minecraft.CrashReport;
import net.minecraft.ReportedException;
import net.minecraft.Util;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.ChatDecorator;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.util.Unit;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ForcedChunksSavedData;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraft.world.level.storage.WorldData;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.Main;
import com.mohistmc.banner.util.ServerUtils;
import com.mojang.datafixers.DataFixer;
import jline.console.ConsoleReader;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import net.minecraft.server.*;
import net.minecraft.server.level.progress.ChunkProgressListenerFactory;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.util.thread.ReentrantBlockableEventLoop;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.RemoteConsoleCommandSender;
import org.bukkit.craftbukkit.v1_19_R3.CraftServer;
import org.bukkit.craftbukkit.v1_19_R3.util.CraftChatMessage;
import org.bukkit.craftbukkit.v1_19_R3.util.LazyPlayerSet;
import org.bukkit.event.player.AsyncPlayerChatPreviewEvent;
import org.bukkit.event.server.ServerLoadEvent;
import org.bukkit.event.world.WorldInitEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.plugin.PluginLoadOrder;
import org.fusesource.jansi.AnsiConsole;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.Proxy;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

// Banner - TODO fix inject method
@Mixin(MinecraftServer.class)
public abstract class MixinMinecraftServer extends ReentrantBlockableEventLoop<TickTask> implements InjectionMinecraftServer {

    // @formatter:off
    @Shadow public MinecraftServer.ReloadableResources resources;

    @Shadow public Map<ResourceKey<net.minecraft.world.level.Level>, ServerLevel> levels;
    @Shadow private static void setInitialSpawn(ServerLevel level, ServerLevelData levelData, boolean generateBonusChest, boolean debug) {}
    @Shadow protected abstract void setupDebugLevel(WorldData worldData);
    @Shadow public WorldData worldData;
    @Shadow @Final public static org.slf4j.Logger LOGGER;
    @Shadow private long nextTickTime;
    @Shadow public abstract boolean isSpawningMonsters();
    @Shadow public abstract boolean isSpawningAnimals();
    // @formatter:on

    @Shadow private String localIp;
    @Shadow private boolean onlineMode;
    // CraftBukkit start
    public WorldLoader.DataLoadContext worldLoader;
    public org.bukkit.craftbukkit.v1_19_R3.CraftServer server;
    public OptionSet options;
    public org.bukkit.command.ConsoleCommandSender console;
    public org.bukkit.command.RemoteConsoleCommandSender remoteConsole;
    public ConsoleReader reader;
    private static int currentTick = ServerUtils.getCurrentTick();
    public java.util.Queue<Runnable> processQueue = ServerUtils.bridge$processQueue;
    public int autosavePeriod = ServerUtils.bridge$autosavePeriod;
    private boolean forceTicks;
    public Commands vanillaCommandDispatcher;
    private boolean hasStopped = false;
    private final Object stopLock = new Object();
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

        if ((options == null) || (options.has("?"))) {
            try {
                parser.printHelpOn(System.out);
            } catch (IOException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else if (options.has("v")) {
            System.out.println(CraftServer.class.getPackage().getImplementationVersion());
        } else {
            // Do you love Java using + and ! as string based identifiers? I sure do!
            String path = new File(".").getAbsolutePath();
            if (path.contains("!") || path.contains("+")) {
                System.err.println("Cannot run server in a directory with ! or + in the pathname. Please rename the affected folders and try again.");
                return;
            }

            float javaVersion = Float.parseFloat(System.getProperty("java.class.version"));
            if (javaVersion < 61.0) {
                System.err.println("Unsupported Java detected (" + javaVersion + "). This version of Minecraft requires at least Java 17. Check your Java version with the command 'java -version'.");
                return;
            }
            if (javaVersion > 63) {
                System.err.println("Unsupported Java detected (" + javaVersion + "). Only up to Java 19 is supported.");
                return;
            }

            try {
                // This trick bypasses Maven Shade's clever rewriting of our getProperty call when using String literals
                String jline_UnsupportedTerminal = new String(new char[]{'j', 'l', 'i', 'n', 'e', '.', 'U', 'n', 's', 'u', 'p', 'p', 'o', 'r', 't', 'e', 'd', 'T', 'e', 'r', 'm', 'i', 'n', 'a', 'l'});
                String jline_terminal = new String(new char[]{'j', 'l', 'i', 'n', 'e', '.', 't', 'e', 'r', 'm', 'i', 'n', 'a', 'l'});

                Main.useJline = !(jline_UnsupportedTerminal).equals(System.getProperty(jline_terminal));

                if (options.has("nojline")) {
                    System.setProperty("user.language", "en");
                    Main.useJline = false;
                }

                if (Main.useJline) {
                    AnsiConsole.systemInstall();
                } else {
                    // This ensures the terminal literal will always match the jline implementation
                    System.setProperty(jline.TerminalFactory.JLINE_TERMINAL, jline.UnsupportedTerminal.class.getName());
                }

                if (options.has("noconsole")) {
                    Main.useConsole = false;
                }

                if (Main.class.getPackage().getImplementationVendor() != null && System.getProperty("IReallyKnowWhatIAmDoingISwear") == null) {
                    Date buildDate = new Date(Integer.parseInt(Main.class.getPackage().getImplementationVendor()) * 1000L);

                    Calendar deadline = Calendar.getInstance();
                    deadline.add(Calendar.DAY_OF_YEAR, -28);
                    if (buildDate.before(deadline.getTime())) {
                        System.err.println("*** Error, this build is outdated ***");
                        System.err.println("*** Please download a new build as per instructions from https://www.spigotmc.org/go/outdated-spigot ***");
                        System.err.println("*** Server will start in 20 seconds ***");
                        Thread.sleep(TimeUnit.SECONDS.toMillis(20));
                    }
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }

            this.vanillaCommandDispatcher = worldStem.dataPackResources().getCommands();
            this.worldLoader = BukkitCaptures.getDataLoadContext();
        }
    }

    @Inject(method = "stopServer", at = @At(value = "INVOKE", remap = false, ordinal = 0, shift = At.Shift.AFTER, target = "Lorg/slf4j/Logger;info(Ljava/lang/String;)V"))
    public void banner$unloadPlugins(CallbackInfo ci) {
        if (this.server != null) {
            this.server.disablePlugins();
        }
    }

    @Inject(method = "getServerModName", at = @At(value = "HEAD"), remap = false, cancellable = true)
    private void banner$setServerModName(CallbackInfoReturnable<String> cir) {
        if (this.server != null) {
            cir.setReturnValue(server.getServer().getServerName());
        }
    }

    @Inject(method = "reloadResources", at = @At(value = "RETURN", target = "Ljava/util/concurrent/CompletableFuture;thenAcceptAsync(Ljava/util/function/Consumer;Ljava/util/concurrent/Executor;)Ljava/util/concurrent/CompletableFuture;"))
    private void banner$syncCommand(Collection<String> selectedIds, CallbackInfoReturnable<CompletableFuture<Void>> cir) {
        this.server.syncCommands();
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
            this.server.getPluginManager().callEvent(new WorldLoadEvent(worldserver.getWorld()));
        }
    }

    @Inject(method = "saveAllChunks", cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD, at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;overworld()Lnet/minecraft/server/level/ServerLevel;"))
    private void banner$skipSave(boolean suppressLog, boolean flush, boolean forced, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(!this.levels.isEmpty());
    }

    @Override
    public void initWorld(ServerLevel serverWorld, ServerLevelData worldInfo, WorldData saveData, WorldOptions worldOptions) {
        boolean flag = saveData.isDebugWorld();
        if ((serverWorld.bridge$generator() != null)) {
            serverWorld.getWorld().getPopulators().addAll(
                    (serverWorld.bridge$generator().getDefaultPopulators(
                            (serverWorld.getWorld()))));
        }
        WorldBorder worldborder = serverWorld.getWorldBorder();
        worldborder.applySettings(worldInfo.getWorldBorder());
        if (!worldInfo.isInitialized()) {
            try {
                setInitialSpawn(serverWorld, worldInfo, worldOptions.generateBonusChest(), flag);
                worldInfo.setInitialized(true);
                if (flag) {
                    this.setupDebugLevel(this.worldData);
                }
            } catch (Throwable throwable) {
                CrashReport crashreport = CrashReport.forThrowable(throwable, "Exception initializing level");
                try {
                    serverWorld.fillReportDetails(crashreport);
                } catch (Throwable throwable2) {
                    // empty catch block
                }
                throw new ReportedException(crashreport);
            }
            worldInfo.setInitialized(true);
        }
    }

    @Override
    public void prepareLevels(ChunkProgressListener listener, ServerLevel serverWorld) {
        if (!serverWorld.getWorld().getKeepSpawnInMemory()) {
            return;
        }
        this.forceTicks = true;
        LOGGER.info("Preparing start region for dimension {}", serverWorld.dimension().location());
        BlockPos blockpos = serverWorld.getSharedSpawnPos();
        listener.updateSpawnPos(new ChunkPos(blockpos));
        ServerChunkCache serverchunkprovider = serverWorld.getChunkSource();
        serverchunkprovider.getLightEngine().setTaskPerBatch(500);
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
        serverchunkprovider.getLightEngine().setTaskPerBatch(5);
        serverWorld.setSpawnSettings(this.isSpawningMonsters(), this.isSpawningAnimals());
        this.forceTicks = false;
    }

    @Override
    public void executeModerately() {
        this.runAllTasks();
        java.util.concurrent.locks.LockSupport.parkNanos("executing tasks", 1000L);
    }

    @Inject(method = "haveTime", cancellable = true, at = @At("HEAD"))
    private void bannerforceAheadOfTime(CallbackInfoReturnable<Boolean> cir) {
        if (this.forceTicks) cir.setReturnValue(true);
    }

    // CraftBukkit start
    public final java.util.concurrent.ExecutorService chatExecutor = java.util.concurrent.Executors.newCachedThreadPool(
            new com.google.common.util.concurrent.ThreadFactoryBuilder().setDaemon(true).setNameFormat("Async Chat Thread - #%d").build());

    @ModifyReturnValue(method = "getChatDecorator", at = @At("RETURN"))
    private ChatDecorator banner$fireChatEvent() {
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

    @Redirect(method = "saveAllChunks", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/storage/ServerLevelData;setWorldBorder(Lnet/minecraft/world/level/border/WorldBorder$Settings;)V"))
    private void banner$cancel0(ServerLevelData instance, WorldBorder.Settings settings) {}

    @Redirect(method = "saveAllChunks", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/storage/WorldData;setCustomBossEvents(Lnet/minecraft/nbt/CompoundTag;)V"))
    private void banner$cancel1(WorldData instance, CompoundTag compoundTag) {}

    @Redirect(method = "saveAllChunks", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/storage/LevelStorageSource$LevelStorageAccess;saveDataTag(Lnet/minecraft/core/RegistryAccess;Lnet/minecraft/world/level/storage/WorldData;Lnet/minecraft/nbt/CompoundTag;)V"))
    private void banner$cancel2(LevelStorageSource.LevelStorageAccess instance, RegistryAccess registries, WorldData serverConfiguration, CompoundTag hostPlayerNBT) {}

    // Banner start -- add to support plugins
    public String u() {
        return this.localIp;
    }

    public boolean U() {
        return this.onlineMode;
    }

    // Banner end

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
        return super.isSameThread(); //|| this.isStopped(); // CraftBukkit - MC-142590
    }
}
