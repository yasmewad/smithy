/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
apply cleanly because Java compilation and Javadoc generation are no-ops when
// there are no Java sources, and the sources/jar tasks simply package the
// markdown resources.
plugins {
    id("smithy.module-conventions")
}

description =
    "Curated, cross-harness Smithy authoring & validation skills (Agent Skills open standard) for AI coding agents."

extra["displayName"] = "Smithy :: Agent Skills"
extra["moduleName"] = "software.amazon.smithy.agent.skills"

// Package the curated skill files into the JAR under a clear, namespaced
// resource path so consumers can extract them deterministically from the
// classpath. The skills/ directory remains the single canonical copy on disk
// (browsable for the plugin marketplace) - it is not duplicated; Gradle reads
// it in place and relocates it to the target path only inside the JAR.
sourceSets {
    main {
        resources {
            srcDir("skills")
        }
    }
}

tasks.processResources {
    // Relocate everything contributed by the skills/ srcDir under a stable,
    // vendor-neutral prefix inside the artifact.
    eachFile {
        relativePath = relativePath.prepend("META-INF", "smithy", "agent-skills")
    }
    // Avoid leaving the (now relocated) originals at the resource root.
    includeEmptyDirs = false
}
