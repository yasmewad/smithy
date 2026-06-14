---
name: smithy-constraint-traits
description: Applies Smithy constraint traits to validate data shapes. Use when adding validation rules like @required, @length, @range, or @pattern to members, constraining collections with @uniqueItems or @sparse, or managing defaults and optionality with @default, @addedDefault, and @clientOptional.
smithy-spec: "2.0.4"
visibility: public
capability: create
audience: all-smithy-authors
harness: any
license: Apache-2.0
---

# Smithy Constraint Traits

## Overview

Constraint traits define validation rules that shapes must satisfy. They enable API contracts to express requirements like "this string must be 1-100 characters" or "this number must be positive" directly in the model. Code generators use these traits to produce validation logic; documentation generators surface them as API requirements.

Apply constraints at the member level when the same shape has different rules in different contexts. Apply at the shape level for universal constraints.

## Core Constraints

### @required

Marks a structure member as mandatory -- the value MUST be present and non-null.

```smithy
structure CreateUserInput {
    @required
    username: String
    
    bio: String  // optional
}
```

Only valid on structure members. Map keys are inherently required; applying @required to them is invalid.

When a structure has `@input`, members marked `@required` become implicitly `@clientOptional` -- clients may omit them, but servers still validate presence. See smithy-idl-basics for `@input` details.

### @length

Constrains the size of strings, lists, maps, and blobs. Requires at least one of `min` or `max`.

| Target | Measures |
|--------|----------|
| string | Unicode scalar values (not bytes) |
| list | Number of members |
| map | Number of key-value pairs |
| blob | Bytes |

```smithy
@length(min: 1, max: 100)
string Username

@length(max: 10)
list Tags {
    member: String
}

structure Request {
    @length(min: 1)
    items: ItemList  // at least one item
}
```

Both bounds are inclusive. `@length(min: 1, max: 5)` accepts 1, 2, 3, 4, or 5. Ensure min <= max; an unsatisfiable constraint (min > max) will fail model validation.

### @range

Constrains numeric values. Requires at least one of `min` or `max`. Only valid on numeric types: byte, short, integer, long, float, double, bigInteger, bigDecimal.

```smithy
@range(min: 1, max: 100)
integer PageSize

@range(min: 0)
long Timestamp  // non-negative

structure Query {
    @range(max: 1000)
    limit: Integer
}
```

Both bounds are inclusive. Smithy has no exclusive bounds. For integers, `@range(min: 1)` means >= 1. To express "positive integer," use `@range(min: 1)` directly. Ensure min <= max.

### @pattern

Validates strings against an ECMA-262 regular expression. The pattern has NO implicit anchoring -- it matches if the regex matches any substring.

```smithy
// Partial match: "abc123def" passes (contains digits)
@pattern("\\d+")
string ContainsDigits

// Full match: use explicit anchors
@pattern("^[a-z][a-z0-9_]{2,31}$")
string Identifier
```

Backslashes require escaping in IDL: `\\d` for `\d`. Common patterns:

| Intent | Pattern |
|--------|---------|
| UUID | `^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$` |
| Email (simple) | `^[^@]+@[^@]+$` |
| Alphanumeric | `^[a-zA-Z0-9]+$` |

### @uniqueItems and @sparse

These traits control list behavior.

`@uniqueItems` requires all list members to be distinct by value equality. Cannot be applied to lists with float, double, or document members (equality is undefined). Conflicts with `@sparse` -- you cannot combine them.

```smithy
@uniqueItems
list TagSet {
    member: String
}
```

`@sparse` allows null values in lists and maps. Without it, collections are dense -- nulls are invalid.

```smithy
@sparse
list OptionalValues {
    member: String  // can contain nulls
}

@sparse
map NullableMetadata {
    key: String
    value: String  // values can be null; keys never null
}
```

### @default and @addedDefault

`@default` assigns a value when none is provided. Valid on simple types, lists, and maps -- not structures or unions.

```smithy
structure Pagination {
    @default(10)
    pageSize: Integer
    
    @default(true)
    includeDeleted: Boolean
    
    @default([])
    filters: FilterList  // empty list
}
```

Type restrictions:
- Lists: only `[]` (empty list)
- Maps: only `{}` (empty map)
- Root-level shapes cannot default to null
- Members CAN use `@default(null)` to override a root-level default or force optionality
- Strings, numbers, booleans: any valid value of that type

`@addedDefault` signals that `@default` was added after initial publication. Required for backward compatibility when evolving existing members.

```smithy
structure Query {
    @addedDefault
    @default(false)
    includeArchived: Boolean  // added in v2
}
```

Use `@default` for new members. Add `@addedDefault` when retrofitting defaults onto existing members.

### @clientOptional

Overrides `@required` for non-authoritative code generators (clients). The member remains required for servers but optional for clients.

```smithy
structure UpdateRequest {
    @required
    @clientOptional
    id: String  // server requires; client may omit
}
```

Primary use case: allowing `@required` to be removed later without breaking clients. If a member has `@clientOptional`, removing `@required` is backward-compatible.

## Trait Interactions

| Combination | Behavior |
|-------------|----------|
| @required + @default | @default ensures a value is always present, but @required forces serialization even when the value equals the default |
| @required + @clientOptional | Server requires, client treats as optional |
| @default + @addedDefault | Use @addedDefault only when adding @default to existing member |
| @sparse + @uniqueItems | CONFLICT -- cannot combine |
| @length + @pattern | Valid combination for validated strings |
| @required + @input | Implicitly @clientOptional (see smithy-idl-basics) |

When @default is present, @required still affects serialization -- the member must be serialized even when its value equals the default. Use @default alone when omission from the wire is acceptable; combine with @required when the value must always appear.

## Common Mistakes

| Mistake | Problem | Fix |
|---------|---------|-----|
| @range on string | @range only works on numeric types | Use @length for strings |
| @length on structure | @length only works on string, list, map, blob | Remove or use member-level constraints |
| @pattern without anchors | `@pattern("\\d+")` matches "abc123" | Use `^...$` for full-string match |
| @uniqueItems + @sparse | These traits conflict | Choose one based on requirements |
| @uniqueItems on float list | Equality undefined for floats | Use a different member type |
| @default on structure | Structures cannot have defaults | Use member-level defaults |
| Adding @default without @addedDefault | Breaks backward compatibility | Add @addedDefault for existing members |
| @required on map key | @required targets structure members only; cannot apply to map keys | Remove the trait |

Migrating from @enum trait: The `@enum` trait is deprecated in Smithy 2.0. Use the `enum` shape instead. Apply the `changeStringEnumsToEnumShapes` transform to migrate existing models.

## Verifying Your Constraints

After applying constraint traits, validate the model with the Smithy CLI:

```bash
smithy validate model/
```

To check that a change is backward-compatible against a prior model, use:

```bash
smithy diff --old old-model/ --new new-model/
```

## Quick Reference

### Trait Applicability

| Trait | Valid Targets |
|-------|---------------|
| @required | structure > member |
| @length | string, list, map, blob (or members targeting these) |
| @range | byte, short, integer, long, float, double, bigInteger, bigDecimal |
| @pattern | string (or members targeting string) |
| @uniqueItems | list (excluding float/double/document members) |
| @sparse | list, map |
| @default | simple types, list, map (or members targeting these) |
| @addedDefault | structure > member with @default |
| @clientOptional | structure > member |

### Common Patterns

```smithy
// Validated string
@length(min: 1, max: 255)
@pattern("^[a-zA-Z][a-zA-Z0-9_]*$")
string Identifier

// Bounded integer with default
@range(min: 1, max: 100)
integer PageSize

// Member-level default (preferred)
structure Pagination {
    @default(10)
    limit: Integer
}

// Backward-compatible new default
@addedDefault
@default(false)
boolean IncludeDeleted

// Unique tag set
@uniqueItems
@length(max: 50)
list Tags {
    member: String
}

// Nullable values
@sparse
map OptionalAttributes {
    key: String
    value: String
}
```
