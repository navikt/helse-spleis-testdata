plugins {
    kotlin("jvm") version "1.6.21"
}

val junitJupiterVersion = "5.8.2"
val ktorVersion = "2.0.1"
val hikariVersion = "3.3.1"
val flywayVersion = "6.1.3"
val kotliqueryVersion = "1.3.0"
val testContainersVersion = "1.16.3"

allprojects {
    group = "no.nav.helse"

    repositories {
        mavenCentral()
        maven("https://jitpack.io")
    }

    apply(plugin = "org.jetbrains.kotlin.jvm")

    dependencies {
        implementation("com.github.navikt:rapids-and-rivers:2022.05.09-12.50.569dc0a4e492")
        implementation("io.ktor:ktor-server-netty:$ktorVersion")
        implementation("io.ktor:ktor-server-websockets:$ktorVersion")
        implementation("io.ktor:ktor-server-content-negotiation:$ktorVersion")
        implementation("io.ktor:ktor-serialization-jackson:$ktorVersion")

        implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
        implementation("io.ktor:ktor-client-cio-jvm:$ktorVersion")
        implementation("io.ktor:ktor-client-auth-jvm:$ktorVersion")
        implementation("io.ktor:ktor-client-json-jvm:$ktorVersion")
        implementation("io.ktor:ktor-client-jackson:$ktorVersion")
        implementation("io.ktor:ktor-websockets:$ktorVersion")

        implementation("org.apache.kafka:kafka-clients:3.1.0")

        implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.13.3")
        implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.13.3")

        implementation("org.slf4j:slf4j-api:1.7.36")
        implementation("ch.qos.logback:logback-classic:1.2.11")
        implementation("net.logstash.logback:logstash-logback-encoder:7.1.1")

        testImplementation("org.junit.jupiter:junit-jupiter-api:$junitJupiterVersion")
        testImplementation("org.junit.jupiter:junit-jupiter-params:$junitJupiterVersion")
        testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitJupiterVersion")
        testImplementation("io.ktor:ktor-server-test-host:$ktorVersion") {
            exclude("junit")
        }
        testImplementation("io.ktor:ktor-client-mock-jvm:$ktorVersion") {
            exclude("junit")
        }
        testImplementation("io.mockk:mockk:1.12.4")
    }
}


subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")

    tasks {
        withType<Test> {
            useJUnitPlatform()
            testLogging {
                events("skipped", "failed")
            }
        }
    }

    dependencies {
        testImplementation("org.junit.jupiter:junit-jupiter-api:$junitJupiterVersion")
        testImplementation("org.junit.jupiter:junit-jupiter-params:$junitJupiterVersion")
        testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitJupiterVersion")
    }
}
