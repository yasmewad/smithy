---
name: smithy-selector
description: Generates and validates Smithy selector expressions for querying shapes in a Smithy model. Use when finding or filtering shapes, querying a model, writing a selector, matching traits, traversing service/resource/operation relationships, filtering by namespace, or validating selector syntax with the Smithy CLI. Also use when asked how to find shapes, get the shape ID of something, or determine which shapes have a given trait.
tags: [skill, smithy, selector, query, model, trait, shape]
visibility: public
capability: validate
audience: all-smithy-authors
harness: any
license: Apache-2.0
---

# Smithy Selector

## Overview

Generate correct Smithy selector DSL expressions and optionally validate them using the Smithy CLI (`smithy select`).

## Usage

Use this skill when:
- User asks how to find/select/query shapes in a Smithy model
- User needs a selector for a validator (`EmitEachSelector`)
- User asks "how do I get the shape ID of X" in Smithy context
- User wants to traverse service/resource/operation relationships
- User needs to filter shapes by trait, namespace, type, or relationship
- User wants to validate a selector against a real model

## Core Concepts

A Smithy selector is a DSL that traverses a model as a labeled multidigraph. Each shape is a vertex; each shape ID and member is an edge. Selectors yield zero or more matching shapes.

### Shape Type Tokens

| Token | Matches |
|-------|---------|
| `*` | All shapes |
| `number` | byte, short, integer, long, float, double, bigDecimal, bigInteger |
| `simpleType` | All simple types |
| `string` | String shapes |
| `blob` | Blob shapes |
| `boolean` | Boolean shapes |
| `document` | Document shapes |
| `timestamp` | Timestamp shapes |
| `list` | List shapes (and set) |
| `map` | Map shapes |
| `structure` | Structure shapes |
| `union` | Union shapes |
| `service` | Service shapes |
| `operation` | Operation shapes |
| `resource` | Resource shapes |
| `member` | Member shapes |
| `integer`, `byte`, `short`, `long`, `float`, `double`, `bigDecimal`, `bigInteger` | Specific numeric types |
| `enum` | Enum shapes (Smithy 2.0) |
| `intEnum` | IntEnum shapes (Smithy 2.0) |

### Attribute Selectors

Test shape properties inside `[...]`:

- `[trait|traitName]` -- existence check (shape has trait)
- `[trait|traitName = value]` -- string equality
- `[trait|traitName != value]` -- not equal
- `[trait|traitName ^= prefix]` -- starts with
- `[trait|traitName $= suffix]` -- ends with
- `[trait|traitName *= substring]` -- contains
- `[trait|traitName ?= true]` -- existence as boolean
- Append `i` before `]` for case-insensitive: `[trait|httpQuery *= token i]`
- Numeric: `>`, `>=`, `<`, `<=`

#### Attribute Paths

- `[id]` -- shape ID; `[id = 'ns#Name']`
- `[id|namespace = 'smithy.example']` -- namespace
- `[id|name = MyShape]` -- shape name
- `[id|member = foo]` -- member name
- `[trait|range|min = 1]` -- nested trait property
- `[trait|(keys)]` -- projection of trait shape IDs
- `[trait|(values)]` -- projection of trait values
- `[trait|(length) > 10]` -- count of traits

#### Trait Shorthand

Traits in `smithy.api` namespace can omit the namespace:
```
[trait|deprecated]          ← same as [trait|smithy.api#deprecated]
[trait|error = client]      ← trait value comparison
```

Custom traits need full ID: `[trait|my.ns#myTrait]`

#### Service Attribute

Available on service shapes: `[service|version ^= '2018-']`

#### Projection `(first)` Property

Flattens and returns first value from a projection (use when projection has single value):
```
[trait|enum|(values)|(first)|value = 'a']
```

### Neighbor Traversal

| Syntax | Name | Description |
|--------|------|-------------|
| `>` | Forward undirected | All shapes referenced by current shape |
| `<` | Reverse undirected | All shapes that reference current shape |
| `~>` | Forward recursive | All shapes in closure (transitive) |
| `-[rel]->` | Forward directed | Follow named relationship |
| `<-[rel]-` | Reverse directed | Reverse named relationship |

#### Key Relationships

| From | Relationship | To |
|------|-------------|-----|
| service | `operation` | Bound operations |
| service | `resource` | Bound resources |
| service | `error` | Error structures |
| resource | `identifier`, `property`, `resource`, `operation`, `collectionOperation` | Various |
| resource | `create`, `read`, `update`, `delete`, `list`, `put` | Lifecycle operations |
| operation | `input`, `output`, `error` | Input/output/error structures |
| list, map, structure, union | `member` | Members |
| member | *(unnamed)* | Target shape |
| enum | `member` | Enum members |
| intEnum | `member` | IntEnum members |
| `*` | `trait` | Trait definition shapes (only with explicit `-[trait]->`) |
| `*` | `mixin` | Mixin shapes |

### Functions

| Function | Purpose | Example |
|----------|---------|---------|
| `:test(selector)` | Test if shape matches without changing current shape | `list:test(> member > string)` |
| `:not(selector)` | Exclude shapes matching selector | `:not(string)` |
| `:is(sel1, sel2)` | Yield shapes matching any selector | `:is(string, number)` |
| `:in(selector)` | Test if shape is in result set | `:in(${myVar})` |
| `:root(selector)` | Evaluate against all shapes globally | `:root(service ~> operation)` |
| `:topdown(qual, disqual?)` | Inherit match down containment hierarchy | `:topdown([trait|streaming])` |
| `:recursive(selector)` | Recursively apply selector | `:recursive(-[mixin]->)` |

### Variables

```
$varName(selector)    ← set variable
${varName}            ← get variable
@{var|varName|...}    ← access in scoped attribute
```

### Scoped Attribute Selectors

Compare multiple properties of the same attribute:
```
[@trait|range: @{min} > @{max}]
[@trait|httpApiKeyAuth: @{name} = header && @{in} != 'x-api-token' i]
```

### Projection Comparators

| Comparator | Meaning |
|-----------|---------|
| `{=}` | Left values all found in right |
| `{!=}` | Negation of `{=}` |
| `{<}` | Left is subset of right |
| `{<<}` | Left is proper subset of right |

## Common Selector Patterns

### By namespace
```
[id|namespace = 'my.namespace']
```

### All operations missing documentation
```
operation:not([trait|documentation])
```

### Protocol trait of a service
```
service -[trait]-> [trait|protocolDefinition]
```

### Shape ID of protocol trait for a given service
```
service [id = 'my.ns#MyService'] -[trait]-> [trait|protocolDefinition]
```

### All shapes in a service closure
```
service ~> *
```

### Operations without @http in a service
```
service[trait|aws.protocols#restJson1] ~> operation:not([trait|http])
```

### Members targeting strings
```
member > string
```

### Required members of a structure
```
structure > member [trait|required]
```

### Shapes with deprecated trait
```
[trait|deprecated]
```

### Auth traits not matching service auth
```
service
$authTraits(-[trait]-> [trait|authDefinition])
~> operation [trait|auth]
:not([@: @{trait|auth|(values)} {<} @{var|authTraits|id}])
```

### Shapes not referenced by anything (orphans)
```
:not([trait|trait]) :not(< *)
```

### Resource lifecycle operations
```
resource -[read]->
resource -[create, read, update, delete, list]->
```

## Validation with the Smithy CLI

You **MUST** validate generated selectors with the `smithy select` command when the user has a model available. This runs the selector against the loaded model and returns the matching shapes.

### Workflow

1. Understand the user's question about what shapes they want to find
2. Compose the selector expression using the patterns above
3. Present the selector with an explanation of how it works
4. **Run `smithy select`** with the selector and model path to validate and get results
5. Present the matching shape IDs to the user
6. Use results to guide next steps (e.g., "these 3 operations are missing @paginated")

### Example Command

```bash
smithy select --selector 'operation:not([trait|documentation])' /path/to/model/
```

Returns one matching shape ID per line, for example:
```
com.example#GetWidget
com.example#DeleteWidget
```

### Using Selectors in Validators (EmitEachSelector)

Selectors power custom validators in `smithy-build.json` or model metadata:
```smithy
metadata validators = [{
    name: "EmitEachSelector"
    id: "OperationMissingDocumentation"
    message: "This operation is missing documentation"
    configuration: {
        selector: "operation:not([trait|documentation])"
    }
}]
```

### Check if CLI is installed
```bash
smithy --help 2>/dev/null && echo "INSTALLED" || echo "NOT_INSTALLED"
```

### Install if needed
See `references/install-smithy-cli.md` for platform-specific instructions.

### `smithy select` Command

```
smithy select [OPTIONS] <MODELS>
```

#### Select-specific flags

| Flag | Description |
|------|-------------|
| `--selector SELECTOR` | Selector expression. Reads STDIN if omitted |
| `--show DATA` | Comma-separated: `type`, `file`, `vars`. Forces JSON array output |
| `--show-traits TRAITS` | Comma-separated trait IDs to include in output. Forces JSON output. Prelude traits can omit namespace (e.g. `length` = `smithy.api#length`) |

#### Shared build flags

| Flag | Short | Description |
|------|-------|-------------|
| `--allow-unknown-traits` | `--aut` | Ignore unknown traits. **Required for raw JSON AST files with custom traits** |
| `--config CONFIG_PATH` | `-c` | Path to smithy-build.json (repeatable, merges configs) |
| `--no-config` | | Disable auto-detection of smithy-build.json |
| `--output OUTPUT_PATH` | | Where to write build artifacts (default: `./build/smithy`) |

#### Global flags

| Flag | Description |
|------|-------------|
| `--debug` | Display debug info (sets logging to ALL) |
| `--quiet` | Silence output except errors |
| `--no-color` | Disable ANSI colors |
| `--force-color` | Force ANSI colors |
| `--logging LOG_LEVEL` | OFF, SEVERE, WARNING, INFO, FINE, ALL |
| `--stacktrace` | Display stacktrace on error |

`<MODELS>` accepts files, directories, or shell globs. No `smithy-build.json` required (use `--no-config` to be explicit).

### Running selectors on arbitrary JSON AST files

You can run selectors directly against Smithy JSON AST files (`.json`) or IDL files (`.smithy`) without a `smithy-build.json`:

```bash
# Against JSON AST files -- use --aut to skip unknown trait validation
smithy select --aut --selector 'operation -[input]-> ~> string' ./models/myservice/*.json

# Against .smithy IDL files
smithy select --selector 'operation:not([trait|documentation])' ./model/*.smithy

# Against a directory (loads all .smithy and .json files)
smithy select --aut --selector 'service ~> operation' ./models/

# Glob patterns work
smithy select --aut --selector 'service' ./models/*/service/*
```

### Enriched output with --show and --show-traits

Default output is one shape ID per line. Adding `--show` or `--show-traits` switches to JSON array output.

```bash
# Show shape type and source file location
smithy select --aut --show type,file --selector 'operation' ./models/*.json
# Output: [{"shape": "com.example#GetFoo", "type": "operation", "file": "/path/model.json:10:1"}, ...]

# Include specific trait values on matches
smithy select --aut --show-traits 'length,range,documentation' --selector ':is([trait|length], [trait|range])' ./models/*.json
# Output: [{"shape": "...", "traits": {"smithy.api#length": {"min": 1}, "smithy.api#documentation": "..."}}, ...]

# Captured variables from selector
smithy select --show vars --selector 'list $list(*) > member > string' model/
# Output: [{"shape": "smithy.api#String", "vars": {"list": ["smithy.example#MyList"]}}, ...]

# Combine all
smithy select --aut --show type,file,vars --show-traits required,documentation \
  --selector 'structure > member $m(*)' ./models/*.json
```

### Selector from STDIN

Omit `--selector` to pipe complex selectors:
```bash
echo 'service ~> operation:not([trait|documentation])' | smithy select --aut ./models/*.json
```

### Real-world example

```bash
# Find optional list members targeting strings within a given namespace
smithy select --aut \
  --selector 'operation -[input]-> ~> list[id|namespace = com.example.metrics] > member:not([trait|required]) :test(> string)' \
  ./models/metrics/service/*
```

## Common Mistakes

### Forgetting quotes around shape IDs with members
```
# Wrong:
[id = foo.baz#Structure$foo]
# Correct:
[id = 'foo.baz#Structure$foo']
```

### Using trait relationships without explicit directed syntax
```
# Wrong -- trait relationships are NOT yielded by > or ~>
service > [trait|protocolDefinition]
# Correct -- must use -[trait]->
service -[trait]-> [trait|protocolDefinition]
```

### Confusing :test with direct traversal
```
# This returns STRING shapes targeted by list members:
list > member > string
# This returns LIST shapes that target strings:
list:test(> member > string)
```

### Missing namespace on custom traits
```
# Wrong (only works for smithy.api traits):
[trait|myCustomTrait]
# Correct:
[trait|my.namespace#myCustomTrait]
```
