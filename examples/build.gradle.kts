plugins {
    id("application")
    id("org.graalvm.buildtools.native") version "0.9.28"
}

dependencies {
    implementation(project(":mochaapi-runtime"))
    implementation(project(":mochaapi-annotations"))
    
    // Add Jackson for JSON serialization
    implementation("com.fasterxml.jackson.core:jackson-databind:2.16.0")
    
    // Logging
    implementation("org.slf4j:slf4j-api:2.0.9")
    implementation("ch.qos.logback:logback-classic:1.4.14")
}

application {
    mainClass.set("com.mochaapi.examples.DemoApp")
}
