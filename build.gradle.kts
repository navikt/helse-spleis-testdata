val junitJupiterVersion = "5.5.2"
val ktorVersion = "1.5.1"
val hikariVersion = "3.3.1"
val flywayVersion = "6.1.3"
val vaultJdbcVersion = "1.3.1"
val kotliqueryVersion = "1.3.0"

plugins {
    kotlin("jvm") version "1.4.30"
}

group = "no.nav.helse"

repositories {
    mavenCentral()
    maven("http://packages.confluent.io/maven/")
    maven("https://dl.bintray.com/kotlin/kotlinx")
    maven("https://jitpack.io")
}

dependencies {
    implementation("com.github.navikt:rapids-and-rivers:20210617121814-3e67e4d")
    implementation("io.ktor:ktor-server-netty:$ktorVersion")
    implementation("io.ktor:ktor-jackson:$ktorVersion")
    implementation("io.ktor:ktor-metrics-micrometer:$ktorVersion")
    implementation("io.ktor:ktor-client-cio-jvm:$ktorVersion")
    implementation("io.ktor:ktor-client-auth-jvm:$ktorVersion")
    implementation("io.ktor:ktor-client-json-jvm:$ktorVersion")
    implementation("io.ktor:ktor-client-jackson:$ktorVersion")

    implementation("io.micrometer:micrometer-registry-prometheus:1.1.6")

    implementation("org.apache.kafka:kafka-clients:2.4.0")

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.9.10")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.9.10")

    implementation("org.slf4j:slf4j-api:1.7.29")
    implementation("ch.qos.logback:logback-classic:1.2.3")
    implementation("net.logstash.logback:logstash-logback-encoder:6.2")

    implementation("com.zaxxer:HikariCP:$hikariVersion")
    implementation("no.nav:vault-jdbc:$vaultJdbcVersion")
    implementation("com.github.seratch:kotliquery:$kotliqueryVersion")

    testImplementation("org.flywaydb:flyway-core:$flywayVersion")
    testImplementation("com.opentable.components:otj-pg-embedded:0.13.1")

    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitJupiterVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-params:$junitJupiterVersion")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitJupiterVersion")
    testImplementation("io.ktor:ktor-server-test-host:$ktorVersion") {
        exclude("junit")
    }
    testImplementation("io.ktor:ktor-client-mock-jvm:$ktorVersion") {
        exclude("junit")
    }
    testImplementation("io.mockk:mockk:1.10.6")
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "15"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "15"
    }

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
                val file = File("$buildDir/libs/${it.name}")
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
