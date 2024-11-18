import org.gradle.api.tasks.testing.logging.TestExceptionFormat

plugins {
    `java-library`
}

// Workaround per: https://github.com/gradle/gradle/issues/15383
val Project.libs get() = the<org.gradle.accessors.dm.LibrariesForLibs>()

dependencies {
    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.jupiter.engine)
    testImplementation(libs.junit.jupiter.params)
    testImplementation(libs.hamcrest)
    testImplementation(libs.apiguardian)
}

tasks.test {
    useJUnitPlatform()

    // Log on passed, skipped, and failed test events if the `-Plog-tests` property is set.
    // https://docs.gradle.org/current/javadoc/org/gradle/api/tasks/testing/logging/TestLoggingContainer.html
    if (project.hasProperty("log-tests")) {
            testLogging.apply {
                events("passed", "skipped", "failed")
                exceptionFormat = TestExceptionFormat.FULL
            }
        }
}

