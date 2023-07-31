package com.mohistmc.banner.libraries;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.impl.launch.FabricLauncher;
import net.fabricmc.loader.impl.launch.FabricLauncherBase;
import net.fabricmc.loader.util.UrlConversionException;
import net.fabricmc.loader.util.UrlUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.net.MalformedURLException;

public class KnotLibraryHelper {

    private static final Logger LOGGER = LogManager.getLogger("KnotLibraryHelper");

    public static void propose(File file) {
        try {
            FabricLauncher launcher = FabricLauncherBase.getLauncher();
            if (!FabricLoader.getInstance().isDevelopmentEnvironment()) {
                launcher.setAllowedPrefixes(FabricLoader.getInstance().getGameDir(), "net.minecraft.");
                launcher.setAllowedPrefixes(UrlUtil.asPath(file.toURI().toURL()));
                launcher.addToClassPath(UrlUtil.asPath(file.toURI().toURL()));
            }
        } catch (MalformedURLException | UrlConversionException e) {
            e.printStackTrace();
            LOGGER.info("ERROR: Got " + e.getClass().getSimpleName() + " while accessing Fabric Loader.");
        }
    }
}
