package com.mohistmc.banner;

import com.mohistmc.banner.eventhandler.BannerEventDispatcherRegistry;
import io.izzel.arclight.mixin.injector.EjectorInfo;
import net.fabricmc.api.DedicatedServerModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.injection.struct.InjectionInfo;

public class BannerServer implements DedicatedServerModInitializer {

    public static final String MOD_ID = "banner";

    public static final Logger LOGGER =
            LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitializeServer() {
        InjectionInfo.register(EjectorInfo.class);
        BannerEventDispatcherRegistry.registerEventDispatchers();
    }

}