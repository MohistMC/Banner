// CHECKSTYLE:OFF
package org.bukkit.plugin.java;

import com.mohistmc.banner.BannerServer;
import com.mohistmc.banner.bukkit.nms.proxy.DelegateURLClassLoder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.thread.NamedThreadFactory;
import org.bukkit.plugin.PluginDescriptionFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

class LibraryLoader {

    public static final ScheduledExecutorService LIBRARY_LOADER = new ScheduledThreadPoolExecutor(10, new NamedThreadFactory("LibraryLoader"));

    public LibraryLoader() {
    }

    @Nullable
    public ClassLoader createLoader(@NotNull PluginDescriptionFile desc) {
        if (desc.getLibraries().isEmpty()) {
            return null;
        }
        BannerServer.LOGGER.info("[{}] Loading {} libraries... please wait", desc.getName(), desc.getLibraries().size());

        List<Dependency> dependencies = new ArrayList<>();
        for (String libraries : desc.getLibraries()) {
            String[] args = libraries.split(":");
            if (args.length > 1) {
                Dependency dependency = new Dependency(args[0], args[1], args[2]);
                dependencies.add(dependency);
            }

        }

        List<File> libraries = new ArrayList<>();

        for (Dependency dependency : dependencies) {
            String group = dependency.group().replaceAll("\\.", "/");
            String fileName = "%s-%s.jar".formatted(dependency.name(), dependency.version());
            String mavenUrl = "https://repo.maven.apache.org/maven2/%s/%s/%s/%s".formatted(group, dependency.name(), dependency.version(), fileName);


            File file = new File(new File(FabricLoader.getInstance().getGameDir().toFile(), "libraries/spigot-lib"), "%s/%s/%s/%s".formatted(group, dependency.name(), dependency.version(), fileName));

            if (file.exists()) {
                BannerServer.LOGGER.info("[{}] Found libraries {}", desc.getName(), file);
                libraries.add(file);
                continue;
            }

            Future<Boolean> future = LIBRARY_LOADER.submit(() -> {
                file.getParentFile().mkdirs();
                file.createNewFile();

                try {
                    InputStream inputStream = new URL(mavenUrl).openStream();
                    Files.copy(inputStream, file.toPath());
                    libraries.add(file);
                    return true;
                } catch (IOException e) {
                    return false;
                }
            });


            try {
                if (future.get()) {
                    BannerServer.LOGGER.info("[{}] Downloading libraries {}", desc.getName(), mavenUrl);
                }
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        }

        List<URL> jarFiles = new ArrayList<>();
        for (File file : libraries) {
            try {
                jarFiles.add(file.toURI().toURL());
                BannerServer.LOGGER.info("[{}] Loaded libraries {}", desc.getName(), file);
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        }

        return new DelegateURLClassLoder(jarFiles.toArray(new URL[0]), getClass().getClassLoader());
    }

    public record Dependency(String group, String name, String version) {}
}
