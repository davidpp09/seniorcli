# Reglas del equipo SeniorCLI

Las reglas para agentes estan en [AGENTS.md](../../AGENTS.md), en la raiz del
repositorio. Leelo completo antes de proponer cualquier cambio.

Resumen minimo, por si solo se carga este archivo:

- Presenta un plan y espera aprobacion antes de editar.
- Solo toca la carpeta del frente de la tarea. No toques `src/contracts/` ni
  `tests/vertical_slice.rs` sin confirmacion.
- No ignores ni debilites tests. No ejecutes texto del LLM como shell. No
  expongas API keys.
- Antes de decir que terminaste: `cargo fmt --all`, `cargo clippy --all-targets
  --all-features -- -D warnings` y `cargo test`, todo en verde.
- Explica el diff en espanol simple: la persona tiene que poder defenderlo en el
  PR.
