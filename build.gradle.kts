plugins {
    kotlin("jvm") version "2.3.0"
}

val tbdLibsVersion = "2026.01.22-09.16-1d3f6039"
val rapidsAndRiversVersion = "2026011411051768385145.e8ebad1177b4"
val junitJupiterVersion = "5.12.1"
val ktorVersion = "3.2.3"
group = "no.nav.helse"

// Sett opp repositories basert på om vi kjører i CI eller ikke
// Jf. https://github.com/navikt/utvikling/blob/main/docs/teknisk/Konsumere%20biblioteker%20fra%20Github%20Package%20Registry.md
repositories {
    mavenCentral()
    if (providers.environmentVariable("GITHUB_ACTIONS").orNull == "true") {
        maven {
            url = uri("https://maven.pkg.github.com/navikt/maven-release")
            credentials {
                username = "token"
                password = providers.environmentVariable("GITHUB_TOKEN").orNull!!
            }
        }
    } else {
        maven("https://repo.adeo.no/repository/github-package-registry-navikt/")
    }
}

apply(plugin = "org.jetbrains.kotlin.jvm")

dependencies {
    implementation("com.github.navikt.tbd-libs:naisful-app:$tbdLibsVersion")
    implementation("com.github.navikt.tbd-libs:azure-token-client-default:$tbdLibsVersion")
    implementation("com.github.navikt.tbd-libs:speed-client:$tbdLibsVersion")
    implementation("com.github.navikt:rapids-and-rivers:$rapidsAndRiversVersion")
    implementation("io.ktor:ktor-server-websockets:$ktorVersion")

    implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-client-cio-jvm:$ktorVersion")
    implementation("io.ktor:ktor-client-auth-jvm:$ktorVersion")
    implementation("io.ktor:ktor-client-json-jvm:$ktorVersion")
    implementation("io.ktor:ktor-client-jackson:$ktorVersion")
    implementation("io.ktor:ktor-websockets:$ktorVersion")

    testImplementation("com.github.navikt.tbd-libs:rapids-and-rivers-test:$tbdLibsVersion")
    testImplementation("com.github.navikt.tbd-libs:naisful-test-app:$tbdLibsVersion")
    testImplementation("io.mockk:mockk:1.13.9")

    testImplementation("io.ktor:ktor-client-mock-jvm:$ktorVersion") {
        exclude("junit")
    }

    testImplementation("org.junit.jupiter:junit-jupiter:$junitJupiterVersion")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of("21"))
    }
}

tasks {
    named<Jar>("jar") {
        archiveBaseName.set("app")

        manifest {
            attributes["Main-Class"] = "no.nav.helse.testdata.AppKt"
            attributes["Class-Path"] = configurations.runtimeClasspath.get().joinToString(separator = " ") {
                it.name
            }
        }

        doLast {
            configurations.runtimeClasspath.get().forEach {
                val file = File("${layout.buildDirectory.get()}/libs/${it.name}")
                if (!file.exists())
                    it.copyTo(file)
            }
        }
    }

    withType<Test> {
        useJUnitPlatform()
        testLogging {
            events("passed", "skipped", "failed")
        }
    }
}
