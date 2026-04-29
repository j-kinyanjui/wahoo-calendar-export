import com.diffplug.spotless.kotlin.KtfmtStep

val ktor_version: String by project
val kotlin_version: String by project
val logback_version: String by project

plugins {
    kotlin("jvm") version "2.3.20"
    application
    kotlin("plugin.serialization") version "2.3.21"
    id("com.diffplug.spotless") version "8.4.0"
}

group = "de.nesski"

version = "0.0.1"

application { mainClass.set("nesski.de.ApplicationKt") }

repositories { mavenCentral() }

dependencies {
    // CLI framework
    implementation("com.github.ajalt.clikt:clikt:5.1.0")

    // TOML config parsing
    implementation("com.akuleshov7:ktoml-core:0.7.1")
    implementation("com.akuleshov7:ktoml-file:0.7.1")

    // Ktor client (retained for GraphQL API calls)
    implementation("io.ktor:ktor-client-core:$ktor_version")
    implementation("io.ktor:ktor-client-cio:$ktor_version")
    implementation("io.ktor:ktor-client-content-negotiation:$ktor_version")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktor_version")

    // ICS calendar generation (ical4j — RFC 5545 compliant)
    implementation("org.mnode.ical4j:ical4j:4.2.5")

    // Email (Simple Java Mail for SMTP with .ics attachment)
    implementation("org.simplejavamail:simple-java-mail:8.12.6")

    // Logging
    implementation("ch.qos.logback:logback-classic:$logback_version")

    // Kotlin stdlib
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlin_version")

    // Test
    testImplementation("org.jetbrains.kotlin:kotlin-test:$kotlin_version")
    testImplementation("io.ktor:ktor-client-mock:$ktor_version")
}

spotless {
    kotlin {
        // version, style and all configurations here are optional
        ktfmt("0.61").kotlinlangStyle().configure {
            it.setRemoveUnusedImports(true)
            it.setTrailingCommaManagementStrategy(KtfmtStep.TrailingCommaManagementStrategy.NONE)
        }
    }
}
