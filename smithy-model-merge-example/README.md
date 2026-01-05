# Smithy Model Merge Example

This module demonstrates how to merge trait artifacts from multiple sources (AI-generated documentation, user overrides) into a Smithy model with priority-based resolution.

## Overview

The example shows the complete workflow for the Trebuchet/Fusion integration Model Merge Task:

1. **Remove old documentation traits** from base model
2. **Convert trait artifacts** to Smithy JSON format with `"type": "apply"`
3. **Merge with priority resolution** using Model Assembler
4. **Validate** the merged model

## Key Classes

- `ModelMerger` - Main class that orchestrates the merge workflow
- `TraitArtifactConverter` - Converts Fusion trait artifact format to Smithy JSON

## Usage Example

```java
// Load base model
Model baseModel = Model.assembler()
    .addImport("service-model.smithy")
    .assemble()
    .unwrap();

// Create AI-generated trait artifacts
Map<String, Map<String, Object>> aiTraits = new HashMap<>();
Map<String, Object> serviceTraits = new HashMap<>();
serviceTraits.put("smithy.api#documentation", 
    "<p>AI-generated documentation</p>");
aiTraits.put("com.example.weather#WeatherService", serviceTraits);

// Create user override trait artifacts (higher priority)
Map<String, Map<String, Object>> userOverrides = new HashMap<>();
Map<String, Object> overrideTraits = new HashMap<>();
overrideTraits.put("smithy.api#documentation",
    "<p>User override documentation</p>");
userOverrides.put("com.example.weather#GetWeather", overrideTraits);

// Merge trait artifacts
ModelMerger merger = new ModelMerger();
Model mergedModel = merger.mergeTraitArtifacts(baseModel, aiTraits, userOverrides);
```

## Trait Artifact Format

Input format (from Fusion Agents):
```json
{
  "com.example.weather#WeatherService": {
    "smithy.api#documentation": "<p>AI-generated docs</p>"
  }
}
```

Converted to Smithy JSON:
```json
{
  "smithy": "2.0",
  "shapes": {
    "com.example.weather#WeatherService": {
      "type": "apply",
      "traits": {
        "smithy.api#documentation": "<p>AI-generated docs</p>"
      }
    }
  }
}
```

## Priority Resolution

Traits are merged in priority order:
1. Base model (lowest priority)
2. AI-generated traits
3. User overrides (highest priority)

Later imports override earlier ones for conflicting traits.

## Running Tests

```bash
./gradlew :smithy-model-merge-example:test
```

## Key Findings

**No custom transformers needed!** This example uses only existing Smithy APIs:

- `ModelTransformer.removeTraitsIf()` - removes old traits
- `Model.assembler()` - merges models with priority resolution
- `ValidatedResult` - validates merged model

The only custom code is format conversion from Fusion trait artifacts to Smithy JSON.
