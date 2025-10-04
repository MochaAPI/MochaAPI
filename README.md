# MochaAPI

[![CI/CD Pipeline](https://github.com/mochaapi/mochaapi/workflows/CI/CD%20Pipeline/badge.svg)](https://github.com/mochaapi/mochaapi/actions)
[![Maven Central](https://img.shields.io/maven-central/v/com.mochaapi/mochaapi.svg)](https://search.maven.org/artifact/com.mochaapi/mochaapi)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

**MochaAPI** is a blazing-fast REST framework for Java 21+ with Go-like performance, low memory footprint, and instant startup. Built with virtual threads, Netty, and GraalVM Native Image support.

## 🚀 Features

- **Blazing Fast**: Go-like performance with minimal overhead
- **Virtual Threads**: Built on JDK 21 Loom for massive concurrency
- **Native Image**: Full GraalVM Native Image support for instant startup
- **Spring Boot-like**: Familiar annotations with zero learning curve
- **CPU-Bound Tasks**: Dedicated executor for compute-intensive operations
- **OpenAPI**: Automatic OpenAPI 3.0 specification generation
- **Metrics**: Built-in Prometheus metrics support
- **Zero Reflection**: Compile-time code generation for maximum performance

## 📦 Installation

Add MochaAPI to your `build.gradle.kts`:

```kotlin
dependencies {
    implementation("com.mochaapi:mochaapi:1.0.0")
}
```

Or for Maven:

```xml
<dependency>
    <groupId>com.mochaapi</groupId>
    <artifactId>mochaapi</artifactId>
    <version>1.0.0</version>
</dependency>
```

## 🎯 Quick Start

Create a simple REST controller:

```java
@RestController
public class UserController {
    
    @GetMapping("/users/{id}")
    public User getUser(@PathVariable String id) {
        return userService.find(id);
    }
    
    @PostMapping("/users")
    public User createUser(@RequestBody User user) {
        return userService.save(user);
    }
    
    @GetMapping("/calc")
    @CpuBound
    public CalcResult calculate(@RequestParam int a, @RequestParam int b) {
        return heavyService.compute(a, b);
    }
}
```

Bootstrap your application:

```java
public class DemoApp {
    public static void main(String[] args) {
        MochaAPI.run(DemoApp.class, args);
    }
}
```

## 🏗️ Architecture

MochaAPI is built as a multi-module Gradle project:

- **`mochaapi-annotations`**: Core annotations (`@RestController`, `@GetMapping`, etc.)
- **`mochaapi-processor`**: Annotation processor for compile-time code generation
- **`mochaapi-runtime`**: Runtime core with Netty server and virtual threads
- **`mochaapi-shaded`**: Final shaded JAR with all dependencies
- **`mochaapi-gradle-plugin`**: Gradle plugin for native image generation

## ⚡ Performance

MochaAPI is designed for maximum performance:

- **Virtual Threads**: Handle millions of concurrent connections
- **CPU-Bound Executor**: Dedicated work-stealing pool for compute tasks
- **Zero Reflection**: All serialization and routing generated at compile-time
- **Native Image**: Sub-millisecond startup times
- **Memory Efficient**: Minimal memory footprint

## 🔧 Configuration

Configure your application with `MochaAPIConfig`:

```java
MochaAPIConfig config = new MochaAPIConfig();
config.setPort(8080);
config.setHost("0.0.0.0");
config.setCpuBoundThreads(8);
config.setEnableMetrics(true);
config.setEnableOpenApi(true);

MochaAPI.run(MyApp.class, config, args);
```

## 📊 Monitoring

Built-in metrics and health checks:

```java
@GetMapping("/health")
public HealthStatus health() {
    return new HealthStatus("UP", System.currentTimeMillis());
}
```

Access metrics at `/actuator/prometheus` and OpenAPI docs at `/docs`.

## 🐳 Native Image

Build native images with the Gradle plugin:

```kotlin
plugins {
    id("com.mochaapi.gradle")
}

tasks.nativeImage {
    // Configure native image build
}
```

Or use the command line:

```bash
./gradlew nativeImage
```

## 🧪 Testing

Run the test suite:

```bash
./gradlew test
./gradlew integrationTest
```

## 📚 Documentation

- [Getting Started Guide](docs/getting-started.md)
- [API Reference](docs/api-reference.md)
- [Native Image Guide](docs/native-image.md)
- [Performance Tuning](docs/performance.md)

## 🤝 Contributing

We welcome contributions! Please see our [Contributing Guide](CONTRIBUTING.md) for details.

## 📄 License

MochaAPI is licensed under the [Apache License 2.0](LICENSE).

## 🙏 Acknowledgments

- Built on [Netty](https://netty.io/) for high-performance networking
- Inspired by [Spring Boot](https://spring.io/projects/spring-boot) for developer experience
- Powered by [GraalVM](https://www.graalvm.org/) for native compilation
- Uses [Micrometer](https://micrometer.io/) for metrics

---

**MochaAPI** - Blazing-fast REST framework for the modern Java ecosystem.
