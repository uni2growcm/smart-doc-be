
# Runtime Plugin Architecture with Spring Boot (Core + Plugin)

This document explains how to build a **Spring Boot application (Core)** that can **load plugins at runtime**.  
Plugins are delivered as **JAR files**, can expose **REST endpoints**, and are **isolated** from the Core while still being able to reuse Core services.

The solution is based on:

- Spring Boot 3.x
- PF4J (Plugin Framework for Java)
- Spring Boot Plugin (SBP – Laxture)

---

## 1. Architecture Overview

```

Client
|
v
Core Application (Spring Boot)

* Plugin Manager
* Security / Permissions
* Shared Services
  |
  v
  Plugin JAR 
* REST Controllers
* Plugin-specific Services

````

### Key principles

- The **Core**:
  - does NOT know which plugins exist
  - manages plugin lifecycle (load, start, stop)
- The **Plugin**:
  - attaches itself to the Core at runtime
  - can expose REST endpoints
  - runs in its own Spring context

---

## 2. Core Application

The Core is a **standard Spring Boot application** with plugin support enabled.

---

### 2.1 Core Maven Configuration

Relevant dependencies only:

```xml
<dependency>
    <groupId>org.laxture</groupId>
    <artifactId>sbp-spring-boot-starter</artifactId>
    <version>3.5.27</version>
</dependency>

<dependency>
    <groupId>org.laxture</groupId>
    <artifactId>sbp-adapter-3</artifactId>
    <version>3.5.27</version>
</dependency>
````

#### Why these dependencies matter

* `sbp-spring-boot-starter` integrates PF4J into Spring Boot
* `sbp-adapter-3` enables compatibility with Spring Boot 3.x

---

### 2.2 Core Configuration (`application.properties`)

```properties
spring.sbp.enabled=true
spring.sbp.plugins-root=plugins
spring.sbp.web-mvc.enabled=true

spring.web.resources.add-mappings=false
spring.sbp.web-mvc.resource-resolver-enabled=false
```

#### Explanation

* `spring.sbp.enabled=true`
  Enables the plugin system

* `spring.sbp.plugins-root=plugins`
  Directory where plugin JARs are placed

* `spring.sbp.web-mvc.enabled=true`
  Allows plugins to register REST controllers dynamically

---

### 2.3 Plugin Administration API (Core)

The Core exposes an endpoint to **inspect loaded plugins**.

```java
@RestController
@RequestMapping("/api/admin/plugins")
public class PluginAdminController {

    @Autowired
    private PluginManager pluginManager;

    @GetMapping("/status")
    public List<Map<String, String>> getPluginsStatus() {
        return pluginManager.getPlugins().stream().map(wrapper -> {
            Map<String, String> info = new HashMap<>();
            info.put("id", wrapper.getPluginId());
            info.put("status", wrapper.getPluginState().toString());
            info.put("version", wrapper.getDescriptor().getVersion());
            return info;
        }).toList();
    }
}
```

#### What this does

* Uses PF4J’s `PluginManager`
* Lists:

    * plugin id
    * plugin version
    * runtime state (CREATED, STARTED, STOPPED)

This confirms that plugins are **loaded and running at runtime**.

---

## 3. Plugin Project

A plugin is **not** a Spring Boot application.
It is a **plain JAR** loaded by the Core.

---

### 3.1 Plugin Maven Configuration

Relevant dependencies only:

```xml
<dependency>
    <groupId>org.laxture</groupId>
    <artifactId>sbp-core</artifactId>
    <version>3.5.27</version>
    <scope>provided</scope>
</dependency>

<dependency>
    <groupId>org.pf4j</groupId>
    <artifactId>pf4j</artifactId>
    <version>3.6.0</version>
    <scope>provided</scope>
</dependency>
```

#### Why `provided` scope is mandatory

* These libraries are already present in the Core
* Prevents classloader duplication and conflicts

---

### 3.2 Plugin JAR Manifest Metadata

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-jar-plugin</artifactId>
    <configuration>
        <archive>
            <manifestEntries>
                <Plugin-Id>example-plugin</Plugin-Id>
                <Plugin-Version>1.0.0</Plugin-Version>
                <Plugin-Class>com.example.plugin.ExamplePlugin</Plugin-Class>
            </manifestEntries>
        </archive>
    </configuration>
</plugin>
```

#### Mandatory fields

* `Plugin-Id` → unique identifier
* `Plugin-Version` → plugin version
* `Plugin-Class` → entry point class

Without this metadata, the plugin **will not load**.

---

## 4. Plugin Entry Point

Each plugin must extend `SpringBootPlugin`.

```java
public class ExamplePlugin extends SpringBootPlugin {

    public ExamplePlugin(PluginWrapper wrapper) {
        super(wrapper);
    }

    @Override
    protected SpringBootstrap createSpringBootstrap() {
        return new SpringBootstrap(this, ExamplePluginConfig.class);
    }
}
```

#### What this does

* Creates a **dedicated Spring ApplicationContext**
* Keeps plugin beans isolated from the Core
* Still allows dependency injection from Core beans

---

## 5. Plugin Spring Configuration

```java
@Configuration
@ComponentScan(basePackages = "com.example.plugin")
public class ExamplePluginConfig {
    // Entry point for scanning plugin beans
}
```

This tells SBP where to find:

* `@RestController`
* `@Service`
* `@Component`

---

## 6. Plugin REST Controller

```java
@RestController
@RequestMapping("/api/plugin")
public class ExampleController {

    @GetMapping("/test")
    public Map<String, String> test() {
        return Map.of(
            "status", "success",
            "message", "Hello from Plugin!"
        );
    }
}
```

Once the plugin JAR is placed in the `plugins/` directory and started:

* The endpoint is **immediately available**
* No Core restart is required

### Runtime Structure

Before starting the Core application, the file system looks like this:

root/
├── core-app.jar
└── plugins/
├── example-plugin.jar
├── another-plugin.jar
