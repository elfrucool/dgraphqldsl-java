plugins {
	java
	id("maven-publish")
}

group = "com.github.elfrucool"
base.archivesName.set("dgraphqldsl-java")
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

repositories {
	mavenCentral()
}

dependencies {
	testImplementation("org.junit.jupiter:junit-jupiter:5.11.4")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
	useJUnitPlatform()
	testLogging {
		events("passed", "skipped", "failed")
		exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
	}
}

tasks.register<Jar>("sourcesJar") {
	from(sourceSets.main.get().allJava)
	archiveClassifier.set("sources")
}

publishing {
	publications {
		register("java", MavenPublication::class) {
			artifactId = "dgraphqldsl-java"
			artifact(tasks.named("jar"))
			artifact(tasks.named("sourcesJar"))
			pom {
				name.set("dgraphqldsl-java")
				description.set("A type-safe Java DSL for building Dgraph DQL queries")
				url.set("https://github.com/elfrucool/dgraphqldsl-java")
			}
		}
	}
}