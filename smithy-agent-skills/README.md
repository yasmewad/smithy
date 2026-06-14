# smithy-agent-skills

Curated [Agent Skills](https://smithy.io/) for authoring and validating
[Smithy](https://smithy.io/) models. The skills package Smithy modeling
expertise — how to write valid IDL, apply constraint traits, run validation, and
check backward compatibility — as portable instructions an AI coding agent can
load on demand.

The skills follow the **Agent Skills open standard**: each skill is a directory
with a `SKILL.md` (YAML frontmatter plus markdown body) and optional bundled
`references/` and `scripts/`. Because the standard is harness-neutral, the same
skill files work across **Claude Code, Cursor, Gemini CLI, Copilot, Codex, and
other agentskills.io-compatible harnesses.**

## Install as a Claude Code plugin

```text
/plugin marketplace add smithy-lang/smithy
/plugin install smithy-authoring@smithy
```

The first command registers this repository as a plugin marketplace; the second
installs the Smithy authoring skill bundle.

## Use in other harnesses

Other harnesses consume the **same `SKILL.md` files** directly — there is no
Claude-specific packaging in the skill content. Point your harness at the skill
directories in this module (or vendor them per your harness's convention) and
the agent will discover them via the standard `name` + `description` frontmatter.
The skills do not require any custom server: all executable steps map to the
public Smithy CLI (see [docs/CLI-MAPPING.md](./docs/CLI-MAPPING.md)).

## What ships today

The currently shipped capabilities are:

- **Create** — writing valid Smithy IDL, defining shapes/operations/services,
  and applying constraint traits.
- **Validate** — running `smithy validate`, checking backward compatibility with
  `smithy diff`, and querying the model with `smithy select`.

Each skill self-declares a `capability` facet (`create`, `validate`, `both`,
`review`, or `discover`) so a harness can load only what it needs.

## Documentation

- [docs/CLI-MAPPING.md](./docs/CLI-MAPPING.md) — how each capability maps to a
  public Smithy CLI command, so no custom server is required.

## License

Apache-2.0.
