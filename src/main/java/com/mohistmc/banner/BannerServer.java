package com.mohistmc.banner;

import com.mohistmc.banner.eventhandler.BannerEventDispatcherRegistry;
import com.mohistmc.i18n.i18n;
import com.llamalad7.mixinextras.MixinExtrasBootstrap;
import io.izzel.arclight.mixin.injector.EjectorInfo;
import net.fabricmc.api.DedicatedServerModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.injection.struct.InjectionInfo;

public class BannerServer implements DedicatedServerModInitializer {

    public static final String MOD_ID = "banner";
    public static i18n I18N;

    public static final Logger LOGGER =
            LoggerFactory.getLogger(MOD_ID);
    public static final float javaVersion = Float.parseFloat(System.getProperty("java.class.version"));

    @Override
    public void onInitializeServer() {
        if (System.getProperty("log4j.configurationFile") == null) {
            System.setProperty("log4j.configurationFile", "log4j2_banner.xml");
        }
        MixinExtrasBootstrap.init();
        InjectionInfo.register(EjectorInfo.class);
        BannerEventDispatcherRegistry.registerEventDispatchers();
    }

    public static String getVersion() {
        return (BannerServer.class.getPackage().getImplementationVersion() != null) ? BannerServer.class.getPackage().getImplementationVersion() : "unknown";
    }
}