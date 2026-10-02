rootProject.name = "spleis-testdata"

// Sett opp repositories basert på om vi kjører i CI eller ikke
// Jf. https://github.com/navikt/utvikling/blob/3eed71e1b493a6a81762c32f2d30521a1a3ccab4/docs/teknisk/Konsumere%20biblioteker%20fra%20Github%20Package%20Registry.md
pluginManagement {
    repositories {
        if (providers.environmentVariable("GITHUB_ACTIONS").orNull == "true" && providers.environmentVariable("AI_AGENT").orNull == null) {
            maven("https://maven.pkg.github.com/navikt/maven-release") {
                credentials {
                    username = "token"
                    password = providers.environmentVariable("GITHUB_TOKEN").orNull!!
                }
            }
        } else {
            maven("https://github-package-registry-mirror.gc.nav.no/cached/maven-release/")
        }
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    // Bare tillat repositories-oppsett her i settings.gradle.kts
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)

    repositories {
        if (providers.environmentVariable("GITHUB_ACTIONS").orNull == "true" && providers.environmentVariable("AI_AGENT").orNull == null) {
            maven("https://maven.pkg.github.com/navikt/maven-release") {
                credentials {
                    username = "token"
                    password = providers.environmentVariable("GITHUB_TOKEN").orNull!!
                }
            }
        } else {
            maven("https://github-package-registry-mirror.gc.nav.no/cached/maven-release/")
        }
        mavenCentral()
        // Node.js-distribusjonen som node-gradle-pluginen laster ned for å bygge frontenden
        ivy("https://nodejs.org/dist/") {
            name = "Node.js"
            patternLayout {
                artifact("v[revision]/[artifact](-v[revision]-[classifier]).[ext]")
            }
            metadataSources {
                artifact()
            }
            content {
                includeModule("org.nodejs", "node")
            }
        }
    }
}
