# Smithy Model Transformers for Federated Model Build Workflow

## Executive Summary

**Bottom Line Up Front:** No custom transformers are required for the Model Merge Task. Smithy's existing transformer infrastructure and Model Assembler API provide all necessary capabilities for merging trait artifacts from multiple sources (AI-generated documentation, user overrides, waiters, examples) with priority-based resolution.

**Key Finding:** The Smithy `Model.assembler()` API natively supports priority-based merging through import ordering, and the `ModelTransformer` class provides built-in methods for trait removal and model manipulation.

---

## Background

The Trebuchet/Fusion integration requires a Model Merge Task that combines trait artifacts from multiple partner teams (Fusion Agents for documentation, waiters teams, code examples teams) into a final external Smithy model. This document analyzes Smithy's transformer architecture to determine implementation requirements.

### Requirements from Federated Model Build Workflow

1. **Priority-based merging**: Staged changes → AI suggestions → User overrides
2. **Trait operations**: Support ADD, REMOVE, UPDATE operations
3. **Multiple trait types**: Documentation, examples, waiters, etc.
4. **Validation**: Pre-merge and post-merge model validation
5. **No doc model packages**: Direct trait artifact merging

---

## Smithy Transformer Architecture

### Two Transformer Types

**1. ModelTransformerPlugin** (smithy-model package)
- Plugin-based system for reacting to model changes
- Invoked automatically when shapes are removed
- Discovered via Java SPI
- Use case: Automatic cleanup of dependent shapes

**2. ProjectionTransformer** (smithy-build package)
- Build-time model transformations
- Applied during projection creation
- Discovered via Java SPI
- Use case: Build pipeline transformations (e.g., `excludeTraits`, `includeTraits`)

### Core API: ModelTransformer Class

The `ModelTransformer` class provides high-level transformation methods:

```java
ModelTransformer transformer = ModelTransformer.create();

// Trait operations
transformer.removeTraitsIf(model, predicate);
transformer.filterTraits(model, predicate);
transformer.mapTraits(model, mapper);

// Shape operations
transformer.removeShapes(model, shapes);
transformer.replaceShapes(model, shapes);
transformer.filterShapes(model, predicate);
transformer.renameShapes(model, renameMap);
```

---

## Existing Transformers Analysis

### Built-in Trait Transformers (smithy-build)

| Transformer | Purpose | Configuration |
|------------|---------|---------------|
| `excludeTraits` | Removes specific traits by ID or namespace | `traits: ["smithy.api#documentation"]` |
| `includeTraits` | Keeps only specified traits | `traits: ["smithy.api#documentation"]` |
| `excludeTraitsByTag` | Removes traits by tag | `tags: ["internal"]` |
| `includeTraitsByTag` | Keeps traits by tag | `tags: ["external"]` |

### Model Assembler (Native Merging)

The `Model.assembler()` API provides built-in model merging with priority resolution:

```java
Model result = Model.assembler()
    .addImport("staged-model.smithy")      // Priority 1
    .addImport("ai-traits.json")           // Priority 2 (overwrites P1)
    .addImport("user-overrides.json")      // Priority 3 (overwrites P2)
    .assemble()
    .unwrap();
```

**Key Feature:** Later imports automatically override earlier ones for conflicting traits.

---

## Implementation Approach for Model Merge Task

### Step 1: Remove Old Documentation Traits

Use existing `ModelTransformer.removeTraitsIf()`:

```java
ModelTransformer transformer = ModelTransformer.create();
Model withoutOldDocs = transformer.removeTraitsIf(stagedModel,
    (shape, trait) -> trait instanceof DocumentationTrait);
```

### Step 2: Convert Fusion Trait Artifacts to Smithy JSON

Fusion Agents output format:
```json
{
  "com.amazonaws.bedrock#CreateCustomModel": {
    "smithy.api#documentation": "<p>AI-generated docs</p>"
  }
}
```

Convert to Smithy JSON with `"type": "apply"`:
```json
{
  "smithy": "2.0",
  "shapes": {
    "com.amazonaws.bedrock#CreateCustomModel": {
      "type": "apply",
      "traits": {
        "smithy.api#documentation": "<p>AI-generated docs</p>"
      }
    }
  }
}
```

**Implementation:**
```java
private ObjectNode convertToSmithyApplyFormat(Map<String, Map<String, Object>> traitArtifact) {
    ObjectNode.Builder shapes = Node.objectNodeBuilder();
    
    traitArtifact.forEach((shapeId, traits) -> {
        shapes.withMember(shapeId, Node.objectNodeBuilder()
            .withMember("type", "apply")
            .withMember("traits", buildTraitsNode(traits))
            .build());
    });
    
    return Node.objectNodeBuilder()
        .withMember("smithy", "2.0")
        .withMember("shapes", shapes.build())
        .build();
}
```

### Step 3: Merge with Priority Resolution

Use `Model.assembler()` for priority-based merging:

```java
Model externalModel = Model.assembler()
    .addModel(withoutOldDocs)              // Base model
    .addImport("ai-traits.json")           // AI-generated traits
    .addImport("user-overrides.json")      // User overrides (highest priority)
    .assemble()
    .unwrap();
```

### Step 4: Validate

```java
ValidatedResult<Model> validated = Model.assembler()
    .addModel(externalModel)
    .assemble();

if (validated.isBroken()) {
    // Handle validation errors
    List<ValidationEvent> errors = validated.getValidationEvents();
}
```

---

## Complete Implementation

```java
public class ModelMergeTask {
    
    public Model mergeTraitArtifacts(
            Model stagedModel,
            Map<String, Map<String, Object>> aiTraits,
            Map<String, Map<String, Object>> userOverrides) {
        
        ModelTransformer transformer = ModelTransformer.create();
        
        // Step 1: Remove old documentation traits
        Model withoutOldDocs = transformer.removeTraitsIf(stagedModel,
            (shape, trait) -> trait instanceof DocumentationTrait);
        
        // Step 2: Convert trait artifacts to Smithy JSON
        Path aiTraitsFile = writeSmithyJson(aiTraits, "ai-traits.json");
        Path overridesFile = writeSmithyJson(userOverrides, "user-overrides.json");
        
        // Step 3: Merge with priority resolution
        ValidatedResult<Model> result = Model.assembler()
            .addModel(withoutOldDocs)
            .addImport(aiTraitsFile)
            .addImport(overridesFile)
            .assemble();
        
        // Step 4: Validate
        if (result.isBroken()) {
            throw new ModelMergeException("Model validation failed", 
                result.getValidationEvents());
        }
        
        return result.unwrap();
    }
    
    private Path writeSmithyJson(Map<String, Map<String, Object>> traits, String filename) {
        ObjectNode smithyJson = convertToSmithyApplyFormat(traits);
        Path path = Paths.get(filename);
        Files.writeString(path, Node.prettyPrintJson(smithyJson));
        return path;
    }
}
```

---

## Custom Transformer Assessment

### Do We Need Custom Transformers?

**Answer: NO**

| Requirement | Existing Solution |
|------------|-------------------|
| Remove old traits | `ModelTransformer.removeTraitsIf()` |
| Apply new traits | `Model.assembler().addImport()` with `"type": "apply"` |
| Priority resolution | Import ordering in Model Assembler |
| Validation | `Model.assembler().assemble()` returns `ValidatedResult` |
| Multiple trait types | Smithy JSON supports all trait types |

### When Would Custom Transformers Be Needed?

Custom transformers would only be required for:

1. **Complex trait transformations** - Modifying trait values based on business logic
2. **Cross-shape dependencies** - Traits that depend on other shapes' traits
3. **Build pipeline integration** - If using `smithy-build.json` projections

**For the Model Merge Task:** None of these apply. The task is straightforward trait merging with priority resolution.

---

## Alignment with E2E Technical Flow

From the Trebuchet/Fusion E2E Technical Flow document:

> "Feed the shipped Smithy model and generated doc traits into the Smithy model assembler. Execute the assembler to obtain the combined model."

This explicitly recommends using the Model Assembler, which is exactly what this implementation does.

---

## Recommendations

### For Immediate Implementation

1. **Use existing APIs only** - No custom transformer development needed
2. **Focus on format conversion** - Convert Fusion trait artifact format to Smithy JSON
3. **Leverage Model Assembler** - Use import ordering for priority resolution
4. **Implement validation** - Use `ValidatedResult` for pre/post-merge validation

### For Future Extensibility

If additional trait types (waiters, examples) need merging:

1. **Same pattern applies** - Convert to Smithy JSON with `"type": "apply"`
2. **Add to import chain** - Append to Model Assembler import list
3. **Priority via ordering** - Later imports override earlier ones

### Code Reusability

The format conversion logic can be extracted into a utility class:

```java
public class TraitArtifactConverter {
    public static ObjectNode toSmithyJson(Map<String, Map<String, Object>> traits);
    public static Path writeToFile(ObjectNode smithyJson, Path outputPath);
}
```

---

## Conclusion

The Model Merge Task requires **zero custom transformers**. Smithy's existing infrastructure provides:

- ✅ Trait removal via `ModelTransformer.removeTraitsIf()`
- ✅ Trait application via `Model.assembler()` with `"type": "apply"`
- ✅ Priority resolution via import ordering
- ✅ Validation via `ValidatedResult`

**Implementation effort:** Focus on converting Fusion trait artifact format to Smithy JSON. All model manipulation uses existing Smithy APIs.

**Timeline impact:** Eliminates custom transformer development, testing, and maintenance overhead.

---

## References

- Smithy Model Transformer: `smithy-model/src/main/java/software/amazon/smithy/model/transform/ModelTransformer.java`
- Model Assembler: `smithy-model/src/main/java/software/amazon/smithy/model/loader/ModelAssembler.java`
- Existing Transformers: `smithy-build/src/main/java/software/amazon/smithy/build/transforms/`
- Trebuchet/Fusion E2E Flow: https://quip-amazon.com/G4LfAJA6nnZe
- Federated Model Build Requirements: https://quip-amazon.com/IttmAnDbdPi5
- Fusion Agents Requirements: https://quip-amazon.com/TCw9AUe5zS4i
