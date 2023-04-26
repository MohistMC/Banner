package com.mohistmc.banner;

import com.mohistmc.banner.config.BannerConfig;
import com.mohistmc.i18n.i18n;
import com.llamalad7.mixinextras.MixinExtrasBootstrap;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;

public class BannerMod implements ModInitializer {

    public static final String MOD_ID = "banner";

    public static final Logger LOGGER =
            LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        MixinExtrasBootstrap.init();
        String l = BannerConfig.banner_lang.split("_")[0];
        String c = BannerConfig.banner_lang.split("_")[1];
        new i18n().build(BannerMod.class.getClassLoader(), new Locale(l, c));
        LOGGER.info(i18n.get("banner.welcome"));
    }
}