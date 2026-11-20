package com.postmanlite.service;

import com.postmanlite.model.RequestSpec;
import com.postmanlite.model.ResponseResult;

import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

/** Ejecuta un {@link RequestSpec} con java.net.http y devuelve un {@link ResponseResult}. */
public class RequestExecutor {

    private final HttpClient client = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL)
            .version(HttpClient.Version.HTTP_1_1)
            .proxy(ProxySelector.getDefault())   // respeta el proxy del sistema (VPN/empresa/colegio)
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    public ResponseResult execute(RequestSpec spec) {
        long start = System.nanoTime();
        try {
            URI uri = URI.create(spec.url().trim());
            if (uri.getScheme() == null || !uri.getScheme().matches("(?i)https?")) {
                return ResponseResult.failure("La URL debe empezar con http:// o https://", 0);
            }

            HttpRequest.BodyPublisher publisher = spec.allowsBody() && !spec.body().isEmpty()
                    ? HttpRequest.BodyPublishers.ofString(spec.body())
                    : HttpRequest.BodyPublishers.noBody();

            HttpRequest.Builder builder = HttpRequest.newBuilder(uri)
                    .timeout(Duration.ofSeconds(30))
                    .method(spec.method(), publisher);
            spec.headers().forEach((k, v) -> {
                // Estos headers los controla el cliente HTTP y rechaza fijarlos a mano.
                if (!isRestricted(k)) {
                    builder.header(k, v);
                }
            });

            HttpResponse<byte[]> res = client.send(builder.build(), HttpResponse.BodyHandlers.ofByteArray());
            long elapsed = (System.nanoTime() - start) / 1_000_000;
            byte[] raw = res.body();
            return new ResponseResult(
                    res.statusCode(),
                    res.headers().map(),
                    new String(raw, StandardCharsets.UTF_8),
                    elapsed,
                    raw.length,
                    null);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return ResponseResult.failure("Request cancelado", elapsedSince(start));
        } catch (Exception e) {
            return ResponseResult.failure(describe(e), elapsedSince(start));
        }
    }

    /** Mensaje util: Java a veces trae message=null y la causa real esta anidada. */
    static String describe(Throwable e) {
        StringBuilder sb = new StringBuilder(e.getClass().getSimpleName());
        for (Throwable t = e; t != null; t = t.getCause() == t ? null : t.getCause()) {
            if (t.getMessage() != null && !sb.toString().contains(t.getMessage())) {
                sb.append(": ").append(t.getMessage());
            } else if (t != e && !sb.toString().contains(t.getClass().getSimpleName())) {
                sb.append(" <- ").append(t.getClass().getSimpleName());
            }
        }
        return sb.toString();
    }

    private static long elapsedSince(long startNanos) {
        return (System.nanoTime() - startNanos) / 1_000_000;
    }

    private static boolean isRestricted(String name) {
        return switch (name.toLowerCase()) {
            case "host", "content-length", "connection", "expect", "upgrade" -> true;
            default -> false;
        };
    }
}
