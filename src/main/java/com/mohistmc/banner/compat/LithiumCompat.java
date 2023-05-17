package com.mohistmc.banner.compat;

import com.mohistmc.banner.BannerServer;
import net.fabricmc.loader.api.FabricLoader;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class LithiumCompat {

    public static void changeLithiumConf() throws IOException {
        if (FabricLoader.getInstance().isModLoaded("lithium")) {
            File file = new File(FabricLoader.getInstance().getConfigDir().toFile(), "lithium.properties");
            if (!file.exists()) {
                file.mkdir();
            }else {
                for (String s : Files.readAllLines(file.toPath(), StandardCharsets.UTF_8)) {
                    if (s.contains("mixin.block.hopper") && s.contains("false")) {
                        return;
                    }else {
                        try {
                            FileWriter fileWriter = new FileWriter(file);
                            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
                            bufferedWriter.write("mixin.block.hopper=false");
                            bufferedWriter.flush();
                            bufferedWriter.close();
                        } catch (IOException e) {
                            BannerServer.LOGGER.error("[Banner] Failed to fix lithium's config");
                        }
                    }
                }
            }
        }
    }
}
