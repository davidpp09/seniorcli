# PostmanLite

Cliente HTTP de escritorio estilo Postman, hecho con Java 17+ y Maven (Swing, sin dependencias externas).

Incluye: metodos GET/POST/PUT/PATCH/DELETE/HEAD/OPTIONS, tabla de headers, body, respuesta con
status, tiempo y tamano, JSON formateado, headers de respuesta e historial de requests.

## Uso

Solo necesitas Java 17+ (JDK). Maven se descarga solo gracias al wrapper (`mvnw`).

```bash
./mvnw test                  # (en Windows: .\mvnw.cmd test)
./mvnw package               # genera target/postman-lite-1.0.0.jar
java -jar target/postman-lite-1.0.0.jar
```

## Estructura

- `model/`   `RequestSpec` y `ResponseResult` (datos puros)
- `service/` `RequestExecutor` (java.net.http) y `JsonPretty`
- `ui/`      `MainFrame` (Swing; el request corre en un `SwingWorker`)

## Ideas para seguir practicando

Variables de entorno (`{{base_url}}`), colecciones guardadas en disco (JSON), autenticacion
(Bearer/Basic), pestana de query params, resaltado de sintaxis, cancelar requests en curso.
