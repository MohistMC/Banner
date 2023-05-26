package com.mohistmc.banner.libraries;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.Version;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;

public class KnotLibraryHelper {

    private static final Logger LOGGER = LogManager.getLogger("KnotLibraryHelper");

    public static void outdated_fabric_warning(double ver) {
        LOGGER.error("======== ERROR: FABRIC OUTDATED ========");
        LOGGER.error("| Your version of Fabric is outdated!!");
        LOGGER.error("| You version is: " + ver);
        LOGGER.error("| Lowest Required: 0.12 or higher");
        LOGGER.error("| Update at: https://fabricmc.dev/use/");
        LOGGER.error("=======================================");
    }

    /**
     * Add to class path in Fabric 0.11
     */
    public static void fabric_0_11_load(File file) {
        try {
            Class<?> l = Class.forName("net.fabricmc.loader.launch.knot.Knot");
            Method m = l.getMethod("getLauncher");
            Object lb = m.invoke(null, (Object) null);
            Method m2 = lb.getClass().getMethod("propose", URL.class);
            m2.invoke(lb, file.toURI().toURL());
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.info("ERROR: Got " + e.getClass().getSimpleName() + " while accessing Fabric Loader.");
        }
    }

    /**
     * Add to class path in Fabric 0.12 or higher
     */
    public static void fabric_modern_load(File file) {
        try {
            Class<?> l = Class.forName("net.fabricmc.loader.impl.launch.FabricLauncherBase");
            Field instance = l.getDeclaredField("launcher");
            instance.setAccessible(true);
            Object lb = instance.get(null);
            Class<?> lbc = lb.getClass();
            Method m = lbc.getMethod("addToClassPath", Path.class, String[].class);

            if (!FabricLoader.getInstance().isDevelopmentEnvironment())
                m.invoke(lb, file.toPath(), getPackages());
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.info("ERROR: Got " + e.getClass().getSimpleName() + " while accessing Fabric Loader.");
        }
    }

    public static void propose(File file) throws MalformedURLException {
        Version loaderVersion = FabricLoader.getInstance().getModContainer("fabricloader").get().getMetadata().getVersion();
        String verString = loaderVersion.getFriendlyString();
        verString = verString.substring(0, verString.lastIndexOf('.'));
        double ver = Double.parseDouble( verString );

        propose_file(file, ver);
    }

    public static void propose_file(File file, double ver) {
        if (ver < 0.11) {
            outdated_fabric_warning(ver);
            return;
        }

        if (ver < 0.12) {
            fabric_0_11_load(file);
        }

        if (ver >= 0.12) {
            fabric_modern_load(file);
        }
    }

    public static String[] getPackages() {
        return new String[] {
                "net.md_5.",
                "org.bukkit",
                "com.mohistmc",
                "com.",
                "net.",
                "org.",
                "org.eclipse.aether.",
                "org.eclipse.aether.transport.http",
                "org.eclipse.aether.connector.basic.",
                "org.eclipse.aether.spi.",
                "io.izzel",
                "jline.",
                "org.yaml.",
                "org.fusesource.jansi.",
                "com.googlecode.json-simple.",
                "org.xerial.",
                "com.mysql.",
                "org.apache.logging.log4j.",
                "org.apache.maven.repository.internal.",
                "commons-io.",
                "commons-lang.",
                "org.checkerframework.",
                "net.md_5.bungee",
                "net.md_5.bungee.",
                "net.md_5.bungee.chat."
        };
    }
}
