import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask

//Plugins are basically extensions to Gradle's functionality which enable you to perform common build tasks in an easier way
plugins {
    java
    idea
    eclipse
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    id("com.github.ben-manes.versions")
    id("com.diffplug.spotless")
}

//Specify Java Version
java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

//Specify Dependency Management
repositories {
    mavenCentral()
}

/*
    SourceSet is a collection of java source files and additional resource files
    that are compiled and assembled together to be executed
*/
sourceSets {
    create("functionalTest") {
        java {
            compileClasspath += sourceSets.main.get().output + sourceSets.test.get().output
            runtimeClasspath += sourceSets.main.get().output + sourceSets.test.get().output
            srcDir("src/functional-test/java")
        }
    }
}

idea {
    module {
        testSources.from(sourceSets["functionalTest"].java.srcDirs)
    }
}

val functionalTestImplementation: Configuration by configurations.getting {
    extendsFrom(configurations.implementation.get())
}
val functionalTestRuntimeOnly: Configuration by configurations.getting

configurations {
    configurations["functionalTestImplementation"].extendsFrom(configurations.testImplementation.get())
    configurations["functionalTestRuntimeOnly"].extendsFrom(configurations.testRuntimeOnly.get())
}


val functionalTest = task<Test>("functionalTest") {
    description = "Runs functional tests."
    group = "verification"

    testClassesDirs = sourceSets["functionalTest"].output.classesDirs
    classpath = sourceSets["functionalTest"].runtimeClasspath
    shouldRunAfter("test")

    useJUnitPlatform()

    testLogging {
        events ("failed", "passed", "skipped", "standard_out")
    }
}

/*
    Dependencies are external modules that your project compile, run, and test the code.
    Instead of coding some functionality from scratch, dependencies allow your application to use functionality
    provided by other software packages
*/
dependencies {
    /* Spring Boot */
    implementation ("org.springframework.boot:spring-boot-starter-web")
    //Dependency to enable Swagger UI
    implementation ("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.2.0")
    //Dependency to validate requests
    implementation("org.springframework.boot:spring-boot-starter-validation")
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude (group = "org.junit.vintage", module = "junit-vintage-engine")
    }
}

/*
    Tasks represent a specific action which Gradle executes during the build process,
    such as compilation of code, running testcases, packaging the app, and so on.
*/
tasks.named<Test>("test") {
    useJUnitPlatform()

    testLogging {
        events ("failed", "passed", "skipped", "standard_out")
    }
}

tasks.check { dependsOn(functionalTest) }

fun isNonStable(version: String): Boolean {
    val stableKeyword = listOf("RELEASE", "FINAL", "GA").any { version.uppercase().contains(it) }
    val regex = "^[0-9,.v-]+(-r)?$".toRegex()
    val isStable = stableKeyword || regex.matches(version)
    return isStable.not()
}

tasks.withType<DependencyUpdatesTask> {
    rejectVersionIf {
        isNonStable(candidate.version)
    }
    gradleReleaseChannel="current"
}

//Spotless is a general-purpose formatting plugin
spotless {
    java {
        palantirJavaFormat()
        formatAnnotations()
    }
}