package com.mohistmc.banner;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

public class BannerMain {

    public static void main(String[] args) throws Throwable {
        System.setProperty("fabric.skipMcProvider", "true");
        System.setProperty("banner.alwaysExtract", "true");
        try {
            var install = fabricInstall();
            var ours = BannerMain.class.getProtectionDomain().getCodeSource().getLocation();
            var classloader = new URLClassLoader(Stream.concat(Stream.of(ours), install.getValue().stream().map(it -> {
                try {
                    return it.toUri().toURL();
                } catch (MalformedURLException e) {
                    throw new RuntimeException(e);
                }
            })).toArray(URL[]::new), ClassLoader.getPlatformClassLoader());
            Thread.currentThread().setContextClassLoader(classloader);
            var cl = Class.forName(install.getKey(), false, classloader);
            var handle = MethodHandles.lookup().findStatic(cl, "main", MethodType.methodType(void.class, String[].class));
            handle.invoke((Object) args);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Fail to launch Banner.");
            System.exit(-1);
        }
    }

    private static Path extractMC() throws Exception {
        var path = BannerMain.class.getModule().getResourceAsStream("/META-INF/jars/server-1.20.1.jar");
        var dir = Paths.get("libraries", "net/minecraft/server/1.20.1");
        if (!Files.exists(dir)) {
            Files.createDirectories(dir);
        }
        var mc = dir.resolve("server-1.20.1.jar");
        if (!Files.exists(mc)) {
            try (var files = Files.list(dir)) {
                for (Path old : files.toList()) {
                    Files.delete(old);
                }
                Files.copy(path, mc);
            }
        }
        return mc;
    }

    @SuppressWarnings("unchecked")
    private static Map.Entry<String, List<Path>> fabricInstall() throws Throwable {
        var path = Paths.get(".banner", "gson.jar");
        if (!Files.exists(path)) {
            Files.createDirectories(path.getParent());
            Files.copy(Objects.requireNonNull(BannerMain.class.getResourceAsStream("/gson.jar")), path);
        }
        try {
            extractMC();
        } catch (Exception e) {
            System.out.println("Failed to extract MC Jar");
        }
        try (var loader = new URLClassLoader(new URL[]{path.toUri().toURL(), BannerMain.class.getProtectionDomain().getCodeSource().getLocation()}, ClassLoader.getPlatformClassLoader())) {
            var cl = loader.loadClass("com.mohistmc.banner.install.FabricInstaller");
            var handle = MethodHandles.lookup().findStatic(cl, "applicationInstall", MethodType.methodType(Map.Entry.class));
            return (Map.Entry<String, List<Path>>) handle.invoke();
        }
    }
}
