# AGENTS.md - SmartDoc Backend Development Guide

This file provides essential information for AI coding agents working in the SmartDoc Backend (SD-BE) repository.

## Project Overview

**SmartDoc Backend** is a Spring Boot REST API for managing digital patient documents within the Open Hospital ecosystem. It operates as a microservice with OpenAPI-first design, proxied through OH-API/BE for authentication and authorization.

**Technology Stack:**
- Java 25
- Spring Boot 4.0.2
- Gradle (Kotlin DSL)
- OpenAPI 3.1 (API-first design)
- Lombok (boilerplate reduction)
- JUnit 5 (testing)
- MySQL database with Flyway migrations

## Build, Test, and Lint Commands

### Essential Gradle Commands

```bash
# Build the project (compile + test)
./gradlew build

# Build without tests
./gradlew build -x test

# Run all tests
./gradlew test

# Run a single test class
./gradlew test --tests "org.openhospital.smartdoc.PersonControllerTest"

# Run a specific test method
./gradlew test --tests "org.openhospital.smartdoc.PersonControllerTest.shouldThrowExceptionForDuplicatePid"

# Clean build artifacts
./gradlew clean

# Run the application locally
./gradlew bootRun

# Generate OpenAPI code (runs automatically before compile)
./gradlew openApiGenerate

# Lint OpenAPI spec
./gradlew lint

# Bundle OpenAPI spec
./gradlew buildSpec

# Preview OpenAPI docs (on port 8086)
./gradlew preview

# Run all checks (tests + validations)
./gradlew check

# Create JAR
./gradlew bootJar
```

### Running Tests in CI/CD

The GitHub Actions pipeline uses Maven for compatibility:
```bash
mvn -B clean package -DskipTests  # Build
mvn test                          # Run tests
```

## Code Style Guidelines

### General Formatting

- **Indentation:** 4 spaces
- **Line length:** 120 characters maximum
- **Line endings:** LF (Unix-style)
- **Charset:** UTF-8
- **Final newline:** Required in all files
- **Braces:** End-of-line style (K&R style)
- **Force braces:** Always use braces for if/for/while/do blocks

### Java-Specific Style

#### Imports
- Use single-class imports (no wildcard imports)
- Static imports separated from regular imports
- Import order: all classes, separator, static imports
- Max classes before import-on-demand: 999 (essentially never)
- Always use fully qualified names where clarity is needed

#### Naming Conventions
- **Classes:** PascalCase (e.g., `SmartdocApplication`, `SecurityConfig`)
- **Methods:** camelCase (e.g., `securityFilterChain`, `contextLoads`)
- **Variables:** camelCase
- **Constants:** UPPER_SNAKE_CASE
- **Test classes:** `{ClassName}Test` or `{ClassName}Tests`
- **Test methods:** `shouldThrowExceptionFor{Scenario}` for exceptions, `should{Action}{Condition}` otherwise
- **Packages:** lowercase, dot-separated (e.g., `org.openhospital.smartdoc`)

#### API Method Naming Conventions
- **Single entity retrieval:** `find<Entity>ById(UUID id)` (e.g., `findPersonById`, `findDocumentById`)
- **Paginated lists and searches:** `find<Entities>(...)` (e.g., `findPersons`, `findDocuments`)
- **Creation:** `create<Entity>(...)`
- **Full updates:** `update<Entity>(...)`
- **Partial updates:** `patch<Entity>(...)`
- **Deletion:** `delete<Entity>(...)`

#### Type Usage
- Always declare explicit types (avoid `var` for clarity)
- Use Java 8+ types (LocalDate, LocalDateTime, Optional, etc.)
- Leverage Lombok annotations to reduce boilerplate:
  - `@Data` for POJOs with getters/setters/equals/hashCode
  - `@Builder` for builder pattern
  - `@Slf4j` for logging
  - `@RequiredArgsConstructor` for constructor injection
  - `@AllArgsConstructor` when all constructor parameters needed
  - `@NoArgsConstructor` for JPA entities

#### Annotations
- Place annotations on separate lines (except parameters)
- Spring annotations:
  - `@SpringBootApplication` for main class
  - `@Configuration` for config classes
  - `@Bean` for bean definitions
  - `@Service`, `@Repository`, `@RestController` for layer separation
  - Always use constructor injection (prefer `@RequiredArgsConstructor` with `final` fields)

#### Error Handling
- Use Spring Boot exception handling with `@ControllerAdvice`
- Return RFC 7807 Problem Details for API errors (see `Problem.yaml` schema)
- Log errors appropriately with SLF4J
- Use specific exception types (avoid generic `Exception`)
- Document exceptions in JavaDoc with `@throws`

#### i18n Error Key Conventions
- Follow the pattern `[domain].errors.[specific-description]` for all exception messages
- **Domains:** `uploads`, `documents`, `persons`
- **Examples:**
  - `uploads.errors.file-too-large` - File size exceeds limit
  - `documents.errors.type-not-found` - Document type doesn't exist
  - `persons.errors.pid-already-exists` - Person PID duplicate
- **Message Style:** User-friendly, actionable error messages in both English and French
- **Location:** `src/main/resources/locales/messages_{en|fr}.properties`

#### Comments and Documentation
- Use JavaDoc for public APIs and complex logic
- Align JavaDoc parameter and exception comments
- Add blank line after description in JavaDoc
- Use single-line comments (`//`) sparingly for complex logic
- Avoid obvious comments (code should be self-documenting)

### Spring Boot Conventions

- Use `application.properties` for configuration
- Follow Spring Boot naming: `spring.application.name=smartdoc`
- Configure security with `SecurityFilterChain` bean
- Use stateless session management (JWT-ready)
- Actuator endpoints for health checks and monitoring

### Testing Guidelines

- Use JUnit 5 (`@Test`, not JUnit 4)
- Test class naming: `{ClassUnderTest}Test` or `{ClassUnderTest}Tests`
- Use `@SpringBootTest` for integration tests
- Use `@WebMvcTest` for controller tests
- Mock external dependencies with `@MockBean`
- Organize tests with nested test classes when appropriate
- Use descriptive test method names (e.g., `shouldReturnDocumentWhenIdExists`, `shouldThrowExceptionForInvalidInput`)
- Use `@ActiveProfiles("test")` for test-specific configuration

## OpenAPI-First Development

This project uses **OpenAPI Generator** to generate Spring interfaces from OpenAPI specs.

### OpenAPI Workflow

1. Define API in `src/main/openapi/openapi.yaml`
2. Create modular paths in `src/main/openapi/paths/`
3. Define schemas in `src/main/openapi/components/schemas/`
4. Run `./gradlew openApiGenerate` (or let compile task run it)
5. Implement generated interfaces in your `@RestController` classes

### OpenAPI Configuration

- Generator: `spring`
- Package: `org.openhospital.smartdoc.openapi`
- Generated code location: `build/generate-resources/main/src/main/java`
- Settings: Spring Boot 3, Jakarta EE, Java 8 date library, interface-only

### Key Points

- **Never modify generated code** (it will be overwritten)
- Implement generated interfaces in your controllers
- Use `operationId` in YAML to control method names
- Follow existing schema patterns (see `Document.yaml`, `Problem.yaml`)

## Mappers and DTO Mapping

Use plain `@Component` classes for mapping instead of MapStruct. Implement fluent builders using Lombok `@Builder` for entity mappings and generated builders for response DTOs.

- Mappers should have methods like `toDto`, `toModel`, `toDtos`, `toModels`, `updateModel`, `patchModel`
- For patch operations, only set non-null fields
- Use constructor injection for dependencies

## Project Structure

```
smart-doc-api/
├── src/main/
│   ├── java/org/openhospital/smartdoc/
│   │   ├── SmartdocApplication.java    # Main Spring Boot app
│   │   ├── config/                      # Configuration classes
│   │   ├── modules/documents/port/      # HTTP client interface contracts
│   │   ├── modules/persons/port/        # HTTP client interface contracts
│   │   ├── models/                      # Base entities (BaseEntity)
│   │   ├── exceptions/                  # Global exception handling
│   │   └── modules/*/                   # Feature modules (service, model, repository, mapper, rest)
│   ├── openapi/                         # OpenAPI specs (API-first)
│   │   ├── openapi.yaml                 # Main spec
│   │   ├── paths/                       # Endpoint definitions
│   │   └── components/                  # Reusable schemas/params
│   └── resources/
│       ├── application.properties       # Spring configuration
│       └── db/migration/                # Flyway migrations
├── src/test/                            # Test classes
├── build.gradle.kts                     # Gradle build config
└── gradle/libs.versions.toml            # Dependency versions
```

## Important Reminders

1. **Always run tests** after making changes: `./gradlew test`
2. **Regenerate OpenAPI code** if specs change: `./gradlew openApiGenerate`
3. **Follow Spring Boot conventions** for configuration and structure
4. **Use Lombok** to reduce boilerplate (getters, setters, constructors, builders)
5. **Implement mappers as @Component classes** with builder patterns
6. **Document public APIs** with JavaDoc
7. **Handle errors properly** using Problem Details (RFC 7807)
8. **Never commit** IDE-specific files (already in `.gitignore`)

## Recent Changes

- **Document Upload API**: Now uses query parameters (`clientId`, `type`, `date`) instead of multipart metadata
- **Document Schema**: Renamed `personId` to `clientId`, added `date` field (YYYY-MM-DD format)
- **File Storage**: Uses provided date for filename prefix instead of `LocalDate.now()`
- **Directory Structure**: Uses `clientId` instead of `personId` for file organization

## Quick Reference

| Task | Command |
|------|---------|
| Run tests | `./gradlew test` |
| Run single test | `./gradlew test --tests "ClassName.methodName"` |
| Build | `./gradlew build` |
| Run app | `./gradlew bootRun` |
| Clean | `./gradlew clean` |
| Lint OpenAPI | `./gradlew lint` |
| Generate API code | `./gradlew openApiGenerate` |</content>
<parameter name="filePath">AGENTS.md