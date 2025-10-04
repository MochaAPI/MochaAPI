dependencies {
    implementation(project(":mochaapi-runtime"))
    implementation(project(":mochaapi-processor"))
    implementation(project(":mochaapi-annotations"))
}

// For now, we'll create a regular JAR without shading
// TODO: Implement proper shading when shadow plugin supports Java 21
tasks.jar {
    archiveBaseName.set("mochaapi")
    archiveClassifier.set("")
    archiveVersion.set("")
    
    manifest {
        attributes(
            "Main-Class" to "com.mochaapi.runtime.MochaAPI",
            "Implementation-Title" to "MochaAPI",
            "Implementation-Version" to project.version,
            "Implementation-Vendor" to "MochaAPI Team"
        )
    }
}
