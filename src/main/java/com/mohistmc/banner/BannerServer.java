package com.mohistmc.banner;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.mohistmc.banner.config.BannerConfig;
import com.mohistmc.banner.eventhandler.BannerEventDispatcherRegistry;
import com.mohistmc.i18n.i18n;
import com.llamalad7.mixinextras.MixinExtrasBootstrap;
import net.fabricmc.api.DedicatedServerModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class BannerServer implements DedicatedServerModInitializer {

    public static final String MOD_ID = "banner";
    public static i18n I18N;

    public static final Logger LOGGER =
            LoggerFactory.getLogger(MOD_ID);
    private static final ExecutorService chatExecutor = Executors.newCachedThreadPool(
            new ThreadFactoryBuilder().setDaemon(true).setNameFormat("Async Chat Thread - #%d")
                    .setThreadFactory(chatFactory()).build());

    @Override
    public void onInitializeServer() {
        if (System.getProperty("log4j.configurationFile") == null) {
            System.setProperty("log4j.configurationFile", "log4j2_banner.xml");
        }
        MixinExtrasBootstrap.init();
        String l = BannerConfig.banner_lang.split("_")[0];
        String c = BannerConfig.banner_lang.split("_")[1];
        I18N = new i18n(BannerServer.class.getClassLoader(), new Locale(l, c));
        LOGGER.info(I18N.get("banner.welcome"));
        BannerEventDispatcherRegistry.registerEventDispatchers();
    }

    private static ThreadFactory chatFactory() {
        var group = Thread.currentThread().getThreadGroup();
        var classLoader = Thread.currentThread().getContextClassLoader();
        return r -> {
            var thread = new Thread(group, r);
            thread.setContextClassLoader(classLoader);
            return thread;
        };
    }

    public static ExecutorService getChatExecutor() {
        return chatExecutor;
    }
}