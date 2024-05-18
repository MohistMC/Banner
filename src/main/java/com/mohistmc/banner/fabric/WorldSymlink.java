package com.mohistmc.banner.fabric;

import com.mohistmc.banner.BannerServer;
import com.mohistmc.banner.util.I18n;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import net.minecraft.world.level.storage.DerivedLevelData;
import org.bukkit.Bukkit;

public class WorldSymlink {

    public static void create(DerivedLevelData worldInfo, File dimensionFolder) {
        String name = worldInfo.getLevelName();
        Path source = new File(Bukkit.getWorldContainer(), name).toPath();
        Path dest = dimensionFolder.toPath();
        try {
            if (!Files.isSymbolicLink(source)) {
                if (Files.exists(source)) {
                    BannerServer.LOGGER.warn(I18n.as("symlink-file-exist"), source);
                    return;
                }
                Files.createSymbolicLink(source, dest);
            }
        } catch (Exception ignored) {
        }
    }
}