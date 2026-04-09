plugins {
	java
	id("com.vanniktech.maven.publish") version "0.29.0"
	id("jacoco")
	id("com.diffplug.spotless") version "8.4.0"
}

group = "io.github.elfrucool"
base.archivesName.set("dgraphqldsl-java")
version = "1.0.0-SNAPSHOT"

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

mavenPublishing {
	publishToMavenCentral(automaticRelease = true)
	
	pom {
		name.set("dgraphqldsl-java")
		description.set("A type-safe Java DSL for building Dgraph DQL queries")
		url.set("https://github.com/elfrucool/dgraphqldsl-java")
		
		licenses {
			license {
				name.set("MIT License")
				url.set("https://opensource.org/licenses/MIT")
			}
		}
		
		developers {
			developer {
				id.set("elfrucool")
				name.set("Gustavo Serrano")
				email.set("elfrucool@gmail.com")
			}
		}
		
		scm {
			url.set("https://github.com/elfrucool/dgraphqldsl-java")
			connection.set("scm:git:git://github.com/elfrucool/dgraphqldsl-java.git")
			developerConnection.set("scm:git:ssh://git@github.com:elfrucool/dgraphqldsl-java.git")
		}
	}
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
