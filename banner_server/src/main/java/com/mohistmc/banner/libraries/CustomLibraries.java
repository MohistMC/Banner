package com.mohistmc.banner.libraries;

import java.io.File;
import net.fabricmc.loader.api.FabricLoader;

public class CustomLibraries {

    public static final File file = new File(FabricLoader.getInstance().getGameDir().toFile(),"libraries/customize_libraries");

    public static void loadCustomLibs() throws Exception {
        if (!file.exists()) {
            file.mkdirs();
        }

        for (File lib : file.listFiles((dir, name) -> name.endsWith(".jar"))) {
            if (!DefaultLibraries.getDefaultLibs().keySet().toString().contains(lib.getName())) {
                KnotLibraryHelper.propose(lib);
            }
            System.out.println(lib.getName() + " custom library loaded successfully.");
        }
    }
}
