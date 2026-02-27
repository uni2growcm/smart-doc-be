# OH API – Core + Plugin Architecture

## 1. Analysis and Problem Statement

The goal of this solution is to design a **modular backend architecture** where:

* A **single Core application (oh-api)** acts as the main entry point
* Multiple **Plugin applications** can be added, removed, or extended independently
* All components run on the **same server**, but on **different ports**
* External clients interact **only with the Core**, never directly with plugins

Key challenges addressed:

* How to expose many APIs without coupling Core to plugin implementations
* How to support **multiple plugins**, each with **multiple endpoints**
* How to avoid starting and configuring routing logic multiple times
* How to keep the system simple, observable, and production-ready

---

## 2. General Idea

The architecture is based on a **configuration-driven reverse proxy** implemented inside the Core application.

### Core Responsibilities

* Expose public APIs to external clients
* Load routing rules from a configuration file
* Forward incoming HTTP requests to the correct plugin
* Return plugin responses transparently to the client

### Plugin Responsibilities

* Expose domain-specific APIs
* Contain business logic
* Remain completely unaware of the Core

> The Core does **not** know plugin controllers, DTOs, or services.
> It only knows **routes**.

---

## 3. High-Level Architecture

```
Client
   |
   v
+--------------------+
|  OH API (Core)     |
|  Reverse Proxy     |
|  Port 8080         |
+--------------------+
   |        |
   v        v
Plugin A   Plugin B
8081       8082
```

* Clients call `http://server:8080/api/plugin/plugin-a...`
* Core decides where to forward the request
* Plugins respond
* Core returns the response

---

## 4. Routing Strategy

Routing is derived from **plugin manifests** and exposed centrally by the Core.

Routing is **prefix-based** and defined in a single configuration file.

### Two-level routing model

The system implements **two distinct routing layers**, each with a clearly defined responsibility:

1. **Core-level routing**
    - Implemented inside the OH API Core
    - Based on plugin manifests (`manifest.yml`)
    - Resolves *which plugin* must handle an incoming request
    - Forwards the request to the correct plugin instance

2. **Plugin-level routing**
    - Implemented inside each plugin application
    - Uses standard Spring MVC / WebFlux controllers
    - Resolves *which controller and endpoint* inside the plugin must handle the request

> The routing logic described in this document refers **only to the Core-level routing**.
> Plugin-level routing is entirely owned by the plugin and follows standard Spring conventions.

---

### Routes generation from manifests

Routing is **not hard-coded** and is **not managed via a separate routes configuration file**. Instead, the Core derives routing rules **directly from plugin manifests** (`manifest.yml`).

The manifest now represents the **single source of truth** for:

* Plugin identity
* Network configuration
* Security requirements

> Authorization is evaluated once at the Core level.
> If access is denied, the request is rejected before any network call is made.

### Manifest-driven routing model

Each plugin declares:

* its logical name
* the local port it listens on
* the permissions required to access its APIs

The Core derives the public API prefix using a deterministic convention:

```
/api/plugin/{plugin-name}/**
```

The **plugin manifest** is the single source of truth for plugin discovery, routing, and authorization.

### Manifest structure

```yaml
- name: "smart-doc"
  port: 4001
  permissions:
    - role: "admin"
      privileges:
        - "document.read"
        - "document.write"
        - "document.delete"
        - "document.update"
```

### Fields description

* **name**: Logical identifier of the plugin. It also defines the public API prefix.
* **port**: Local port where the plugin HTTP server is exposed.
* **permissions**: Access control rules evaluated by the Core.

    * **role**: Required user role.
    * **privileges**: Fine-grained permissions required to access the plugin APIs.

### Core responsibilities based on the manifest

Using the manifest, the Core:

* Discovers which plugins are available
* Builds routing rules dynamically
* Exposes public APIs under `/api/plugin/{plugin-name}/**`
* Enforces authorization *before* forwarding requests
* Centralizes governance and security policies

> Plugins do not implement authorization logic themselves. The Core acts as the policy enforcement point.

### Derived route (implicit)

From the manifest above, the Core automatically derives the following routing rule:

```
/api/plugin/smart-doc/**  →  http://localhost:4001/**
```

### How routing works at runtime

1. Core loads all plugin manifests at startup
2. For each plugin, Core:

    * registers `/api/plugin/{plugin-name}` as public base path
    * maps it to `http://localhost:{port}`
3. Incoming requests are matched on the base path
4. The base path is stripped and the remaining path is appended to the plugin URL
5. The request is forwarded preserving:

    * HTTP method
    * Headers
    * Body
    * Query parameters

Example:

```
Incoming request:
GET /api/plugin/smart-doc/documents/123

Resolved target:
http://localhost:4001/documents/123
```

This allows **one manifest entry to expose multiple APIs** inside a plugin.

---

## 5. Implementation Details

### 5.1 Manifest Loading and Plugin Registry

Routes are **not configured explicitly**. The Core loads plugin definitions from a `manifest.yml` file and builds routing information dynamically.

```java
@ConfigurationProperties(prefix = "plugins")
public class PluginRegistry {

    private List<PluginDefinition> plugins = new ArrayList<>();

    public List<PluginDefinition> getPlugins() {
        return plugins;
    }

    public void setPlugins(List<PluginDefinition> plugins) {
        this.plugins = plugins;
    }
}
```

Each `PluginDefinition` contains:

* `name`: plugin identifier
* `port`: local HTTP port exposed by the plugin
* `permissions`: authorization rules (roles and privileges)

From this information, the Core checks the permissions and implicitly derives:

```
/api/plugin/{plugin-name}/**  →  http://localhost:{port}/**
```

---

### 5.2 Proxy Controller (Manifest-driven)

The Core exposes a **single catch-all controller** that resolves plugins using the manifest registry:

```java
@RestController
public class ProxyController {

    @Value("${proxy.api-prefix:/api/plugin/}")
    private String apiPrefix;

    @Value("${proxy.target-host:http://localhost:}")
    private String targetHost;

    private final PluginRegistry pluginRegistry;
    private final RestTemplate restTemplate;

    public ProxyController(PluginRegistry pluginRegistry, RestTemplate restTemplate) {
        this.pluginRegistry = pluginRegistry;
        this.restTemplate = restTemplate;
    }

    @RequestMapping("${proxy.api-prefix:/api/plugin/}**")
    public ResponseEntity<?> proxy(HttpServletRequest request,
                                   @RequestBody(required = false) byte[] body) {

        String path = request.getRequestURI();
        // es: /api/plugin/smart-doc/documents/123

        // Remove /api/plugin/
        String relativePath = path.substring(apiPrefix.length());
        // es: smart-doc/documents/123

        // Extract plugin name
        String pluginName = relativePath.split("/", 2)[0];

        PluginDefinition plugin = pluginRegistry.getPlugins().stream()
                .filter(p -> p.getName().equals(pluginName))
                .findFirst()
                .orElseThrow(() ->
                        new RuntimeException("No plugin registered for: " + pluginName)
                );

        String targetUrl = path.replace(
                apiPrefix + plugin.getName(),
                targetHost + plugin.getPort()
        );

        HttpMethod method = HttpMethod.valueOf(request.getMethod());
        HttpEntity<byte[]> entity = new HttpEntity<>(body);

        return restTemplate.exchange(targetUrl, method, entity, byte[].class);
    }
}
```

This controller:

* Matches all `/api/plugin/**` requests
* Resolves the target plugin using the manifest
* Builds the target URL dynamically
* Forwards the request transparently

> In a production-ready implementation, the Core must also forward:
> - HTTP headers (Authorization, Correlation-Id, etc.)
> - Query parameters
> - Response headers and status codes

---

## 6. Deployment on a Server

### Suggested directory layout

```
/opt/oh-system/
├── core/
│   └── oh-api.jar
├── plugins/
│   ├── customer.jar
│   └── test.jar
```

---

### Startup options

#### Option A – Shell script

```bash
java -jar plugins/customer.jar &
java -jar plugins/test.jar &
java -jar core/oh-api.jar
```

#### Option B – systemd (production)

* One service for Core
* One service per plugin
* Automatic restart and startup at boot

#### Option C – Docker Compose

* One container for Core
* One container per plugin
* Single command startup
* Clear isolation and scalability

---

## 7. Benefits of This Approach

* Loose coupling between Core and plugins
* Easy to add or remove plugins
* Single entry point for clients
* No API duplication
* Clear ownership of responsibilities
* Minimal runtime complexity

---

## 8. Future Improvements

* Health checks for plugins
* Route validation at startup
* Authentication and authorization at Core level
* Rate limiting
* Metrics and tracing
