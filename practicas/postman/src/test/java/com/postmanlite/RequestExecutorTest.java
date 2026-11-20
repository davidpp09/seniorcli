package com.postmanlite;

import com.postmanlite.model.RequestSpec;
import com.postmanlite.model.ResponseResult;
import com.postmanlite.service.RequestExecutor;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class RequestExecutorTest {

    private static HttpServer server;
    private static String base;
    private final RequestExecutor executor = new RequestExecutor();

    @BeforeAll
    static void start() throws Exception {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        // /echo devuelve metodo, header X-Test y body recibidos.
        server.createContext("/echo", ex -> {
            String body = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            String out = ex.getRequestMethod() + "|" + ex.getRequestHeaders().getFirst("X-Test") + "|" + body;
            byte[] bytes = out.getBytes(StandardCharsets.UTF_8);
            ex.getResponseHeaders().add("X-Reply", "ok");
            ex.sendResponseHeaders(201, bytes.length);
            ex.getResponseBody().write(bytes);
            ex.close();
        });
        server.createContext("/missing", ex -> { ex.sendResponseHeaders(404, -1); ex.close(); });
        server.start();
        base = "http://127.0.0.1:" + server.getAddress().getPort();
    }

    @AfterAll
    static void stop() { server.stop(0); }

    @Test
    void sendsMethodHeadersAndBody() {
        ResponseResult r = executor.execute(new RequestSpec("POST", base + "/echo", Map.of("X-Test", "abc"), "{\"x\":1}"));
        assertFalse(r.isError());
        assertEquals(201, r.status());
        assertEquals("POST|abc|{\"x\":1}", r.body());
        assertEquals("ok", r.headers().get("x-reply").get(0));
        assertEquals(r.body().length(), r.sizeBytes());
    }

    @Test
    void reportsErrorStatusesWithoutFailing() {
        ResponseResult r = executor.execute(new RequestSpec("GET", base + "/missing", null, null));
        assertFalse(r.isError());
        assertEquals(404, r.status());
    }

    @Test
    void ignoresBodyOnGet() {
        ResponseResult r = executor.execute(new RequestSpec("GET", base + "/echo", null, "ignorado"));
        assertEquals("GET|null|", r.body());
    }

    @Test
    void rejectsInvalidUrls() {
        assertTrue(executor.execute(new RequestSpec("GET", "ftp://x", null, null)).isError());
        assertTrue(executor.execute(new RequestSpec("GET", "no es una url", null, null)).isError());
    }

    @Test
    void reportsConnectionErrors() {
        assertTrue(executor.execute(new RequestSpec("GET", "http://127.0.0.1:1/", null, null)).isError());
    }
}
