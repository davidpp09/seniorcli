# Frente 3 - CLI, Producto & Persistencia

**2 personas; una de ellas es la responsable tecnica del frente.**
**Entrega principal:** experiencia de usuario + sesiones recuperables.

Convierten los motores internos en algo claro, recuperable y usable. Aunque los
otros dos frentes funcionen, el producto falla si la interaccion confunde o la
sesion se pierde.

## Su codigo

```text
src/
  main.rs         arranque del binario `senior`
  app/
    mod.rs        AppState y el flujo de cada comando   [funcionando]
    cli.rs        subcomandos y flags (clap)            [funcionando]
    storage.rs    .senior/, escrituras atomicas         [funcionando]
    session.rs    turnos en JSONL                       [funcionando]
    ui.rs         presentacion en terminal              [funcionando]
    repl.rs       entrada de la sesion interactiva      [minimo: sin historial]
```

Son el unico frente que imprime en pantalla y el unico que escribe en disco.

## Que ya funciona

```bash
cargo test app::
cargo run -- init
cargo run              # sesion interactiva
cargo run -- debug
cargo run -- progress
```

- Los cuatro comandos parsean y corren.
- `senior` a secas abre una sesion interactiva multi-turno (`cmd_sesion` y
  `bucle` en `mod.rs`): analiza el proyecto una vez al entrar, la respuesta del
  alumno a la pregunta del tutor alimenta `record_evidence`, y tiene los
  comandos `/ayuda`, `/contexto`, `/progreso`, `/analizar` y `/salir`.
- `.senior/` con `config.toml`, `profile.json`, `profile.json.bak` y
  `sessions/*.jsonl`.
- Escrituras atomicas (temporal + rename), con el caso de Windows resuelto.
- El perfil se valida al cargar; si esta corrupto, se restaura el respaldo; se
  **rechaza** guardar un perfil invalido.
- Cada turno se escribe apenas ocurre. Una linea corrupta cuesta un turno, no la
  sesion.
- La salida es ASCII puro: nada critico depende de color ni de glifos.
- Los errores se traducen a mensajes accionables, no a stack traces.

## Que les toca construir

### 1. REPL de verdad (`repl.rs`)

Hoy es un `read_line` sobre stdin. Funciona, pero no se siente como una
herramienta seria: no hay historial ni edicion de linea.

- Descomenten `rustyline` en `Cargo.toml` (seccion Frente 3).
- Implementen `Prompt` con rustyline. **La frontera ya esta aislada**: el trait
  `Prompt` no cambia, asi que no tocan nada mas del frente.
- Guarden el historial en `.senior/history`.

**Hecho cuando:** flechas arriba/abajo recorren el historial y `ScriptedPrompt`
sigue funcionando para los tests.

### 2. Afinar la sesion interactiva

El ciclo multi-turno ya existe (`bucle` en `mod.rs`). Lo que falta es pulirlo:

- Hoy el contexto solo se vuelve a recolectar con `/analizar`. Estaria bien
  detectar que el alumno cambio archivos (por ejemplo, con `git status`) y
  sugerirle analizar de nuevo, **sin** recolectar en cada turno (es caro).
- `senior learn` y `senior debug` siguen resolviendo un solo turno. Decidir en
  equipo si deben entrar a la sesion interactiva despues de la primera
  respuesta.

**Hecho cuando:** hay un test con `ScriptedPrompt` que cubre el caso nuevo.

### 3. Manejo de Ctrl+C

`tokio` ya trae la feature `signal`. Al recibir la senal: terminar el turno en
curso si se puede, garantizar que lo escrito quedo en disco, y salir con un
mensaje claro, no con un panic.

**Hecho cuando:** Ctrl+C a mitad de una sesion y `senior progress` muestra los
turnos anteriores intactos.

### 4. Retomar la sesion anterior

`Storage::latest_session_path` y `SessionState::resume` ya existen, pero nadie
los usa fuera de `progress --sessions`. Falta que la sesion interactiva
(`senior` a secas), `senior learn` y `senior debug` ofrezcan continuar la
ultima sesion.

### 5. Estados de carga

Recolectar contexto puede tardar (un `mvn test-compile` no es instantaneo) y
ahora mismo el usuario solo ve "Analizando el proyecto...". Hace falta indicar
que esta pasando y cuanto lleva, sin depender de glifos animados.

### 6. Migracion de esquema

`PROFILE_SCHEMA_VERSION` y `ProjectConfig::schema_version` existen, pero no hay
migraciones. Cuando el Frente 2 agregue campos a `StudentProfile`, va a hacer
falta.

Regla: **nunca sobrescribir un perfil valido con uno parcial.**

### 7. Persistir las metricas

El Frente 2 produce `TurnMetrics` y hoy se descarta. Coordinen con ellos donde
guardarlo (probablemente junto al turno en el JSONL).

### 8. Mostrar el progreso mejor

`render_progress` ya da nivel, temas y barras. Falta tendencia: mejoro o empeoro
desde la semana pasada? Eso sale de `TopicMastery::last_seen`.

## Lo que NO necesitan todavia

Dashboard web, sincronizacion en la nube, TUI de pantalla completa.

## Riesgos que les tocan a ustedes

| Riesgo | Como lo evitamos |
|---|---|
| Estado desincronizado | `TutorDecision` es la fuente de verdad del turno. La CLI no re-decide el nivel de ayuda. |
| Perdida de sesion | Escritura incremental por turno, escrituras atomicas para archivos criticos. |
| Perfil corrupto | Validar al cargar, respaldo, y negarse a guardar algo invalido. |
| UX demasiado chat | Mostrar siempre evidencia, tipo de intervencion y estado de aprendizaje. |
| Errores opacos | `SeniorError::user_message()` y `ui::render_error`. Nada de 429 crudos. |
| Incompatibilidad de terminal | ASCII puro. Hay un test que lo verifica. |

## Prueba minima del frente

> Ejecutar `learn`/`debug`, mostrar la decision, cerrar y reabrir sin perder
> perfil ni sesion reciente.

La parte de persistencia ya esta cubierta en `app::tests` y en
`tests/vertical_slice.rs`.

## Como integrarse con los demas

No necesitan a nadie: `MockContextEngine` finge al Frente 1 y `MockTutorEngine`
al Frente 2. En `AppState` todo entra por traits, asi que cambiar un mock por la
implementacion real es una linea.
