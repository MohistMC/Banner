package com.mohistmc.banner;

import com.mohistmc.banner.eventhandler.BannerEventDispatcherRegistry;
import com.mohistmc.i18n.i18n;
import io.izzel.arclight.mixin.injector.EjectorInfo;
import net.fabricmc.api.DedicatedServerModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.injection.struct.InjectionInfo;

import java.util.Locale;

public class BannerServer implements DedicatedServerModInitializer {

    public static final String MOD_ID = "banner";

    public static final Logger LOGGER =
            LoggerFactory.getLogger(MOD_ID);
    public static final float javaVersion = Float.parseFloat(System.getProperty("java.class.version"));
    public static i18n I18N;

    @Override
    public void onInitializeServer() {
        InjectionInfo.register(EjectorInfo.class);
        BannerEventDispatcherRegistry.registerEventDispatchers();
        String l = BannerConfig.banner_lang.split("_")[0];
        String c = BannerConfig.banner_lang.split("_")[1];
        I18N = new i18n(BannerServer.class.getClassLoader(), new Locale(l, c));
    }

    public static String getVersion() {
        try {
            Class<?> version = Class.forName("com.mohistmc.banner.VersionInfo");
            return (String) version.getField("VERSION").get(null);
        } catch (ClassNotFoundException | NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
            return "unknown";
        }
    }
}