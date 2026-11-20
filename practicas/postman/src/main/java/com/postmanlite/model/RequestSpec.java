package com.postmanlite.model;

import java.util.LinkedHashMap;
import java.util.Map;

/** Lo que el usuario quiere enviar: metodo, URL, headers y body. */
public record RequestSpec(String method, String url, Map<String, String> headers, String body) {

    public RequestSpec {
        headers = headers == null ? new LinkedHashMap<>() : new LinkedHashMap<>(headers);
        body = body == null ? "" : body;
    }

    public boolean allowsBody() {
        return !(method.equals("GET") || method.equals("HEAD"));
    }
}
