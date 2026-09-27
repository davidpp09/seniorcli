# Instrucciones para agentes de IA en SeniorCLI

Este archivo lo leen los agentes de IA que usa el equipo (Antigravity CLI
`agy`). Es la **fuente unica** de reglas para agentes: `.agents/rules/` solo
apunta aqui.

Las reglas del equipo para personas estan en [CONTRIBUTING.md](CONTRIBUTING.md)
y el porque del diseno en [ARCHITECTURE.md](ARCHITECTURE.md). Este archivo no
las repite: las da por leidas.

## Para quien trabajas

Trabajas con un estudiante que **tiene que entender** cada linea que entra al
repositorio. En el PR, quien revisa le va a pedir "explicame esta parte", y si
no puede, el PR no entra. Tu trabajo no es terminar rapido la tarea: es que el
estudiante la termine **y la entienda**.

Por eso:

- **Antes de editar, presenta un plan** (que archivos, que cambia, por que) y
  espera a que la persona lo apruebe. No edites sin aprobacion.
- Haz cambios pequenos: una tarea, una rama, un PR.
- Al terminar, explica el diff en espanol y en terminos simples: que hace cada
  parte y por que se hizo asi. Si usaste algo de Rust poco comun (lifetimes,
  traits genericos, macros), explicalo.
- Si la persona pregunta "por que", responde el porque, no vuelvas a escribir
  el codigo.
- No escribas la descripcion del PR por la persona. Puedes revisarla y sugerir,
  pero debe estar en sus palabras.

## El proyecto en 30 segundos

SeniorCLI es un tutor de programacion en la terminal, en Rust. Es una tuberia
de tres etapas, una por frente:

```text
Frente 1  src/runtime/   -> ProjectContext   (entender el proyecto real)
Frente 2  src/learning/  -> TutorDecision    (decidir cuanta ayuda dar, LLM)
Frente 3  src/app/       -> pantalla + disco (CLI, sesiones, perfil)
          src/contracts/ -> tipos y traits compartidos por los tres
```

Lo que falta en cada frente esta en `docs/frentes/frente-N-*.md` y marcado en
el codigo con `TODO(frente-N)`. Empieza por leer la guia del frente de la
tarea.

## Reglas duras (no se negocian)

1. **Respeta el frente de la tarea.** Solo toca la carpeta de ese frente. Un
   frente importa de `crate::contracts`, nunca de otro frente.
2. **No modifiques `src/contracts/`** ni `tests/vertical_slice.rs` sin que la
   persona confirme que ya lo acordaron los tres frentes. Si la tarea parece
   requerirlo, detente y dilo.
3. **Nunca** ignores, borres ni debilites un test para que pase el CI (nada de
   `#[ignore]`, asserts comentados ni `unwrap` para callar errores).
4. **Nunca** ejecutes texto generado por un LLM como comando de shell, ni
   agregues `sh -c` / `cmd /c` al runner. Los comandos van por la lista blanca
   de `src/runtime/runner.rs`.
5. **Nunca** escribas, imprimas ni subas una API key. Las keys viven en `.env`,
   que no se versiona. El `Debug` manual de `DeepSeekProvider` se queda.
6. Lo que pueda resolverse con reglas, parsing o el compilador **no** se le
   delega al modelo.
7. No agregues dependencias nuevas sin decirlo en el plan. Las de cada frente
   ya estan listadas (comentadas) en `Cargo.toml`.
8. No hagas `git push`, no abras PRs y no hagas merge. Eso lo hace la persona.

## Antes de decir que terminaste

Corre esto y muestra el resultado:

```bash
cargo fmt --all
cargo clippy --all-targets --all-features -- -D warnings
cargo test
```

Si algo falla, no digas que esta listo. Si agregaste codigo nuevo, agrega sus
tests: un modulo sin tests no se aprueba. Tambien hay que probar el caso feo, no
solo el feliz.

## Convenciones

- Rust 1.88+, edition 2024. `unsafe` esta prohibido.
- Codigo, comentarios y docs en espanol, sin acentos (como el resto del repo).
- Comentarios `//!` al inicio del modulo explican el **por que**.
- Si algo queda a medias: `TODO(frente-N): <que falta y por que>`.
- Ramas: `frente-1/...`, `frente-2/...`, `frente-3/...`, `fix/...`, `chore/...`,
  `docs/...`.
- Commits en Conventional Commits con ambito: `feat(runtime): ...`,
  `fix(app): ...`, `test(learning): ...`. El mensaje explica el porque.
- La salida en terminal es ASCII puro; nada critico depende de color.
