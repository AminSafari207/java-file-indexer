import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.plugins.JavaApplication
import org.gradle.api.tasks.testing.logging.TestExceptionFormat

group = "com.jfi"
version = "0.1.0-SNAPSHOT"

val javaVersion = JavaLanguageVersion.of(21)

val junitJupiter = "org.junit.jupiter:junit-jupiter:5.10.2"
val junitLauncher = "org.junit.platform:junit-platform-launcher:1.10.2"
val slf4jApi = "org.slf4j:slf4j-api:2.0.13"
val logback = "ch.qos.logback:logback-classic:1.5.6"
val picocli = "info.picocli:picocli:4.7.6"

subprojects {
    plugins.apply("java-library")

    extensions.configure<JavaPluginExtension> {
        toolchain.languageVersion.set(javaVersion)
        withSourcesJar()
        withJavadocJar()
    }

    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
        testLogging {
            events("passed", "failed", "skipped")
            exceptionFormat = TestExceptionFormat.FULL
        }
    }

    dependencies {
        add("implementation", slf4jApi)
        add("testImplementation", junitJupiter)
        add("testRuntimeOnly", junitLauncher)
    }
}

////////////////////////////////////
/// Module wiring //////////////////
////////////////////////////////////

project(":jfi-core") {
}

project(":jfi-analysis") {
    dependencies { add("api", project(":jfi-core")) }
}

project(":jfi-index") {
    dependencies {
        add("api", project(":jfi-core"))
        add("implementation", project(":jfi-analysis"))
    }
}

project(":jfi-io") {
    dependencies { add("api", project(":jfi-core")) }
}

project(":jfi-search") {
    dependencies {
        add("api", project(":jfi-core"))
        add("implementation", project(":jfi-analysis"))
        add("implementation", project(":jfi-index"))
    }
}

project(":jfi-cli") {
    plugins.apply("application")

    extensions.configure<JavaApplication> {
        mainClass.set("com.jfi.cli.Main")
    }

    dependencies {
        add("implementation", project(":jfi-search"))
        add("implementation", project(":jfi-index"))
        add("implementation", project(":jfi-analysis"))
        add("implementation", project(":jfi-io"))
        add("implementation", project(":jfi-core"))

        add("implementation", picocli)
        add("runtimeOnly", logback)
        add("testImplementation", junitJupiter)
        add("testRuntimeOnly", junitLauncher)
    }
}

project(":jfi-examples") {
}

tasks.register("checks") {
    group = "verification"
    description = "Run tests on all modules."
    dependsOn(subprojects.map { it.path + ":test" })
}
