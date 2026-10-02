import com.github.gradle.node.pnpm.task.PnpmTask

group = "no.nav.helse"

plugins {
    alias(libs.plugins.sykepenger.deployable)
    alias(libs.plugins.nodeGradle)
}

sykepengerDeployable {
    mainClass = "no.nav.helse.testdata.AppKt"
}

dependencies {
    implementation(libs.tbdLibs.naisfulApp)
    implementation(libs.tbdLibs.azureTokenClientDefault)
    implementation(libs.tbdLibs.speedClient)
    implementation(libs.rapidsAndRivers)
    implementation(libs.ktor.server.websockets)
    implementation(libs.bundles.ktor.client)

    implementation(libs.hikaricp)
    implementation(libs.postgresql)
    implementation(libs.kotliquery)
    implementation(libs.flyway.core)
    implementation(libs.flyway.databasePostgresql)

    testImplementation(libs.tbdLibs.rapidsAndRiversTest)
    testImplementation(libs.tbdLibs.naisfulTestApp)
    testImplementation(libs.mockk)
    testImplementation(libs.testcontainers.postgresql)
    testImplementation(libs.ktor.client.mock) {
        exclude("junit")
    }
}

// Frontenden bygges med Node og pnpm som Gradle laster ned selv, slik at standard-workflowene
// (som bare kjører Gradle) både tester frontenden og får den med i imaget.
node {
    download = true
    version = "24.21.0"
    pnpmVersion = "10.28.0"
    // Repositoryet for Node-distribusjonen er satt opp i settings.gradle.kts (FAIL_ON_PROJECT_REPOS)
    distBaseUrl.set(null as String?)
    nodeProjectDir = layout.projectDirectory.dir("frontend")
}

tasks.pnpmInstall {
    // Ikke-interaktiv installasjon med låst lockfile, også lokalt
    environment = mapOf("CI" to "true")
}

val frontendKilder =
    files(
        "frontend/src",
        "frontend/index.html",
        "frontend/package.json",
        "frontend/pnpm-lock.yaml",
        "frontend/pnpm-workspace.yaml",
        "frontend/tsconfig.json",
        "frontend/vite.config.ts",
    )

val frontendTypesjekk by tasks.registering(PnpmTask::class) {
    group = "frontend"
    dependsOn(tasks.pnpmInstall)
    pnpmCommand = listOf("run", "tsc")
    inputs.files(frontendKilder)
    outputs.upToDateWhen { true }
}

val frontendTest by tasks.registering(PnpmTask::class) {
    group = "frontend"
    dependsOn(tasks.pnpmInstall)
    pnpmCommand = listOf("run", "test")
    environment = mapOf("TZ" to "UTC")
    inputs.files(frontendKilder)
    outputs.upToDateWhen { true }
}

val frontendBygg by tasks.registering(PnpmTask::class) {
    group = "frontend"
    dependsOn(tasks.pnpmInstall)
    pnpmCommand = listOf("run", "build")
    inputs.files(frontendKilder)
    outputs.dir(layout.projectDirectory.dir("public"))
}

tasks.named("check") {
    dependsOn(frontendTypesjekk, frontendTest)
}

jib {
    container {
        workingDirectory = "/app"
    }
    extraDirectories {
        paths {
            path {
                setFrom(layout.projectDirectory.dir("public"))
                into = "/app/public"
            }
        }
    }
}

tasks.matching { it.name in setOf("jib", "jibDockerBuild", "jibBuildTar") }.configureEach {
    dependsOn(frontendBygg)
}
