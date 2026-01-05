/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package software.amazon.smithy.modelmerge;

import java.util.Map;
import software.amazon.smithy.model.node.Node;
import software.amazon.smithy.model.node.ObjectNode;

/**
 * Converts Fusion trait artifacts to Smithy JSON format with "type": "apply".
 */
public final class TraitArtifactConverter {

    private TraitArtifactConverter() {}

    /**
     * Converts a trait artifact map to Smithy JSON format.
     *
     * @param traitArtifact Map of shape IDs to trait maps
     * @return Smithy JSON as ObjectNode
     */
    public static ObjectNode toSmithyJson(Map<String, Map<String, Object>> traitArtifact) {
        ObjectNode.Builder shapes = Node.objectNodeBuilder();

        traitArtifact.forEach((shapeId, traits) -> {
            shapes.withMember(shapeId,
                    Node.objectNodeBuilder()
                            .withMember("type", "apply")
                            .withMember("traits", buildTraitsNode(traits))
                            .build());
        });

        return Node.objectNodeBuilder()
                .withMember("smithy", "2.0")
                .withMember("shapes", shapes.build())
                .build();
    }

    private static ObjectNode buildTraitsNode(Map<String, Object> traits) {
        ObjectNode.Builder traitsNode = Node.objectNodeBuilder();

        traits.forEach((traitName, traitValue) -> {
            if (traitValue instanceof String) {
                traitsNode.withMember(traitName, Node.from((String) traitValue));
            } else if (traitValue instanceof Number) {
                traitsNode.withMember(traitName, Node.from((Number) traitValue));
            } else if (traitValue instanceof Boolean) {
                traitsNode.withMember(traitName, Node.from((Boolean) traitValue));
            } else {
                // For complex objects, convert to string representation
                traitsNode.withMember(traitName, Node.from(traitValue.toString()));
            }
        });

        return traitsNode.build();
    }
}
