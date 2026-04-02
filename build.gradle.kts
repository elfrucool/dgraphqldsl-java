plugins {
	java
	id("maven-publish")
	id("signing")
	id("jacoco")
	id("com.diffplug.spotless") version "8.4.0"
}

group = "io.github.elfrucool"
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

val sourcesJar by tasks.registering(Jar::class) {
	from(sourceSets.main.get().allJava)
	archiveClassifier.set("sources")
}

val javadocJar by tasks.registering(Jar::class) {
	from(sourceSets.main.get().allJava)
	archiveClassifier.set("javadoc")
}

publishing {
	publications {
		register("java", MavenPublication::class) {
			artifactId = "dgraphqldsl-java"
			artifact(tasks.named("jar"))
			artifact(sourcesJar)
			artifact(javadocJar)
			pom {
				name.set("dgraphqldsl-java")
				description.set("A type-safe Java DSL for building Dgraph DQL queries")
				url.set("https://github.com/elfrucool/dgraphqldsl-java")
				
				developers {
					developer {
						id.set("elfrucool")
						name.set("Fruition Labs")
						email.set("info@fruition-labs.com")
						organization.set("Fruition Labs")
						organizationUrl.set("https://fruition-labs.com")
					}
				}
				
				licenses {
					license {
						name.set("MIT License")
						url.set("https://opensource.org/licenses/MIT")
					}
				}
				
				scm {
					connection.set("scm:git:git@github.com:elfrucool/dgraphqldsl-java.git")
					developerConnection.set("scm:git:git@github.com:elfrucool/dgraphqldsl-java.git")
					url.set("https://github.com/elfrucool/dgraphqldsl-java")
				}
			}
		}
	}
}

signing {
	sign(publishing.publications.getByName("java"))
}

spotless {
	java {
		palantirJavaFormat()
	}
}

subprojects {
	apply(plugin = "com.diffplug.spotless")
	
	spotless {
		java {
			palantirJavaFormat()
		}
	}
}