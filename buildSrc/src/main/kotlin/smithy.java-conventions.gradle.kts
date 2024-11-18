import org.gradle.api.JavaVersion
import org.gradle.kotlin.dsl.`java-library`
import com.github.spotbugs.snom.Effort
import java.util.regex.Pattern


plugins {
    `java-library`

    // Formatting and error checking
    id("com.github.spotbugs")

    // Dependency analysis
    id("com.autonomousapps.dependency-analysis")

    // Unit Test conventions
    // TODO Uncomment when testing done.
    // id("smithy.test-conventions")
}

// Java language version configuration
java {
    toolchain {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

/*
 * Formatting
 * ==================
 * see: https://github.com/diffplug/spotless/blob/main/plugin-gradle/README.md#java
 */
 // TODO - Enable once the buildSrc plugin conversion is complete.
// spotless {
//     java {
//         // Enforce a common license header on all files
//         licenseHeaderFile("${project.rootDir}/config/spotless/license-header.txt")
//             .onlyIfContentMatches("^((?!SKIPLICENSECHECK)[\\s\\S])*\$")
//         indentWithSpaces()
//         endWithNewline()

//         eclipse().configFile("${project.rootDir}/config/spotless/formatting.xml")

//         // Fixes for some strange formatting applied by eclipse:
//         // see: https://github.com/kamkie/demo-spring-jsf/blob/bcacb9dc90273a5f8d2569470c5bf67b171c7d62/build.gradle.kts#L159
//         custom("Lambda fix") { it.replace("} )", "})").replace("} ,", "},") }
//         custom("Long literal fix") { Pattern.compile("([0-9_]+) [Ll]").matcher(it).replaceAll("\$1L") }

//         // Static first, then everything else alphabetically
//         removeUnusedImports()
//         importOrder("\\#", "")

//         // Ignore generated generated code for formatter check
//         targetExclude("**/build/**/*.*")
//     }

//     // Formatting for build.gradle.kts files
//     kotlinGradle {
//         ktlint()
//         indentWithSpaces()
//         trimTrailingWhitespace()
//         endWithNewline()
//     }
// }
// tasks.spotlessCheck {
//     dependsOn(tasks.spotlessApply)
// }

/*
 * Spotbugs
 * ====================================================
 *
 * Run spotbugs against source files and configure suppressions.
 */
// Configure the spotbugs extension.
spotbugs {
    effort = Effort.MAX
    excludeFilter = file("${project.rootDir}/config/spotbugs/filter.xml")
}

// We don't need to lint tests.
tasks.named("spotbugsTest") {
    enabled = false
}

/*
 * Repositories - these are applied for your current project. Not for the plugins.
 * ================================
 */
repositories {
    mavenLocal()
    mavenCentral()
}