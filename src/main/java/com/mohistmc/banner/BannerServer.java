package com.mohistmc.banner;

import com.mohistmc.banner.config.BannerConfig;
import com.mohistmc.banner.fabric.FabricInjectBukkit;
import com.mohistmc.i18n.i18n;
import com.llamalad7.mixinextras.MixinExtrasBootstrap;
import net.fabricmc.api.DedicatedServerModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;

public class BannerServer implements DedicatedServerModInitializer {

    public static final String MOD_ID = "banner";
    public static i18n I18N;

    public static final Logger LOGGER =
            LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitializeServer() {
        MixinExtrasBootstrap.init();
        String l = BannerConfig.banner_lang.split("_")[0];
        String c = BannerConfig.banner_lang.split("_")[1];
        I18N = new i18n(BannerServer.class.getClassLoader(), new Locale(l, c));
        LOGGER.info(I18N.get("banner.welcome"));
    }
}