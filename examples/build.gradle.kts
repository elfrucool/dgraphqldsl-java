plugins {
    java
    id("org.springframework.boot") version "4.0.4"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "org.frunix"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
    mavenLocal()
}

dependencies {
    implementation(project(":"))
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("io.dgraph:dgraph4j:24.2.0")
    implementation("io.grpc:grpc-api:1.71.0")
    implementation("io.grpc:grpc-stub:1.71.0")
    implementation("com.google.protobuf:protobuf-java:4.30.2")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.18.2")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}
