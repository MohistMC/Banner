package com.mohistmc.banner.stackdeobf.mappings.providers;
// Created by booky10 in StackDeobfuscator (22:08 23.03.23)

import com.mohistmc.banner.stackdeobf.http.HttpUtil;
import com.mohistmc.banner.stackdeobf.util.CompatUtil;
import com.mohistmc.banner.stackdeobf.util.MavenArtifactInfo;
import com.mohistmc.banner.util.I18n;
import net.fabricmc.mappingio.MappingReader;
import net.fabricmc.mappingio.MappingVisitor;
import net.fabricmc.mappingio.format.MappingFormat;
import net.fabricmc.mappingio.tree.MemoryMappingTree;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
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

public class BuildBasedMappingProvider extends AbstractMappingProvider {

    protected final MavenArtifactInfo artifactInfo;

    protected Path path;
    protected MemoryMappingTree mappings;

    public BuildBasedMappingProvider(String name, MavenArtifactInfo artifactInfo) {
        super(name);
        this.artifactInfo = artifactInfo;
    }

    @Override
    protected CompletableFuture<Void> downloadMappings0(Path cacheDir, Executor executor) {
        // after 1.14.2, fabric switched to using the version id instead of the name for yarn versions
        String version = CompatUtil.WORLD_VERSION >= 1963 ? CompatUtil.VERSION_ID : CompatUtil.VERSION_NAME;

        // versions somewhere before mojang mappings (I don't have decompiled mc versions
        // before mojang mappings) include the current commit hash in the version.json name
        version = StringUtils.split(version, ' ')[0];

        return this.fetchLatestVersion(cacheDir, version, executor)
                .thenCompose(build -> {
                    this.path = cacheDir.resolve(this.name + "_" + build + ".gz");

                    // already cached, don't download anything
                    if (Files.exists(this.path)) {
                        CompatUtil.LOGGER.info(I18n.as("stackdeobf.download.already"), this.name, build);
                        return CompletableFuture.completedFuture(null);
                    }

                    URI uri = this.artifactInfo.buildUri(build, "jar");
                    CompatUtil.LOGGER.info(I18n.as("stackdeobf.downloading"), this.name, build);

                    return HttpUtil.getAsync(uri, executor).thenAccept(mappingJarBytes -> {
                        byte[] mappingBytes = this.extractPackagedMappings(mappingJarBytes);
                        try (OutputStream fileOutput = Files.newOutputStream(this.path);
                             GZIPOutputStream gzipOutput = new GZIPOutputStream(fileOutput)) {
                            gzipOutput.write(mappingBytes);
                        } catch (IOException exception) {
                            throw new RuntimeException(exception);
                        }
                    });
                });
    }

    private CompletableFuture<String> fetchLatestVersion(Path cacheDir, String mcVersion, Executor executor) {
        return CompletableFuture.completedFuture(null).thenComposeAsync($ -> {
            Path versionCachePath = cacheDir.resolve(this.name + "_" + mcVersion + "_latest.txt");
            if (Files.exists(versionCachePath)) {
                try {
                    long lastVersionFetch = Files.getLastModifiedTime(versionCachePath).toMillis();
                    long timeDiff = (System.currentTimeMillis() - lastVersionFetch) / 1000 / 60;

                    long maxTimeDiff = Long.getLong("stackdeobf.build-refresh-cooldown", 2 * 24 * 60 /* specified in minutes */);
                    if (timeDiff <= maxTimeDiff) {
                        // latest build has already been fetched in the last x minutes (default: 2 days)
                        CompatUtil.LOGGER.info(I18n.as("stackdeobf.getbuild"),
                                this.name, (long) Math.floor(timeDiff / 60d), (long) Math.ceil((maxTimeDiff - timeDiff) / 60d));
                        return CompletableFuture.completedFuture(Files.readString(versionCachePath).trim());
                    } else {
                        CompatUtil.LOGGER.info(I18n.as("stackdeobf.refreshing"),
                                this.name, (long) Math.ceil(timeDiff / 60d));
                    }
                } catch (IOException exception) {
                    throw new RuntimeException(exception);
                }
            }

            URI metaUri = this.artifactInfo.buildMetaUri();
            CompatUtil.LOGGER.info(I18n.as("stackdeobf.fetching"), this.name);

            return HttpUtil.getAsync(metaUri, executor).thenApply(resp -> {
                try (InputStream input = new ByteArrayInputStream(resp)) {
                    Document document;
                    try {
                        // https://stackoverflow.com/a/14968272
                        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                        document = factory.newDocumentBuilder().parse(input);
                    } catch (ParserConfigurationException | SAXException exception) {
                        throw new IOException(exception);
                    }

                    NodeList versions = document.getElementsByTagName("version");
                    for (int i = versions.getLength() - 1; i >= 0; i--) {
                        String version = versions.item(i).getTextContent();
                        if (!version.startsWith(mcVersion + "+")) {
                            // 19w14b and before have this formatting
                            if (!version.startsWith(mcVersion + ".")) {
                                continue;
                            }

                            if (version.substring((mcVersion + ".").length()).indexOf('.') != -1) {
                                // mcVersion is something like "1.19" and version is something like "1.19.4+build.1"
                                // this prevents this being recognized as a valid mapping
                                continue;
                            }
                        }

                        Files.writeString(versionCachePath, version);
                        CompatUtil.LOGGER.info(I18n.as("stackdeobf.cached.lastest"), this.name, version);

                        return version;
                    }

                    throw new IllegalArgumentException(I18n.as("stackdeobf.cantfind") + " " + this.name + " " + I18n.as("stackdeobf.mappings.version") + " " + mcVersion);
                } catch (IOException exception) {
                    throw new RuntimeException(I18n.as("stackdeobf.cantparse") + " " + metaUri + " " + I18n.as("stackdeobf.for") + " " + mcVersion, exception);
                }
            });
        }, executor);
    }

    @Override
    protected CompletableFuture<Void> parseMappings0(Executor executor) {
        return CompletableFuture.supplyAsync(() -> {
            MemoryMappingTree mappings = new MemoryMappingTree();

            try (InputStream fileInput = Files.newInputStream(this.path);
                 GZIPInputStream gzipInput = new GZIPInputStream(fileInput);
                 Reader reader = new InputStreamReader(gzipInput)) {
                MappingReader.read(reader, MappingFormat.TINY_2, mappings);
            } catch (IOException exception) {
                throw new RuntimeException(exception);
            }

            this.mappings = mappings;
            return null;
        }, executor);
    }

    @Override
    protected CompletableFuture<Void> visitMappings0(MappingVisitor visitor, Executor executor) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                this.mappings.accept(visitor);
            } catch (IOException exception) {
                throw new RuntimeException(exception);
            }
            return null;
        }, executor);
    }
}
