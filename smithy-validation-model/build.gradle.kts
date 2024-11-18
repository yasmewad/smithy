/*
 * Copyright 2021 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

plugins {
    id("software.amazon.smithy.gradle.smithy-jar")
    id("smithy.java-conventions")
    id("smithy.test-conventions")
}

description = "This module provides support for validation in Smithy server SDKs"

extra["displayName"] = "Smithy :: Validation Support"
extra["moduleName"] = "software.amazon.smithy.validation.model"

dependencies {
    implementation(project(path = ":smithy-cli", configuration = "shadow"))
}

tasks {
    sourcesJar {
        dependsOn("smithyJarStaging")
    }
}

smithy {
    smithyBuildConfigs.set(project.files())
}
