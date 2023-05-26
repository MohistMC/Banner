package com.mohistmc.banner.libraries;

import com.mohistmc.banner.util.JarLoader;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;

public class CustomLibraries {

    public static final File file = new File(FabricLoader.getInstance().getGameDir().toFile(),"libraries/customize_libraries");

    public static void loadCustomLibs() throws Exception {
        if (!file.exists()) {
            file.mkdirs();
        }

        for (File lib : file.listFiles((dir, name) -> name.endsWith(".jar"))) {
            if (!DefaultLibraries.getDefaultLibs().keySet().toString().contains(lib.getName())) {
                JarLoader.loadJar(lib.toPath());
                KnotLibraryHelper.propose(lib);
            }
            System.out.println(lib.getName() + " custom library loaded successfully.");
        }
    }
}
