# ADR 0001: Kotoba is the GAPKI catalog source authority

- Status: Accepted
- Date: 2026-07-21

## Context

The former CLJC source exposed an unbounded host map and sequence API. That
made catalog shape, traversal, and runtime authority depend on Clojure-family
host semantics even though this repository is a read-only fact source.

## Decision

`src/association_facts.kotoba` is the sole production source. The two admitted
GAPKI citations retain every prior field: id, title, association, ISIC,
country, kind, URL, URL provenance, established date, retrieved date, and the
complete topic set. A fixed field vocabulary and bounded count/index ABI
replace host map and sequence traversal.

Unknown associations, fields, topics, negative indexes, and out-of-range
indexes return zero or a typed option-none. The source declares no effects.
DataScript EDN remains a derived provider artifact, not an executable language
authority.

CI executes the reference semantics, restricted JavaScript, and instantiated
typed WebAssembly. It also rejects production `.clj`, `.cljc`, and `.cljs`
sources.

## Consequences

- All existing citation observations remain reconstructible through a bounded
  typed ABI.
- Coverage cannot imply a rule for an unknown association or topic.
- Clojure and the JVM are compiler/test hosts only.
