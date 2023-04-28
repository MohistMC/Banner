package com.mohistmc.banner.mixin.server.dedicated;

import com.mohistmc.banner.BannerServer;
import com.mojang.datafixers.DataFixer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.ConsoleInput;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.Services;
import net.minecraft.server.WorldStem;
import net.minecraft.server.dedicated.DedicatedPlayerList;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.dedicated.DedicatedServerProperties;
import net.minecraft.server.level.progress.ChunkProgressListenerFactory;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.rcon.RconConsoleSource;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_19_R3.CraftServer;
import org.bukkit.craftbukkit.v1_19_R3.command.ColouredConsoleSender;
import org.bukkit.craftbukkit.v1_19_R3.command.CraftRemoteConsoleCommandSender;
import org.bukkit.event.server.RemoteServerCommandEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.plugin.PluginLoadOrder;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.net.Proxy;

@Mixin(DedicatedServer.class)
public abstract class MixinDedicatedServer extends MinecraftServer {

    @Shadow public abstract DedicatedPlayerList getPlayerList();

    @Shadow @Final public RconConsoleSource rconConsoleSource;

    @Shadow public abstract DedicatedServerProperties getProperties();

    public MixinDedicatedServer(Thread thread, LevelStorageSource.LevelStorageAccess levelStorageAccess, PackRepository packRepository, WorldStem worldStem, Proxy proxy, DataFixer dataFixer, Services services, ChunkProgressListenerFactory chunkProgressListenerFactory) {
        super(thread, levelStorageAccess, packRepository, worldStem, proxy, dataFixer, services, chunkProgressListenerFactory);
    }

    @Inject(method = "initServer", at = @At("HEAD"))
    private void banner$startInit(CallbackInfoReturnable<Boolean> cir) {
        ((DedicatedServer) (Object) this).setPlayerList(
                new DedicatedPlayerList((DedicatedServer) (Object) this,
                        this.registries(), this.playerDataStorage));
        this.banner$setServer(new CraftServer(((DedicatedServer) (Object) this), this.getPlayerList()));
        this.banner$setConsole(ColouredConsoleSender.getInstance());
    }

    @Inject(method = "initServer", at = @At(value = "JUMP", ordinal = 8))
    private void banner$setupServer(CallbackInfoReturnable<Boolean> cir) {
        LOGGER.info(" _____       ___   __   _   __   _   _____   _____   ");
        LOGGER.info("|  _  \\     /   | |  \\ | | |  \\ | | | ____| |  _  \\  ");
        LOGGER.info("| |_| |    / /| | |   \\| | |   \\| | | |__   | |_| |  ");
        LOGGER.info("|  _  {   / / | | | |\\   | | |\\   | |  __|  |  _  /  ");
        LOGGER.info("| |_| |  / /  | | | | \\  | | | \\  | | |___  | | \\ \\  ");
        LOGGER.info("|_____/ /_/   |_| |_|  \\_| |_|  \\_| |_____| |_|  \\_\\ ");
        BannerServer.LOGGER.info("Loading Bukkit plugins...");
        ((CraftServer) Bukkit.getServer()).loadPlugins();
        ((CraftServer) Bukkit.getServer()).enablePlugins(PluginLoadOrder.STARTUP);
    }

    @Inject(method = "initServer", at = @At("RETURN"))
    private void banner$finishInit(CallbackInfoReturnable<Boolean> cir) {
        CraftServer cbServer = (CraftServer) Bukkit.getServer();
        cbServer.enablePlugins(PluginLoadOrder.POSTWORLD);
    }


    @Inject(method = "initServer", at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/server/rcon/thread/RconThread;create(Lnet/minecraft/server/ServerInterface;)Lnet/minecraft/server/rcon/thread/RconThread;"))
    public void banner$setRcon(CallbackInfoReturnable<Boolean> cir) {
        this.banner$setRemoteConsole(new CraftRemoteConsoleCommandSender(this.rconConsoleSource));
    }

    @Inject(method = "initServer", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/server/dedicated/DedicatedServer;setPvpAllowed(Z)V",
            shift = At.Shift.BEFORE))
    private void banner$addConfig(CallbackInfoReturnable<Boolean> cir) {
        // Spigot start
        org.spigotmc.SpigotConfig.init((java.io.File) bridge$options().valueOf("spigot-settings"));
        org.spigotmc.SpigotConfig.registerCommands();
        // Spigot end
    }

    @Inject(method = "getPluginNames", at = @At("RETURN"), cancellable = true)
    private void banner$setPluginNames(CallbackInfoReturnable<String> cir) {
        StringBuilder result = new StringBuilder();
        org.bukkit.plugin.Plugin[] plugins = bridge$server().getPluginManager().getPlugins();

        result.append(bridge$server().getName());
        result.append(" on Bukkit ");
        result.append(bridge$server().getBukkitVersion());

        if (plugins.length > 0 && bridge$server().getQueryPlugins()) {
            result.append(": ");

            for (int i = 0; i < plugins.length; i++) {
                if (i > 0) {
                    result.append("; ");
                }

                result.append(plugins[i].getDescription().getName());
                result.append(" ");
                result.append(plugins[i].getDescription().getVersion().replaceAll(";", ","));
            }
        }

        cir.setReturnValue(result.toString());
    }

    @Redirect(method = "handleConsoleInputs", at = @At(value = "INVOKE", target = "Lnet/minecraft/commands/Commands;performPrefixedCommand(Lnet/minecraft/commands/CommandSourceStack;Ljava/lang/String;)I"))
    private int banner$serverCommandEvent(Commands commands, CommandSourceStack source, String command) {
        if (command.isEmpty()) {
            return 0;
        }
        ServerCommandEvent event = new ServerCommandEvent(bridge$console(), command);
        Bukkit.getPluginManager().callEvent(event);
        if (!event.isCancelled()) {
            bridge$server().dispatchServerCommand(bridge$console(), new ConsoleInput(event.getCommand(), source));
        }
        return 0;
    }

    /**
     * @author wdog5
     * @reason
     */
    @Overwrite
    public String runCommand(String command) {
        this.rconConsoleSource.prepareForCommand();
        this.executeBlocking(() -> {
            RemoteServerCommandEvent event = new RemoteServerCommandEvent(bridge$remoteConsole(), command);
            Bukkit.getPluginManager().callEvent(event);
            if (event.isCancelled()) {
                return;
            }
            this.bridge$server().dispatchServerCommand(bridge$remoteConsole(), new ConsoleInput(event.getCommand(), this.rconConsoleSource.createCommandSourceStack()));
        });
        return this.rconConsoleSource.getCommandResponse();
    }

    @Override
    public CommandSender getBukkitSender(CommandSourceStack wrapper) {
        return bridge$console();
    }
}
