rootProject.name = "java-file-indexer"

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories { mavenCentral() }
}

include(
    "jfi-core",
    "jfi-analysis",
    "jfi-index",
    "jfi-io",
    "jfi-search",
    "jfi-cli",
    "jfi-examples"
)
