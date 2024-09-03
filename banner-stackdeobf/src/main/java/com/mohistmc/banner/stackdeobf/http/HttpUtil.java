package com.mohistmc.banner.stackdeobf.http;

import com.mohistmc.banner.BannerMCStart;
import com.mohistmc.banner.util.I18n;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import org.apache.commons.io.FileUtils;

public final class HttpUtil {

    private static final Map<Executor, HttpClient> HTTP = new WeakHashMap<>();

    private HttpUtil() {
    }

    private static HttpClient getHttpClient(Executor executor) {
        synchronized (HTTP) {
            return HTTP.computeIfAbsent(executor, $ -> HttpClient.newBuilder().executor(executor).build());
        }
    }

    public static byte[] getSync(URI uri) {
        return getSync(uri, ForkJoinPool.commonPool());
    }

    public static byte[] getSync(URI uri, Executor executor) {
        return getAsync(uri, executor).join();
    }

    public static CompletableFuture<byte[]> getAsync(URI uri) {
        return getAsync(uri, ForkJoinPool.commonPool());
    }

    public static CompletableFuture<byte[]> getAsync(URI uri, Executor executor) {
        HttpRequest request = HttpRequest.newBuilder(uri).build();
        HttpResponse.BodyHandler<byte[]> handler = HttpResponse.BodyHandlers.ofByteArray();

        BannerMCStart.LOGGER.info(I18n.as("stackdeobf.requesting"), uri);
        long start = System.currentTimeMillis();

        return getHttpClient(executor).sendAsync(request, handler).thenApplyAsync(resp -> {
            long timeDiff = System.currentTimeMillis() - start;
            byte[] bodyBytes = resp.body();

            String message = I18n.as("stackdeobf.received");
            Object[] args = {bodyBytes.length, FileUtils.byteCountToDisplaySize(bodyBytes.length),
                    resp.statusCode(), uri, timeDiff};

            if (!isSuccess(resp.statusCode())) {
                BannerMCStart.LOGGER.error(message, args);
                throw new FailedHttpRequestException(resp);
            }

            BannerMCStart.LOGGER.info(message, args);
            return bodyBytes;
        }, executor);
    }

    private static boolean isSuccess(int code) {
        return code >= 200 && code <= 299;
    }
}
