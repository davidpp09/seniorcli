package com.postmanlite.model;

import java.util.List;
import java.util.Map;

/** Resultado de ejecutar un request. Si {@code error} no es null, no hubo respuesta HTTP. */
public record ResponseResult(
        int status,
        Map<String, List<String>> headers,
        String body,
        long elapsedMillis,
        long sizeBytes,
        String error) {

    public static ResponseResult failure(String error, long elapsedMillis) {
        return new ResponseResult(0, Map.of(), "", elapsedMillis, 0, error);
    }

    public boolean isError() {
        return error != null;
    }
}
