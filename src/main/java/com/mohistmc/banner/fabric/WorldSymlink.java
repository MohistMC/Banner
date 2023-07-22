package com.mohistmc.banner.fabric;

import com.mohistmc.banner.BannerMCStart;
import com.mohistmc.banner.BannerServer;
import org.bukkit.Bukkit;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import net.minecraft.world.level.storage.DerivedLevelData;

public class WorldSymlink {

    public static void create(DerivedLevelData worldInfo, File dimensionFolder) {
        String name = worldInfo.getLevelName();
        Path source = new File(Bukkit.getWorldContainer(), name).toPath();
        Path dest = dimensionFolder.toPath();
        try {
            if (!Files.isSymbolicLink(source)) {
                if (Files.exists(source)) {
                    BannerServer.LOGGER.warn(BannerMCStart.I18N.get("symlink-file-exist"), source);
                    return;
                }
                Files.createSymbolicLink(source, dest);
            }
        } catch (UnsupportedOperationException e) {
            BannerServer.LOGGER.warn(BannerMCStart.I18N.get("error-symlink"), e);
        } catch (IOException e) {
            BannerServer.LOGGER.error("Error creating symlink", e);
        }
    }
}
