plugins {
    id("java-gradle-plugin")
    id("com.gradle.plugin-publish") version "1.2.1"
}

dependencies {
    implementation("org.graalvm.buildtools:native-gradle-plugin:0.9.28")
}

gradlePlugin {
    plugins {
        create("mochaapi") {
            id = "com.mochaapi.gradle"
            implementationClass = "com.mochaapi.gradle.MochaAPIPlugin"
            displayName = "MochaAPI Gradle Plugin"
            description = "Gradle plugin for MochaAPI native image generation"
        }
    }
}
