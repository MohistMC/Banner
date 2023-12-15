package com.mohistmc.banner.stackdeobf.mappings.providers;

// Created by booky10 in StackDeobfuscator (14:35 23.03.23)

import com.google.common.base.Preconditions;
import com.mohistmc.banner.stackdeobf.util.CompatUtil;
import com.mohistmc.banner.util.I18n;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.mappingio.MappingVisitor;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public abstract class AbstractMappingProvider {

    protected final String name;

    protected AbstractMappingProvider(String name) {
        this.name = name;
    }

    private static Path getCacheDir() {
        Path cacheDir;
        if (System.getProperties().containsKey("stackdeobf.mappings-cache-dir")) {
            cacheDir = Path.of(System.getProperty("stackdeobf.mappings-cache-dir"));
        } else {
            cacheDir = FabricLoader.getInstance().getGameDir().resolve("stackdeobf_mappings");
        }

        if (Files.notExists(cacheDir)) {
            try {
                Files.createDirectories(cacheDir);
            } catch (IOException exception) {
                throw new RuntimeException(exception);
            }
        }

        Preconditions.checkState(Files.isDirectory(cacheDir), cacheDir + " has to be a directory");
        return cacheDir;
    }

    public CompletableFuture<Void> cacheMappings(MappingVisitor visitor, Executor executor) {
        Path cacheDir = getCacheDir();

        return CompletableFuture.completedFuture(null)
                .thenComposeAsync($ -> this.downloadMappings(cacheDir, executor), executor)
                .thenCompose($ -> this.parseMappings(executor))
                .thenCompose($ -> this.visitMappings(visitor, executor));
    }

    protected byte[] extractPackagedMappings(byte[] jarBytes) {
        try {
            Path jarPath = Files.createTempFile(null, ".jar");
            try {
                Files.write(jarPath, jarBytes);
                try (FileSystem jar = FileSystems.newFileSystem(jarPath)) {
                    return Files.readAllBytes(jar.getPath("mappings", "mappings.tiny"));
                }
            } finally {
                Files.delete(jarPath);
            }
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    private CompletableFuture<Long> trackTime(CompletableFuture<Void> future) {
        long start = System.currentTimeMillis();
        return future.thenApply($ -> System.currentTimeMillis() - start);
    }

    private CompletableFuture<Void> downloadMappings(Path cacheDir, Executor executor) {
        CompatUtil.LOGGER.info(I18n.as("stackdeobf.verifying"), this.name);
        return this.trackTime(this.downloadMappings0(cacheDir, executor)).thenAccept(timeDiff ->
                CompatUtil.LOGGER.info(I18n.as("stackdeobf.verified"), this.name, timeDiff));
    }

    private CompletableFuture<Void> parseMappings(Executor executor) {
        CompatUtil.LOGGER.info(I18n.as("stackdeobf.parsing"), this.name);
        return this.trackTime(this.parseMappings0(executor)).thenAccept(timeDiff ->
                CompatUtil.LOGGER.info(I18n.as("stackdeobf.parsed"), this.name, timeDiff));
    }

    private CompletableFuture<Void> visitMappings(MappingVisitor visitor, Executor executor) {
        CompatUtil.LOGGER.info(I18n.as("stackdeobf.caching"), this.name);
        return this.trackTime(this.visitMappings0(visitor, executor)).thenAccept(timeDiff ->
                CompatUtil.LOGGER.info(I18n.as("stackdeobf.cached"), this.name, timeDiff));
    }

    protected abstract CompletableFuture<Void> downloadMappings0(Path cacheDir, Executor executor);

    protected abstract CompletableFuture<Void> parseMappings0(Executor executor);

    protected abstract CompletableFuture<Void> visitMappings0(MappingVisitor visitor, Executor executor);

    public String getName() {
        return this.name;
    }
}
