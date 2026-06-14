---
name: smithy-idl-basics
description: Writes valid Smithy IDL files with correct syntax, types, and structure. Use when authoring a new Smithy model, defining a shape, creating a structure/union/list/map, choosing between enum and intEnum, writing operations with inline input/output, or troubleshooting an IDL parse or validation error.
smithy-spec: "2.0.4"
visibility: public
capability: create
audience: all-smithy-authors
harness: any
license: Apache-2.0
---

# Smithy IDL Basics

## Overview

Smithy IDL is the human-readable format for defining API models. This skill covers file structure, type selection, shape definitions, and the semantic model that tools operate on. Master these fundamentals before using constraint traits or selectors.

## Core Concepts

### IDL File Structure

Every Smithy file has three ordered sections. All are optional, but order is enforced.

```smithy
// 1. CONTROL SECTION -- must come first
$version: "2"

// 2. METADATA SECTION -- after control, before shapes
metadata foo = "bar"

// 3. SHAPE SECTION -- namespace, use, then shapes
namespace com.example

use smithy.api#String

structure MyStruct {
    name: String
}
```

| Statement | Section | Required | Notes |
|-----------|---------|----------|-------|
| `$version: "2"` | Control | Strongly recommended | Omitting works but tools may assume older version |
| `metadata key = value` | Metadata | No | Model-level config |
| `namespace x.y` | Shape | Yes | Exactly one per file |
| `use ns#Shape` | Shape | No | Import before shapes |

### Simple Types

14 simple types for modeling scalar values (enum and intEnum are covered separately below):

| Type | Use When |
|------|----------|
| `blob` | Binary data (files, images) |
| `boolean` | True/false flags |
| `string` | Text, identifiers, enums |
| `byte` | -128 to 127 |
| `short` | -32,768 to 32,767 |
| `integer` | -2³¹ to 2³¹-1 (default for counts) |
| `long` | -2⁶³ to 2⁶³-1 (timestamps, large IDs) |
| `float` | 32-bit IEEE 754 |
| `double` | 64-bit IEEE 754 (default for decimals) |
| `bigInteger` | Arbitrary precision integer |
| `bigDecimal` | Arbitrary precision decimal |
| `timestamp` | Date/time values |
| `document` | Protocol-specific free-form data |

```smithy
string MyString
integer Count
timestamp CreatedAt
```

### Aggregate Types

| Type | Members | Key Constraint |
|------|---------|----------------|
| `list` | `member` | Dense by default; `@sparse` allows nulls |
| `map` | `key`, `value` | Key must target string; keys never null |
| `structure` | Named members | All optional by default |
| `union` | Named members | Exactly one member set at runtime |

```smithy
list StringList {
    member: String
}

map Metadata {
    key: String
    value: String
}

structure Address {
    street: String
    city: String
}

union Result {
    success: Output
    error: Error
}
```

### Enum and IntEnum

Both are OPEN types -- clients must handle unknown values gracefully.

| Aspect | `enum` | `intEnum` |
|--------|--------|-----------|
| Backing type | String | Integer |
| Implicit values | Yes (member name) | No |
| Explicit values | Optional | Required |

```smithy
// enum: string-based, implicit values OK
enum Status {
    ACTIVE          // value = "ACTIVE"
    INACTIVE = "OFF" // explicit value
}

// intEnum: integer-based, explicit values REQUIRED
intEnum Priority {
    LOW = 1
    MEDIUM = 2
    HIGH = 3
}
```

Member naming convention: `^[A-Z]+[A-Z_0-9]*$`

### Namespaces and Shape IDs

```smithy
namespace com.example

use smithy.api#String      // import from prelude
use other.ns#CustomTrait   // import custom shape
```

Relative shape ID resolution order:
1. Explicit `use` imports
2. Current file's namespace
3. Prelude (`smithy.api`)

### Semantic Model

The IDL is one serialization format. Tools operate on the semantic model -- an in-memory graph of shapes and traits.

Shape ID format:
```
namespace#ShapeName           // shape
namespace#ShapeName$member    // member
```

```smithy
// Targeting a member in selectors requires quotes
// [id = 'com.example#MyStruct$name']
```

Key rules:
- Shape IDs are case-sensitive
- Two shapes cannot differ only by case (`Foo` vs `foo` = conflict)
- Model merging: shapes with same ID must have identical type and members
- Trait resolution: list/set traits concatenate; conflicting scalar values error

### @input/@output Traits

Mark structures as operation input/output. Enables inline syntax and affects member optionality.

Constraints:
- Applying `@input` restricts the structure to only being referenceable as an operation's input
- Applying `@output` restricts the structure to only being referenceable as an operation's output
- Each marked structure can only be used by a single operation
- Conflicts with `@error`

Key effect: `@input` makes `@required` members implicitly `@clientOptional`.

Inline syntax (preferred):
```smithy
operation GetUser {
    input := {
        @required
        userId: String
    }
    output := {
        user: User
    }
}
```

Equivalent explicit form:
```smithy
@input
structure GetUserInput {
    @required
    userId: String
}

@output
structure GetUserOutput {
    user: User
}

operation GetUser {
    input: GetUserInput
    output: GetUserOutput
}
```

## Common Mistakes

| Mistake | Wrong | Correct |
|---------|-------|---------|
| Missing $version | *(no control section)* | `$version: "2"` |
| intEnum without values | `intEnum E { A }` | `intEnum E { A = 1 }` |
| Wrong section order | namespace before $version | $version → metadata → namespace |
| Member targeting unquoted | `[id = ns#S$m]` | `[id = 'ns#S$m']` |
| Case collision | `Foo` and `foo` in same namespace | Use distinct names |
| @input on reused structure | Same input for multiple ops | One structure per operation |
| Missing namespace | Shapes without namespace | `namespace x.y` before shapes |
| Ignoring OPEN semantics | Switch on enum without default | Handle unknown values |

## Verifying Your Model

After writing IDL, validate it with the Smithy CLI:

```bash
smithy validate model/
```

To inspect the resolved semantic model as JSON AST:

```bash
smithy ast model/
```

## Quick Reference

### Minimal Valid File

```smithy
$version: "2"
namespace com.example

structure Empty {}
```

### Simple Types

`blob` `boolean` `string` `byte` `short` `integer` `long` `float` `double` `bigInteger` `bigDecimal` `timestamp` `document`

### enum vs intEnum

```smithy
enum Color { RED GREEN BLUE }           // string values
intEnum Code { OK = 0  ERR = 1 }        // integer values required
```

### Shape ID Format

```
com.example#MyStruct           // absolute shape ID
com.example#MyStruct$field     // member ID
MyStruct                       // relative (resolved via use/namespace/prelude)
```

### Inline Operation

```smithy
operation Create {
    input := { @required name: String }
    output := { id: String }
    errors: [ValidationError]
}
```
