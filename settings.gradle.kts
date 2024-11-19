pluginManagement {
    repositories {
        mavenLocal()
        mavenCentral()
        gradlePluginPortal()
    }

    plugins {
        val smithyGradleVersion : String by settings
        id("software.amazon.smithy.gradle.smithy-jar") version smithyGradleVersion
    }
}

rootProject.name = "smithy"

include(":smithy-aws-iam-traits")
include(":smithy-aws-traits")
include(":smithy-aws-apigateway-traits")
include(":smithy-aws-apigateway-openapi")
include(":smithy-aws-protocol-tests")
include(":smithy-cli")
include(":smithy-codegen-core")
include(":smithy-build")
include(":smithy-model")
include(":smithy-diff")
include(":smithy-linters")
include(":smithy-mqtt-traits")
include(":smithy-jsonschema")
include(":smithy-openapi")
include(":smithy-openapi-traits")
include(":smithy-utils")
include(":smithy-protocol-test-traits")
include(":smithy-jmespath")
include(":smithy-waiters")
include(":smithy-aws-cloudformation-traits")
include(":smithy-aws-cloudformation")
include(":smithy-validation-model")
include(":smithy-rules-engine")
include(":smithy-smoke-test-traits")
include(":smithy-syntax")
include(":smithy-aws-endpoints")
include(":smithy-aws-smoke-test-model")
include(":smithy-protocol-traits")
include(":smithy-protocol-tests")
include(":smithy-trait-codegen")
include(":smithy-docgen-core")
include(":smithy-docgen-test")