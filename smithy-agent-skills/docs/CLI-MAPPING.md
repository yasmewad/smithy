# From tool calls to the Smithy CLI

The skills in this module describe *capabilities* — validate a model, check a
diff for backward compatibility, select shapes, inspect the AST, build. None of
those capabilities require a custom server or proprietary tooling. Each one maps
directly to a command in the open-source [Smithy CLI](https://smithy.io/2.0/guides/smithy-cli/index.html),
which is the **public contract** for executing them.

This means a public user can follow these skills with nothing more than a stock
Smithy CLI installation. There is no hidden dependency on a backend service.

## Capability-to-command map

| Capability                        | Public Smithy CLI command | Notes |
|-----------------------------------|---------------------------|-------|
| Validate a model                  | `smithy validate`         | Runs the model assembler and all registered validators; non-zero exit on `ERROR`-severity events. |
| Diff / backward-compatibility     | `smithy diff`             | Compares an "old" model against a "new" model and reports evolution events. See classification below. |
| Select shapes                     | `smithy select`           | Evaluates a selector expression against the model and prints matching shape IDs. |
| Inspect the AST                   | `smithy ast`              | Emits the model as a normalized JSON AST for programmatic inspection. |
| Build                             | `smithy build`            | Assembles the model, runs validation, applies transforms, and runs build plugins per `smithy-build.json`. |

### `smithy validate`

The canonical "is this model well-formed and conformant?" check. It assembles
the model, applies every validator on the classpath (built-in plus any project
or custom validators), and reports validation events by severity. Treat a
non-zero exit as a hard gate before publishing or diffing.

### `smithy diff` and change classification

`smithy diff` takes a baseline ("old") model and a candidate ("new") model and
emits the set of differences as validation-style events. Conceptually those
differences classify into the three semantic-versioning buckets:

- **MAJOR** — breaking changes. Removing a shape, member, or operation; changing
  a member's target incompatibly; tightening a constraint a client already
  relies on. These break existing consumers and require a major version bump.
- **MINOR** — backward-compatible additions. Adding a new optional member, a new
  operation, or a new shape. Existing consumers keep working; new capability is
  available.
- **PATCH** — changes with no contract impact. Documentation-only edits and
  similar cosmetic changes.

The exact event a given change produces (and its severity) is determined by
Smithy's diff evaluators; the buckets above are the conceptual lens for deciding
how the change should affect the version number. Run `smithy diff` with both
model revisions to get the authoritative list, then map each reported event to
MAJOR / MINOR / PATCH.

### `smithy select`

Evaluates a Smithy *selector* — a query expression over the shape graph — and
returns the matching shape IDs. Use it to answer questions like "which
operations are missing pagination?" or "which structures carry a given trait?"
without hand-walking the model.

### `smithy ast`

Produces the model as a normalized JSON AST. This is the right tool when an
agent or script needs to read model structure programmatically rather than
parse IDL text.

### `smithy build`

The end-to-end assemble-validate-transform-plugin pipeline driven by
`smithy-build.json`. It supersedes a bare `smithy validate` when the project
defines projections, transforms, or build plugins.

## Public contract vs. richer wrappers

A downstream agent integration *may* wrap these commands — for example, exposing
them through a tool/MCP layer that adds structured output, caching, or
summarization for nicer agent ergonomics. That is an implementation detail of a
particular harness. The **public contract these skills depend on is the Smithy
CLI itself**: every instruction in this module is expressed in terms of the CLI
commands above, so the skills work identically whether or not such a wrapper is
present.

## Installing the CLI

Follow the official Smithy CLI installation guide:
<https://smithy.io/2.0/guides/smithy-cli/cli_installation.html>

Once installed, verify with:

```bash
smithy --version
smithy validate model/
```

