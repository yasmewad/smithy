/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
plugins {
    id("smithy.module-conventions")
}

description = "Example demonstrating model merge with trait artifacts"

extra["displayName"] = "Smithy :: Model Merge Example"
extra["moduleName"] = "software.amazon.smithy.modelmerge"

dependencies {
    api(project(":smithy-model"))
}
