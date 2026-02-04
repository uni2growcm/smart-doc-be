## Technology Stack

### 1. Programming Language
**Choice:** Java 25

**Justification:**
Stable, LTS, widely used in enterprise environments.

---

### 2. Core Framework
**Choice:** Spring Boot 4.0.2

**Justification:**
Standard framework for fast, production-ready backend services.

---

### 3. Build & Dependency Management
**Choice:** Gradle (Kotlin DSL)

**Justification:**
Modern build tool with Kotlin DSL for better scripting and maintainability in multi-team environments.

---

### 4. API & Data Handling
**REST/Web:** Spring Web
**Serialization:** Jackson
**DTO Mapping:** MapStruct
**Boilerplate Reduction:** Lombok

**Justification:**
Default Spring solutions for REST APIs with JSON; MapStruct for type-safe DTO mapping; Lombok to reduce boilerplate code.

---

### 5. File System Management
**Choice:** Native Java NIO / Standard I/O

**Justification:**
Sufficient for handling files on a local or mounted filesystem, no DB needed.

---

### 6. Containerization
**Choice:** Docker

**Justification:**
Required for deployment in Kubernetes/OpenShift and consistent environment setup.

---

### 7. API Documentation & Design
**Choice:** OpenAPI 3.1 with OpenAPI Generator (API-first design)

**Justification:**
Enables API-first development with automatic code generation from OpenAPI specifications, ensuring consistency and reducing manual implementation.

---

### 8. Database & Migrations
**Choice:** Flyway (for schema management)

**Justification:**
Manages database schema versions with migrations, ensuring schema alignment with JPA models.

---

### 9. Testing
**Choice:** JUnit 5

**Justification:**
Modern testing framework for unit and integration tests.

---

## API-First Design

This project follows an **API-first approach** using OpenAPI 3.1 specifications. The API is defined in `src/main/openapi/openapi.yaml`, and Spring interfaces are generated automatically using OpenAPI Generator. This ensures the implementation always matches the spec.

## Code Conventions

### General Formatting
- **Indentation:** 4 spaces (defined in `.editorconfig`)
- **Line length:** 120 characters maximum
- **Line endings:** LF (Unix-style)
- **Charset:** UTF-8
- **Final newline:** Required in all files

### Java-Specific Style
#### Imports
- Use single-class imports (no wildcard imports)
- Static imports separated from regular imports
- Import order: `$*, |, *` (all classes, separator, static)

#### Naming Conventions
- **Classes:** PascalCase
- **Methods:** camelCase
- **Variables:** camelCase
- **Constants:** UPPER_SNAKE_CASE
- **Test classes:** `{ClassName}Test` or `{ClassName}Tests`

#### API Method Naming Conventions
- `find<Entity>ById(UUID id)` for single entity retrieval
- `find<Entities>(...)` for paginated lists/searches
- `create<Entity>(...)`, `update<Entity>(...)`, `patch<Entity>(...)`, `delete<Entity>(...)`

#### Type Usage
- Always declare explicit types (avoid `var`)
- Use Java 8+ types (LocalDate, LocalDateTime, Optional)

#### Annotations
- Place annotations on separate lines (except parameters)
- Use Lombok annotations: `@Data`, `@Builder`, `@Slf4j`, `@RequiredArgsConstructor`

#### Error Handling
- Use Spring Boot exception handling with `@ControllerAdvice`
- Return RFC 7807 Problem Details for API errors
- Log errors with SLF4J

### Testing Guidelines
- Use JUnit 5
- Test class naming: `{ClassUnderTest}Test`
- Use `@SpringBootTest` for integration tests, `@WebMvcTest` for controllers
- Mock dependencies with `@MockBean`

### Project Structure
- Controllers implement contract in port package.
- Business logic in services
- Data access with Spring Data JPA
- DTOs mapped with MapStruct
