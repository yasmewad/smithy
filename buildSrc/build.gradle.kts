plugins {
    `kotlin-dsl`
}


// This configures sources where libraries come from for the buildSrc plugins.
repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation(libs.spotbugs)
    implementation(libs.spotless)
    implementation(libs.dependency.analysis)

    // https://github.com/gradle/gradle/issues/15383
    implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))
}
