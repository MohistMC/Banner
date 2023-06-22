package com.mohistmc.banner;

import com.mohistmc.banner.eventhandler.BannerEventDispatcherRegistry;
import com.mohistmc.banner.stackdeobf.mappings.CachedMappings;
import com.mohistmc.banner.stackdeobf.mappings.providers.MojangMappingProvider;
import com.mohistmc.banner.stackdeobf.util.CompatUtil;
import com.mohistmc.banner.stackdeobf.util.RemappingRewritePolicy;
import io.izzel.arclight.mixin.injector.EjectorInfo;
import net.fabricmc.api.DedicatedServerModInitializer;
import org.apache.logging.log4j.LogManager;
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