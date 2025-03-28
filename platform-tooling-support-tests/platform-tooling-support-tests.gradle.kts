import org.gradle.api.tasks.PathSensitivity.RELATIVE
import org.gradle.jvm.toolchain.internal.NoToolchainAvailableException

plugins {
	`java-library-conventions`
	`testing-conventions`
}

javaLibrary {
	mainJavaVersion = JavaVersion.VERSION_11
}

dependencies {
	internal(platform(project(":dependencies")))

	implementation("de.sormuras:bartholdy") {
		because("manage external tool installations")
	}
	implementation("commons-io:commons-io") {
		because("moving/deleting directory trees")
	}

	testImplementation("org.assertj:assertj-core") {
		because("more assertions")
	}
	testImplementation("com.tngtech.archunit:archunit-junit5-api") {
		because("checking the architecture of JUnit 5")
	}
	testImplementation("org.codehaus.groovy:groovy") {
		because("it provides convenience methods to handle process output")
		exclude(group = "org.junit.platform", module = "junit-platform-launcher")
	}
	testImplementation("biz.aQute.bnd:biz.aQute.bndlib") {
		because("parsing OSGi metadata")
	}
	testRuntimeOnly("com.tngtech.archunit:archunit-junit5-engine") {
		because("contains the ArchUnit TestEngine implementation")
	}
	testRuntimeOnly("org.slf4j:slf4j-jdk14") {
		because("provide appropriate SLF4J binding")
	}
}

tasks.test {
	// Opt-in via system property: '-Dplatform.tooling.support.tests.enabled=true'
	enabled = System.getProperty("platform.tooling.support.tests.enabled")?.toBoolean() ?: false

	// The following if-block is necessary since Gradle will otherwise
	// always publish all mavenizedProjects even if this "test" task
	// is not executed.
	if (enabled) {
		// All maven-aware projects must be installed, i.e. published to the local repository
		val mavenizedProjects: List<Project> by rootProject
		val tempRepoName: String by rootProject

		(mavenizedProjects + project(":junit-bom"))
				.map { project -> project.tasks.named("publishAllPublicationsTo${tempRepoName.capitalize()}Repository") }
				.forEach { dependsOn(it) }
	}

	val tempRepoDir: File by rootProject
	jvmArgumentProviders += MavenRepo(tempRepoDir)

	// Pass version constants (declared in Versions.kt) to tests as system properties
	systemProperty("Versions.apiGuardian", versions.apiguardian)
	systemProperty("Versions.assertJ", versions.assertj)
	systemProperty("Versions.junit4", versions.junit4)
	systemProperty("Versions.ota4j", versions.opentest4j)

	(options as JUnitPlatformOptions).apply {
		includeEngines("archunit")
	}

	inputs.apply {
		dir("projects").withPathSensitivity(RELATIVE)
		file("${rootDir}/gradle.properties")
		file("${rootDir}/settings.gradle.kts")
		file("${rootDir}/gradlew")
		file("${rootDir}/gradlew.bat")
		dir("${rootDir}/gradle/wrapper").withPathSensitivity(RELATIVE)
		dir("${rootDir}/documentation/src/main").withPathSensitivity(RELATIVE)
		dir("${rootDir}/documentation/src/test").withPathSensitivity(RELATIVE)
	}

	distribution {
		requirements.add("jdk=8")
	}
	jvmArgumentProviders += JavaHomeDir(project, 8)

	maxParallelForks = 1 // Bartholdy.install is not parallel safe, see https://github.com/sormuras/bartholdy/issues/4
}

class MavenRepo(@get:InputDirectory @get:PathSensitive(RELATIVE) val repoDir: File) : CommandLineArgumentProvider {
	override fun asArguments() = listOf("-Dmaven.repo=$repoDir")
}

class JavaHomeDir(project: Project, @Input val version: Int) : CommandLineArgumentProvider {
	@Internal
	val passToolchain = project.providers.gradleProperty("enableTestDistribution").map(String::toBoolean).orElse(false).map { !it }

	@Internal
	val javaLauncher: Property<JavaLauncher> = project.objects.property<JavaLauncher>()
			.value(project.provider {
				try {
					project.the<JavaToolchainService>().launcherFor {
						languageVersion.set(JavaLanguageVersion.of(version))
					}.get()
				} catch (e: NoToolchainAvailableException) {
					null
				}
			})

	override fun asArguments(): List<String> {
		if (passToolchain.get()) {
			val metadata = javaLauncher.map { it.metadata }
			val javaHome = metadata.map { it.installationPath.asFile.absolutePath }.orNull
			return javaHome?.let { listOf("-Djava.home.$version=$it") } ?: emptyList()
		}
		return emptyList()
	}
}
