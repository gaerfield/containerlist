import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.springframework.boot.gradle.tasks.bundling.BootJar

buildscript {
    repositories {
        mavenCentral()
    }
}

plugins {
    `java-library`

    id("org.springframework.boot") version "2.1.3.RELEASE"
    id("io.spring.dependency-management") version "1.0.6.RELEASE"

    val kotlinVersion = "1.3.21"
    id("org.jetbrains.kotlin.jvm") version kotlinVersion
    id("org.jetbrains.kotlin.plugin.spring") version kotlinVersion
    id("org.jetbrains.kotlin.kapt") version kotlinVersion
}

group = "de.kramhal"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("reflect"))
    implementation("io.github.microutils:kotlin-logging:1.5.4")

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-devtools")
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    implementation("nz.net.ultraq.thymeleaf:thymeleaf-layout-dialect:2.3.0")
    // implementation("org.springframework.boot:spring-boot-starter-security:2.0.3.RELEASE")
    //implementation("org.springframework:spring-web:5.0.6.RELEASE")

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    // Docker-Client-Dependencies
    implementation("com.spotify:docker-client:8.15.1")
    implementation("org.glassfish.jersey.inject:jersey-hk2:2.26")
    implementation("org.glassfish.jersey.bundles.repackaged:jersey-guava:2.25.1")

    implementation("org.webjars:webjars-locator:0.34")
    implementation("org.webjars:bootstrap:4.1.3")
    implementation("org.webjars:bootstrap-glyphicons:bdd2cbfba0")

    // unit-tests
//    testImplementation("org.springframework.boot:spring-boot-starter-test:2.0.3.RELEASE") {
//        exclude(module = "junit")
//    }
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testImplementation("org.junit.jupiter:junit-jupiter-params")
    testImplementation("io.mockk:mockk:1.8.5")
    testRuntime("org.junit.jupiter:junit-jupiter-engine")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

springBoot {
    mainClassName = "de.kramhal.containerlist.ContainerListKt"
}

tasks {
    getByName<BootJar>("bootJar") {
        launchScript {
            properties["inlinedConfScript"] = "src/main/resources/javaOpts.conf"
        }
    }
    getByName<Test>("test") {
        useJUnitPlatform()
    }
//    getByName<JavaExec>("run") {
//        standardInput = System.`in`
//    }
}
