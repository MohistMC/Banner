package com.mohistmc.banner;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.stream.Collectors;

public class BannerLauncher {

    public static void main(String[] args) {
        try {
            discoverFabricServer();
            setupModFile();
            launchServer(args);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void launchServer(String[] args) {
        try {
            Class<?> clazz = Class.forName("net.fabricmc.loader.impl.launch.server.FabricServerLauncher");
            Method method = clazz.getDeclaredMethod("main", String[].class);
            URLClassLoader classLoader = new URLClassLoader(new URL[]{}, ClassLoader.getSystemClassLoader());
            method.invoke(classLoader, (Object) args);
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    private static void discoverFabricServer() {
        try {
            File serverJar = new File("fabric-server-launch.jar");
            JarFile jarFile = new JarFile(serverJar);
            Manifest manifest = jarFile.getManifest();
            Attributes attributes = manifest.getMainAttributes();
            try (InputStream stream = BannerLauncher.class.getModule().getResourceAsStream("/META-INF/MANIFEST.MF")) {
                Manifest manifest1 = new Manifest(stream);
                Attributes attributes1 = manifest1.getMainAttributes();
                attributes1.putValue("Class-Path", attributes.getValue(Attributes.Name.CLASS_PATH));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void setupModFile() throws Exception {
        try (InputStream stream = BannerLauncher.class.getModule().getResourceAsStream("/META-INF/MANIFEST.MF")) {
            Manifest manifest = new Manifest(stream);
            Attributes attributes = manifest.getMainAttributes();
            String version = attributes.getValue(Attributes.Name.IMPLEMENTATION_VERSION);
            extractJar(BannerLauncher.class.getModule().getResourceAsStream("META-INF/jars/banner-common.jar"), version);
            System.setProperty("fabric.addMods", ".banner/mod_file/banner-" + version + ".jar");
        }
    }

    private static void extractJar(InputStream path, String version) throws Exception {
        System.setProperty("banner.version", version);
        var dir = Paths.get(".banner", "mod_file");
        if (!Files.exists(dir)) {
            Files.createDirectories(dir);
        }
        var mod = dir.resolve("banner-" + version + ".jar");
        if (!Files.exists(mod)) {
            for (Path old : Files.list(dir).collect(Collectors.toList())) {
                Files.delete(old);
            }
            Files.copy(path, mod);
        }
    }
}
