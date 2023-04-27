package com.mohistmc.banner.bukkit;

import com.mohistmc.banner.BannerServer;
import net.minecraft.SharedConstants;
import net.minecraft.server.packs.PackType;

import java.io.File;

public class BukkitDataPackGenerator {

    public static void createBukkitDataPack(File file) {
        File bukkitDataPackFolder = new File(file, "bukkit");
        if (!bukkitDataPackFolder.exists()) {
            bukkitDataPackFolder.mkdirs();
            BannerServer.LOGGER.info("Creating Bukkit datapack...");
        }
        File mcMeta = new File(bukkitDataPackFolder, "pack.mcmeta");
        if (!mcMeta.exists()) {
            try {
                com.google.common.io.Files.write("{\n"
                        + "    \"pack\": {\n"
                        + "        \"description\": \"Data pack for resources provided by Bukkit plugins\",\n"
                        + "        \"pack_format\": " + SharedConstants.getCurrentVersion().getPackVersion(PackType.SERVER_DATA) + "\n"
                        + "    }\n"
                        + "}\n", mcMeta, com.google.common.base.Charsets.UTF_8);
            } catch (java.io.IOException ex) {
                throw new RuntimeException("Could not initialize Bukkit datapack", ex);
            }
        }
    }

}
