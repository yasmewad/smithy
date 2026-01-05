/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package software.amazon.smithy.modelmerge;

import java.util.HashMap;
import java.util.Map;
import software.amazon.smithy.model.Model;
import software.amazon.smithy.model.shapes.ShapeId;
import software.amazon.smithy.model.traits.DocumentationTrait;

/**
 * Example demonstrating the model merge workflow.
 */
public final class ExampleRunner {

    public static void main(String[] args) {
        System.out.println("=== Smithy Model Merge Example ===\n");

        // Load base model with old documentation
        System.out.println("1. Loading base model...");
        Model baseModel = Model.assembler()
                .addImport(ExampleRunner.class.getResource("/base-model.smithy"))
                .assemble()
                .unwrap();

        ShapeId serviceId = ShapeId.from("com.example.weather#WeatherService");
        String oldDocs = baseModel.expectShape(serviceId)
                .expectTrait(DocumentationTrait.class)
                .getValue();
        System.out.println("   Old documentation: " + oldDocs);

        // Create AI-generated trait artifacts
        System.out.println("\n2. Creating AI-generated trait artifacts...");
        Map<String, Map<String, Object>> aiTraits = new HashMap<>();

        Map<String, Object> serviceTraits = new HashMap<>();
        serviceTraits.put("smithy.api#documentation",
                "<p>AI-generated: Weather service providing current conditions and forecasts.</p>");
        aiTraits.put("com.example.weather#WeatherService", serviceTraits);

        Map<String, Object> operationTraits = new HashMap<>();
        operationTraits.put("smithy.api#documentation",
                "<p>AI-generated: Retrieves current weather for a city.</p>");
        aiTraits.put("com.example.weather#GetWeather", operationTraits);

        System.out.println("   Added AI docs for 2 shapes");

        // Create user override trait artifacts
        System.out.println("\n3. Creating user override trait artifacts...");
        Map<String, Map<String, Object>> userOverrides = new HashMap<>();

        Map<String, Object> overrideTraits = new HashMap<>();
        overrideTraits.put("smithy.api#documentation",
                "<p>User override: Get real-time weather data for any city worldwide.</p>");
        userOverrides.put("com.example.weather#GetWeather", overrideTraits);

        System.out.println("   Added user override for GetWeather operation");

        // Merge trait artifacts
        System.out.println("\n4. Merging trait artifacts with priority resolution...");
        ModelMerger merger = new ModelMerger();
        Model mergedModel = merger.mergeTraitArtifacts(baseModel, aiTraits, userOverrides);

        // Display results
        System.out.println("\n5. Results:");
        System.out.println("   ✓ Service documentation (from AI):");
        String serviceDocs = mergedModel.expectShape(serviceId)
                .expectTrait(DocumentationTrait.class)
                .getValue();
        System.out.println("     " + serviceDocs);

        ShapeId opId = ShapeId.from("com.example.weather#GetWeather");
        System.out.println("\n   ✓ Operation documentation (from user override, not AI):");
        String opDocs = mergedModel.expectShape(opId)
                .expectTrait(DocumentationTrait.class)
                .getValue();
        System.out.println("     " + opDocs);

        ShapeId inputId = ShapeId.from("com.example.weather#GetWeatherInput");
        boolean hasInputDocs = mergedModel.expectShape(inputId).hasTrait(DocumentationTrait.class);
        System.out.println("\n   ✓ Input structure documentation removed: " + !hasInputDocs);

        System.out.println("\n=== Model merge completed successfully! ===");
    }
}
