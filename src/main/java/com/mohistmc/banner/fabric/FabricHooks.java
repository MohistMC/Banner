package com.mohistmc.banner.fabric;

public class FabricHooks {

    private static boolean markBiomeModified;

    public static void banner$captureBiomeModified(boolean markModified) {
        markBiomeModified = markModified;
    }

    public static boolean banner$getBiomeModified() {
        return markBiomeModified;
    }

}
