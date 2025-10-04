dependencies {
    implementation(project(":mochaapi-annotations"))
    implementation("com.squareup:javapoet:1.13.0")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.16.0")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.16.0")
    
    compileOnly("com.google.auto.service:auto-service:1.1.1")
    annotationProcessor("com.google.auto.service:auto-service:1.1.1")
}

tasks.jar {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}
