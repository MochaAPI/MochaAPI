dependencies {
    implementation(project(":mochaapi-annotations"))
    implementation("io.netty:netty-all:4.1.104.Final")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.16.0")
    implementation("io.micrometer:micrometer-core:1.12.0")
    implementation("io.micrometer:micrometer-registry-prometheus:1.12.0")
    
    // Logging
    implementation("org.slf4j:slf4j-api:2.0.9")
    implementation("ch.qos.logback:logback-classic:1.4.14")
    
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.mockito:mockito-core:5.8.0")
}
