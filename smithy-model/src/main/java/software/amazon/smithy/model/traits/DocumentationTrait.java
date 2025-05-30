/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package software.amazon.smithy.model.traits;

import software.amazon.smithy.model.SourceLocation;
import software.amazon.smithy.model.shapes.ShapeId;

/**
 * Adds documentation to a model.
 */
public final class DocumentationTrait extends StringTrait {
    public static final ShapeId ID = ShapeId.from("smithy.api#documentation");

    public DocumentationTrait(String value, SourceLocation sourceLocation) {
        super(ID, value, sourceLocation);
    }

    public DocumentationTrait(String value) {
        this(value, SourceLocation.NONE);
    }

    public static final class Provider extends StringTrait.Provider<DocumentationTrait> {
        public Provider() {
            super(ID, DocumentationTrait::new);
        }
    }
}
