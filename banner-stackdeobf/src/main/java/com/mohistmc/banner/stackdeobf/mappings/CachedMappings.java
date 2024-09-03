package com.mohistmc.banner.stackdeobf.mappings;

// Created by booky10 in StackDeobfuscator (17:04 20.03.23)

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.mohistmc.banner.BannerMCStart;
import com.mohistmc.banner.stackdeobf.mappings.providers.AbstractMappingProvider;
import com.mohistmc.banner.util.I18n;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.jetbrains.annotations.Nullable;

public final class CachedMappings {

    // "CLASSES" name has package prefixed (separated by '.')
    private static final Int2ObjectMap<String> CLASSES = Int2ObjectMaps.synchronize(new Int2ObjectOpenHashMap<>());
    private static final Int2ObjectMap<String> METHODS = Int2ObjectMaps.synchronize(new Int2ObjectOpenHashMap<>());
    private static final Int2ObjectMap<String> FIELDS = Int2ObjectMaps.synchronize(new Int2ObjectOpenHashMap<>());

    private CachedMappings() {
    }

    public static void init(AbstractMappingProvider provider) {
        BannerMCStart.LOGGER.info(I18n.as("stackdeobf.creating"));
        ExecutorService cacheExecutor = Executors.newSingleThreadExecutor(
                new ThreadFactoryBuilder().setNameFormat("Mappings Cache Thread").setDaemon(true).build());
        long start = System.currentTimeMillis();

        // visitor expects mappings to be intermediary -> named
        provider.cacheMappings(new MappingCacheVisitor(CLASSES, METHODS, FIELDS), cacheExecutor)
                .thenAccept($ -> {
                    long timeDiff = System.currentTimeMillis() - start;
                    BannerMCStart.LOGGER.info(I18n.as("stackdeobf.cached.mappings"), timeDiff);

                    BannerMCStart.LOGGER.info(" " + I18n.as("stackdeobf.classes") + " " + CLASSES.size());
                    BannerMCStart.LOGGER.info(" " + I18n.as("stackdeobf.methods") + " " + METHODS.size());
                    BannerMCStart.LOGGER.info(" " + I18n.as("stackdeobf.fields") + " "  + FIELDS.size());
                })
                // needs to be executed asynchronously, otherwise the
                // executor of the current thread would be shut down
                .thenRunAsync(() -> {
                    BannerMCStart.LOGGER.info(I18n.as("stackdeobf.shutting.down"));
                    BannerMCStart.LOGGER.info(I18n.as("load.libraries"));
                    cacheExecutor.shutdown();
                });
    }

    public static @Nullable String remapClass(int id) {
        return CLASSES.get(id);
    }

    public static @Nullable String remapMethod(int id) {
        return METHODS.get(id);
    }

    public static @Nullable String remapField(int id) {
        return FIELDS.get(id);
    }
}
