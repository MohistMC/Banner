package com.mohistmc.banner.bukkit.remapping;


/**
 * RemappingClassLoader
 *
 * @author Mainly by IzzelAliz
 * @originalClassName RemappingClassLoader
 */
public interface RemappingClassLoader {

    ClassLoaderRemapper getRemapper();

    static ClassLoader asTransforming(ClassLoader classLoader) {
        boolean found = false;
        while (classLoader != null) {
            if (classLoader instanceof RemappingClassLoader) {
                found = true;
                break;
            } else {
                classLoader = classLoader.getParent();
            }
        }
        return found ? classLoader : RemappingClassLoader.class.getClassLoader();
    }
}
