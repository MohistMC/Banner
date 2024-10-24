package com.mohistmc.banner.stackdeobf.mappings.providers;
// Created by booky10 in StackDeobfuscator (16:57 23.03.23)

import com.google.common.base.Preconditions;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mohistmc.banner.BannerMCStart;
import com.mohistmc.banner.config.BannerConfigUtil;
import com.mohistmc.banner.stackdeobf.http.HttpUtil;
import com.mohistmc.banner.stackdeobf.util.CompatUtil;
import com.mohistmc.banner.util.I18n;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.mappingio.MappingReader;
import net.fabricmc.mappingio.MappingVisitor;
import net.fabricmc.mappingio.adapter.ForwardingMappingVisitor;
import net.fabricmc.mappingio.adapter.MappingSourceNsSwitch;
import net.fabricmc.mappingio.format.MappingFormat;
import net.fabricmc.mappingio.tree.MappingTree;
import net.fabricmc.mappingio.tree.MemoryMappingTree;
import net.minecraft.util.GsonHelper;
import org.apache.commons.lang3.StringUtils;

public class MojangMappingProvider extends AbstractMappingProvider {

    private static final String LICENSE =
            I18n.as("stackdeobf.mojang.licenseheader.1") + "\n"
                    + I18n.as("stackdeobf.mojang.licenseheader.2")+ "\n"
                    + I18n.as("stackdeobf.mojang.licenseheader.3")+ "\n"
                    + I18n.as("stackdeobf.mojang.licenseheader.4")+ "\n"
                    + I18n.as("stackdeobf.mojang.licenseheader.5")+ "\n"
                    + "\n";

    // the production/intermediary mappings need to be mapped back to their
    // obfuscated form, because mojang mappings are obfuscated -> named,
    // without the intermediary mappings inbetween
    private final IntermediaryMappingProvider intermediary = new IntermediaryMappingProvider();

    private Path path;
    private MemoryMappingTree mappings;

    public MojangMappingProvider() {
        super("mojang");
        Preconditions.checkState(CompatUtil.WORLD_VERSION >= 2203 || CompatUtil.WORLD_VERSION == 1976,
                "Mojang mappings are only provided by mojang starting from 19w36a (excluding 1.14.4)");

        BannerMCStart.LOGGER.warn(I18n.as("stackdeobf.mojang.license"));
        for (String line : StringUtils.split(LICENSE, '\n')) {
            BannerMCStart.LOGGER.warn(line);
        }
    }

    @Override
    protected CompletableFuture<Void> downloadMappings0(Path cacheDir, Executor executor) {
        CompletableFuture<Void> intermediaryFuture = this.intermediary.downloadMappings0(cacheDir, executor);

        this.path = cacheDir.resolve("mojang_" + CompatUtil.VERSION_ID + ".gz");
        if (Files.exists(this.path)) {
            return intermediaryFuture;
        }

        return intermediaryFuture.thenCompose($ -> this.fetchMojangMappingsUri(CompatUtil.VERSION_ID, executor)
                .thenCompose(uri -> HttpUtil.getAsync(uri, executor))
                .thenAccept(mappingBytes -> {
                    try (OutputStream fileOutput = Files.newOutputStream(this.path);
                         GZIPOutputStream gzipOutput = new GZIPOutputStream(fileOutput)) {
                        gzipOutput.write(mappingBytes);
                    } catch (IOException exception) {
                        throw new RuntimeException(exception);
                    }
                }));
    }

    private CompletableFuture<URI> fetchMojangMappingsUri(String mcVersion, Executor executor) {
        final String REPO_SITE = "https://piston-meta.mojang.com/mc/game/version_manifest_v2.json";
        URI manifestUri = URI.create(System.getProperty("stackdeobf.manifest-uri", REPO_SITE));
        return HttpUtil.getAsync(manifestUri, executor).thenCompose(manifestResp -> {
            JsonObject manifestObj;
            try (ByteArrayInputStream input = new ByteArrayInputStream(manifestResp);
                 Reader reader = new InputStreamReader(input)) {
                manifestObj = GsonHelper.parse(reader);
            } catch (IOException exception) {
                throw new RuntimeException(exception);
            }

            for (JsonElement element : manifestObj.getAsJsonArray("versions")) {
                JsonObject elementObj = element.getAsJsonObject();
                if (!mcVersion.equals(elementObj.get("id").getAsString())) {
                    continue;
                }

                URI infoUri = URI.create(elementObj.get("url").getAsString());
                return HttpUtil.getAsync(infoUri, executor).thenApply(infoResp -> {
                    JsonObject infoObj;
                    try (ByteArrayInputStream input = new ByteArrayInputStream(infoResp);
                         Reader reader = new InputStreamReader(input)) {
                        infoObj = GsonHelper.parse(reader);
                    } catch (IOException exception) {
                        throw new RuntimeException(exception);
                    }

                    EnvType env = FabricLoader.getInstance().getEnvironmentType();
                    String envName = env.name().toLowerCase(Locale.ROOT);

                    return URI.create(infoObj
                            .getAsJsonObject("downloads")
                            .getAsJsonObject(envName + "_mappings")
                            .get("url").getAsString());
                });
            }

            throw new IllegalStateException(I18n.as("stackdeobf.invalid.mcversion") + " " + mcVersion + " " + I18n.as("stackdeobf.mcversion.notfound"));
        });
    }

    @Override
    protected CompletableFuture<Void> parseMappings0(Executor executor) {
        return this.intermediary.parseMappings0(executor).thenRun(() -> {
            try {
                MemoryMappingTree rawMappings = new MemoryMappingTree();

                try (InputStream fileInput = Files.newInputStream(this.path);
                     GZIPInputStream gzipInput = new GZIPInputStream(fileInput);
                     Reader reader = new InputStreamReader(gzipInput)) {
                    MappingReader.read(reader, MappingFormat.PROGUARD_FILE, rawMappings);
                }

                rawMappings.setSrcNamespace("named");
                rawMappings.setDstNamespaces(List.of("official"));

                // mappings provided by mojang are named -> obfuscated
                // this needs to be switched for the remapping to work properly

                MemoryMappingTree switchedMappings = new MemoryMappingTree();
                rawMappings.accept(new MappingSourceNsSwitch(switchedMappings, "official"));
                this.mappings = switchedMappings;
            } catch (IOException exception) {
                throw new RuntimeException(exception);
            }
        });
    }

    @Override
    protected CompletableFuture<Void> visitMappings0(MappingVisitor visitor, Executor executor) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // the source names need to be mapped to intermediary, because
                // the specified visitor expects to receive intermediary source names
                this.mappings.accept(new Visitor(visitor));
            } catch (IOException exception) {
                throw new RuntimeException(exception);
            }
            return null;
        }, executor);
    }

    private final class Visitor extends ForwardingMappingVisitor {

        private MappingTree.ClassMapping clazz;

        private Visitor(MappingVisitor next) {
            super(next);
        }

        @Override
        public boolean visitClass(String srcName) throws IOException {
            this.clazz = MojangMappingProvider.this.intermediary.getMappings().getClass(srcName);
            if (this.clazz == null) {
                return false;
            }
            return super.visitClass(this.clazz.getDstName(0));
        }

        @Override
        public boolean visitMethod(String srcName, String srcDesc) throws IOException {
            MappingTree.MethodMapping mapping = this.clazz.getMethod(srcName, srcDesc);
            if (mapping == null) {
                return false;
            }
            return super.visitMethod(mapping.getDstName(0), mapping.getDstDesc(0));
        }

        @Override
        public boolean visitField(String srcName, String srcDesc) throws IOException {
            MappingTree.FieldMapping mapping = this.clazz.getField(srcName, srcDesc);
            if (mapping == null) {
                return false;
            }
            return super.visitField(mapping.getDstName(0), mapping.getDstDesc(0));
        }
    }
}
