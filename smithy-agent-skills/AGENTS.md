# AGENTS.md

Guidance for AI coding agents working in this directory.

## What lives here

This package vends a set of cross-harness Agent Skills (agentskills.io open
standard) for authoring and validating [Smithy](https://smithy.io) models.
They work in any agentskills.io-compatible coding agent or harness, and are
auto-discovered from `skills/`.

## Skills (create + validate)

- **smithy-idl-basics** — write valid Smithy IDL: shapes, members, operations,
  services, structures, enums, and `intEnum`; fix syntax and parse errors.
- **smithy-constraint-traits** — apply constraint traits (`@required`,
  `@length`, `@range`, `@pattern`, `@uniqueItems`, `@default`, etc.) to
  validate data shapes and manage member optionality.
- **smithy-selector** — write and check Smithy selector expressions to query
  shapes, match traits, and traverse relationships in a model.

## Validation runs via the Smithy CLI

These skills do not bundle a validation engine. Use the public Smithy CLI:

- `smithy validate` — validate a model and report violations.
- `smithy diff` — compare two model versions for backward-compatibility.
- `smithy select` — evaluate a selector against a model.
- `smithy ast` — emit the model AST for inspection.
- `smithy build` — build the model and run configured projections/plugins.

See https://smithy.io for CLI installation and reference documentation.
