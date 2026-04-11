plugins {
    java
    application
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.github.elfrucool:dgraphqldsl-java:1.0.0")
    implementation("io.dgraph:dgraph4j:24.2.0")
    implementation("com.google.protobuf:protobuf-java:4.30.2")
    implementation("io.grpc:grpc-core:1.71.0")
    implementation("io.grpc:grpc-stub:1.71.0")
}

application {
    mainClass = "tutorial.Tutorial"
}