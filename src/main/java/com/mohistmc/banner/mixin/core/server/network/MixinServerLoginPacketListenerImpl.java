package com.mohistmc.banner.mixin.core.server.network;

import com.mohistmc.banner.injection.server.network.InjectionServerLoginPacketListenerImpl;
import com.mojang.authlib.GameProfile;
import net.minecraft.DefaultUncaughtExceptionHandler;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.Connection;
import net.minecraft.network.TickablePacketListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.login.ServerLoginPacketListener;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerLoginPacketListenerImpl;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.util.Waitable;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerPreLoginEvent;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import java.util.concurrent.atomic.AtomicInteger;

@Mixin(ServerLoginPacketListenerImpl.class)
public abstract class MixinServerLoginPacketListenerImpl implements ServerLoginPacketListener, TickablePacketListener, InjectionServerLoginPacketListenerImpl {

    @Shadow public abstract void disconnect(Component component);
    @Shadow @Final private MinecraftServer server;

    @Shadow @Final public Connection connection;
    @Shadow @Final private static AtomicInteger UNIQUE_THREAD_ID;

    @Shadow @Nullable private String requestedUsername;

    @Shadow @Final private static Logger LOGGER;

    @Shadow abstract void startClientVerification(GameProfile gameProfile);

    @Override
    public void disconnect(final String s) {
        this.disconnect(Component.literal(s));
    }

    @Redirect(method = "handleHello",
            at = @At(value = "INVOKE",
            target = "Lnet/minecraft/server/network/ServerLoginPacketListenerImpl;startClientVerification(Lcom/mojang/authlib/GameProfile;)V",
                    ordinal = 1))
    private void banner$handleHello(ServerLoginPacketListenerImpl instance, GameProfile gameProfile) {
        // CraftBukkit start
        class Handler extends Thread {

            public Handler() {
                super("User Authenticator #" + UNIQUE_THREAD_ID.incrementAndGet());
            }

            @Override
            public void run() {
                try {
                    GameProfile gameprofile = UUIDUtil.createOfflineProfile(requestedUsername);

                    callPlayerPreLoginEvents(gameprofile);
                    LOGGER.info("UUID of player {} is {}", gameprofile.getName(), gameprofile.getId());
                    startClientVerification(gameprofile);
                } catch (Exception ex) {
                    disconnect("Failed to verify username!");
                    server.bridge$server().getLogger().log(java.util.logging.Level.WARNING, "Exception verifying " + requestedUsername, ex);
                }
            }
        }
        Handler thread = new Handler();
        thread.setUncaughtExceptionHandler(new DefaultUncaughtExceptionHandler(LOGGER));
        thread.start();
        // CraftBukkit end
    }


    // CraftBukkit start
    @Override
    public void callPlayerPreLoginEvents(GameProfile gameprofile) throws Exception {
        String playerName = gameprofile.getName();
        java.net.InetAddress address = ((java.net.InetSocketAddress) connection.getRemoteAddress()).getAddress();
        java.util.UUID uniqueId = gameprofile.getId();
        final CraftServer server = this.server.bridge$server();

        AsyncPlayerPreLoginEvent asyncEvent = new AsyncPlayerPreLoginEvent(playerName, address, uniqueId);
        server.getPluginManager().callEvent(asyncEvent);

        if (PlayerPreLoginEvent.getHandlerList().getRegisteredListeners().length != 0) {
            final PlayerPreLoginEvent event = new PlayerPreLoginEvent(playerName, address, uniqueId);
            if (asyncEvent.getResult() != PlayerPreLoginEvent.Result.ALLOWED) {
                event.disallow(asyncEvent.getResult(), asyncEvent.getKickMessage());
            }
            Waitable<PlayerPreLoginEvent.Result> waitable = new Waitable<PlayerPreLoginEvent.Result>() {
                @Override
                protected PlayerPreLoginEvent.Result evaluate() {
                    server.getPluginManager().callEvent(event);
                    return event.getResult();
                }
            };

            this.server.bridge$processQueue().add(waitable);
            if (waitable.get() != PlayerPreLoginEvent.Result.ALLOWED) {
                disconnect(event.getKickMessage());
                return;
            }
        } else {
            if (asyncEvent.getLoginResult() != AsyncPlayerPreLoginEvent.Result.ALLOWED) {
                disconnect(asyncEvent.getKickMessage());
                return;
            }
        }
    }
    // CraftBukkit end

}