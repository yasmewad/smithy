/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package software.amazon.smithy.modelmerge;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import software.amazon.smithy.model.Model;
import software.amazon.smithy.model.node.Node;
import software.amazon.smithy.model.node.ObjectNode;
import software.amazon.smithy.model.traits.DocumentationTrait;
import software.amazon.smithy.model.transform.ModelTransformer;
import software.amazon.smithy.model.validation.ValidatedResult;

/**
 * Merges trait artifacts from multiple sources with priority resolution.
 */
public final class ModelMerger {

    private final ModelTransformer transformer;

    public ModelMerger() {
        this.transformer = ModelTransformer.create();
    }

    /**
     * Merges trait artifacts into a base model with priority resolution.
     *
     * @param baseModel Base Smithy model
     * @param aiTraits AI-generated trait artifacts
     * @param userOverrides User override trait artifacts
     * @return Merged and validated model
     * @throws ModelMergeException if merge or validation fails
     */
    public Model mergeTraitArtifacts(
            Model baseModel,
            Map<String, Map<String, Object>> aiTraits,
            Map<String, Map<String, Object>> userOverrides
    ) {

        // Step 1: Remove old documentation traits
        Model withoutOldDocs = transformer.removeTraitsIf(baseModel,
                (shape, trait) -> trait instanceof DocumentationTrait);

        try {
            // Step 2: Merge trait artifacts with priority (user overrides win)
            Map<String, Map<String, Object>> mergedTraits = new java.util.HashMap<>(aiTraits);
            userOverrides.forEach((shapeId, traits) -> {
                mergedTraits.merge(shapeId, traits, (ai, user) -> {
                    Map<String, Object> combined = new java.util.HashMap<>(ai);
                    combined.putAll(user); // User overrides win
                    return combined;
                });
            });

            // Step 3: Convert merged traits to Smithy JSON
            Path mergedTraitsFile = createTempSmithyJson(mergedTraits, "merged-traits");

            // Step 4: Apply merged traits
            ValidatedResult<Model> result = Model.assembler()
                    .addModel(withoutOldDocs)
                    .addImport(mergedTraitsFile)
                    .assemble();

            // Step 5: Validate
            if (result.isBroken()) {
                throw new ModelMergeException("Model validation failed: "
                        + result.getValidationEvents());
            }

            return result.unwrap();

        } catch (IOException e) {
            throw new ModelMergeException("Failed to write trait artifacts", e);
        }
    }

    private Path createTempSmithyJson(Map<String, Map<String, Object>> traits, String prefix)
            throws IOException {
        ObjectNode smithyJson = TraitArtifactConverter.toSmithyJson(traits);
        Path tempFile = Files.createTempFile(prefix, ".json");
        Files.writeString(tempFile, Node.prettyPrintJson(smithyJson));
        return tempFile;
    }

    /**
     * Exception thrown when model merge fails.
     */
    public static class ModelMergeException extends RuntimeException {
        public ModelMergeException(String message) {
            super(message);
        }

        public ModelMergeException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
