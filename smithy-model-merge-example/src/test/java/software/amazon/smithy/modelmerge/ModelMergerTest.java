/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package software.amazon.smithy.modelmerge;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import software.amazon.smithy.model.Model;
import software.amazon.smithy.model.shapes.ShapeId;
import software.amazon.smithy.model.traits.DocumentationTrait;

public class ModelMergerTest {

    @Test
    public void testMergeTraitArtifacts() {
        // Load base model
        Model baseModel = Model.assembler()
                .addImport(getClass().getResource("/base-model.smithy"))
                .assemble()
                .unwrap();

        // Verify base model has old documentation
        ShapeId serviceId = ShapeId.from("com.example.weather#WeatherService");
        assertTrue(baseModel.expectShape(serviceId).hasTrait(DocumentationTrait.class));
        String oldDoc = baseModel.expectShape(serviceId)
                .expectTrait(DocumentationTrait.class)
                .getValue();
        assertEquals("Old documentation that will be replaced", oldDoc);

        // Create AI-generated trait artifacts
        Map<String, Map<String, Object>> aiTraits = new HashMap<>();
        Map<String, Object> serviceTraits = new HashMap<>();
        serviceTraits.put("smithy.api#documentation",
                "<p>AI-generated: Weather service providing current conditions and forecasts.</p>");
        aiTraits.put("com.example.weather#WeatherService", serviceTraits);

        Map<String, Object> operationTraits = new HashMap<>();
        operationTraits.put("smithy.api#documentation",
                "<p>AI-generated: Retrieves current weather for a city.</p>");
        aiTraits.put("com.example.weather#GetWeather", operationTraits);

        // Create user override trait artifacts (higher priority)
        Map<String, Map<String, Object>> userOverrides = new HashMap<>();
        Map<String, Object> overrideTraits = new HashMap<>();
        overrideTraits.put("smithy.api#documentation",
                "<p>User override: Get real-time weather data for any city worldwide.</p>");
        userOverrides.put("com.example.weather#GetWeather", overrideTraits);

        // Merge trait artifacts
        ModelMerger merger = new ModelMerger();
        Model mergedModel = merger.mergeTraitArtifacts(baseModel, aiTraits, userOverrides);

        // Verify service has AI-generated documentation
        String serviceDocs = mergedModel.expectShape(serviceId)
                .expectTrait(DocumentationTrait.class)
                .getValue();
        assertEquals("<p>AI-generated: Weather service providing current conditions and forecasts.</p>",
                serviceDocs);

        // Verify operation has user override (not AI-generated)
        ShapeId opId = ShapeId.from("com.example.weather#GetWeather");
        String opDocs = mergedModel.expectShape(opId)
                .expectTrait(DocumentationTrait.class)
                .getValue();
        assertEquals("<p>User override: Get real-time weather data for any city worldwide.</p>",
                opDocs);

        // Verify shapes without new docs have no documentation trait
        ShapeId inputId = ShapeId.from("com.example.weather#GetWeatherInput");
        assertFalse(mergedModel.expectShape(inputId).hasTrait(DocumentationTrait.class));
    }

    @Test
    public void testEmptyTraitArtifacts() {
        Model baseModel = Model.assembler()
                .addImport(getClass().getResource("/base-model.smithy"))
                .assemble()
                .unwrap();

        ModelMerger merger = new ModelMerger();
        Model mergedModel = merger.mergeTraitArtifacts(
                baseModel,
                new HashMap<>(),
                new HashMap<>());

        // All documentation traits should be removed
        ShapeId serviceId = ShapeId.from("com.example.weather#WeatherService");
        assertFalse(mergedModel.expectShape(serviceId).hasTrait(DocumentationTrait.class));
    }

    @Test
    public void testPriorityResolution() {
        Model baseModel = Model.assembler()
                .addImport(getClass().getResource("/base-model.smithy"))
                .assemble()
                .unwrap();

        // Both AI and user provide docs for same shape
        Map<String, Map<String, Object>> aiTraits = new HashMap<>();
        Map<String, Object> aiServiceTraits = new HashMap<>();
        aiServiceTraits.put("smithy.api#documentation", "<p>AI version</p>");
        aiTraits.put("com.example.weather#WeatherService", aiServiceTraits);

        Map<String, Map<String, Object>> userOverrides = new HashMap<>();
        Map<String, Object> userServiceTraits = new HashMap<>();
        userServiceTraits.put("smithy.api#documentation", "<p>User version</p>");
        userOverrides.put("com.example.weather#WeatherService", userServiceTraits);

        ModelMerger merger = new ModelMerger();
        Model mergedModel = merger.mergeTraitArtifacts(baseModel, aiTraits, userOverrides);

        // User override should win
        ShapeId serviceId = ShapeId.from("com.example.weather#WeatherService");
        String docs = mergedModel.expectShape(serviceId)
                .expectTrait(DocumentationTrait.class)
                .getValue();
        assertEquals("<p>User version</p>", docs);
    }
}
