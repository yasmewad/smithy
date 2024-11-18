/*
 * Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.
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
    id("smithy.java-conventions")
    id("smithy.test-conventions")
}

description = "Defines Smithy waiters."

extra["displayName"] = "Smithy :: Waiters"
extra["moduleName"] = "software.amazon.smithy.waiters"

dependencies {
    api(project(":smithy-model"))
    api(project(":smithy-jmespath"))
}
