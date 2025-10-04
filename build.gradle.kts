plugins {
    id("java")
    id("maven-publish")
    id("signing")
    id("com.gradle.plugin-publish") version "1.2.1"
}

group = "com.mochaapi"
version = "1.0.0-SNAPSHOT"

repositories {
    mavenCentral()
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "maven-publish")
    apply(plugin = "signing")
    
    group = rootProject.group
    version = rootProject.version
    
    repositories {
        mavenCentral()
    }
    
    java {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
        withSourcesJar()
        withJavadocJar()
    }
    
    dependencies {
        testImplementation(platform("org.junit:junit-bom:5.10.0"))
        testImplementation("org.junit.jupiter:junit-jupiter")
        testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    }
    
    tasks.test {
        useJUnitPlatform()
    }
    
    publishing {
        publications {
            create<MavenPublication>("maven") {
                from(components["java"])
                
                pom {
                    name.set(project.name)
                    description.set("MochaAPI - Blazing-fast REST framework for Java")
                    url.set("https://github.com/mochaapi/mochaapi")
                    
                    licenses {
                        license {
                            name.set("The Apache License, Version 2.0")
                            url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                        }
                    }
                    
                    developers {
                        developer {
                            id.set("mochaapi")
                            name.set("MochaAPI Team")
                            email.set("team@mochaapi.com")
                        }
                    }
                    
                    scm {
                        connection.set("scm:git:git://github.com/mochaapi/mochaapi.git")
                        developerConnection.set("scm:git:ssh://github.com:mochaapi/mochaapi.git")
                        url.set("https://github.com/mochaapi/mochaapi")
                    }
                }
            }
        }
    }
}