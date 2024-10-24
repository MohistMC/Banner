package com.mohistmc.banner.stackdeobf.mappings.providers;
// Created by booky10 in StackDeobfuscator (20:56 30.03.23)

import com.mohistmc.banner.BannerMCStart;
import com.mohistmc.banner.config.BannerConfigUtil;
import com.mohistmc.banner.stackdeobf.http.HttpUtil;
import com.mohistmc.banner.stackdeobf.util.CompatUtil;
import com.mohistmc.banner.stackdeobf.util.MavenArtifactInfo;
import com.mohistmc.banner.util.I18n;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import net.fabricmc.mappingio.MappingReader;
import net.fabricmc.mappingio.MappingVisitor;
import net.fabricmc.mappingio.format.MappingFormat;
import net.fabricmc.mappingio.tree.MemoryMappingTree;

public class IntermediaryMappingProvider extends AbstractMappingProvider {

    private static final String REPO_SITE = BannerConfigUtil.isCN() ? "https://repository.hanbings.io/proxy" : "https://maven.fabricmc.net";
    private static final String REPO_URL =
            System.getProperty("stackdeobf.intermediary.repo-url", REPO_SITE);
    private static final MavenArtifactInfo MAPPINGS_ARTIFACT = MavenArtifactInfo.parse(REPO_URL,
            System.getProperty("stackdeobf.intermediary.mappings-artifact", "net.fabricmc:intermediary:v2"));

    private Path path;
    private MemoryMappingTree mappings;

    // only used as a conversion step (mojang + hashed quilt)
    IntermediaryMappingProvider() {
        super("intermediary");
    }

    @Override
    protected CompletableFuture<Void> downloadMappings0(Path cacheDir, Executor executor) {
        this.path = cacheDir.resolve("intermediary_" + CompatUtil.VERSION_ID + ".gz");
        if (Files.exists(this.path)) {
            return CompletableFuture.completedFuture(null);
        }

        URI uri = MAPPINGS_ARTIFACT.buildUri(CompatUtil.VERSION_ID, "jar");
        BannerMCStart.LOGGER.info(I18n.as("stackdeobf.downloading.intermediary"), CompatUtil.VERSION_ID);

        return HttpUtil.getAsync(uri, executor).thenAccept(jarBytes -> {
            byte[] mappingBytes = this.extractPackagedMappings(jarBytes);
            try (OutputStream fileOutput = Files.newOutputStream(this.path);
                 GZIPOutputStream gzipOutput = new GZIPOutputStream(fileOutput)) {
                gzipOutput.write(mappingBytes);
            } catch (IOException exception) {
                throw new RuntimeException(exception);
            }
        });
    }

    @Override
    protected CompletableFuture<Void> parseMappings0(Executor executor) {
        return CompletableFuture.supplyAsync(() -> {
            MemoryMappingTree mappings = new MemoryMappingTree();

            try (InputStream fileInput = Files.newInputStream(this.path);
                 GZIPInputStream gzipInput = new GZIPInputStream(fileInput);
                 Reader reader = new InputStreamReader(gzipInput)) {
                MappingReader.read(reader, MappingFormat.TINY_2_FILE, mappings);
            } catch (IOException exception) {
                throw new RuntimeException(exception);
            }

            this.mappings = mappings;
            return null;
        }, executor);
    }

    @Override
    protected CompletableFuture<Void> visitMappings0(MappingVisitor visitor, Executor executor) {
        throw new UnsupportedOperationException();
    }

    public Path getPath() {
        return this.path;
    }

    public MemoryMappingTree getMappings() {
        return this.mappings;
    }
}
