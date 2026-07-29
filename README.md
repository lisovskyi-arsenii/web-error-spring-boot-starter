# lisovskyi-web-error-starter

![Java](https://img.shields.io/badge/Java-25-orange?logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.0-brightgreen?logo=springboot)
![Version](https://img.shields.io/badge/version-0.1.1-blue)
![License](https://img.shields.io/badge/license-Apache%202.0-green)

A Spring Boot auto-configuration library that provides a consistent, structured error response format for REST APIs. It handles all common exception types — application-level, validation, Spring Security, and unhandled — and maps them to a uniform JSON body without any configuration required in the consumer service.

---

## Project Overview

Every Spring Boot REST API must handle errors: validation failures, missing resources, access denied, malformed requests, and unexpected exceptions. Without a shared contract, each service invents its own error format, making client-side error handling inconsistent and fragile.

`lisovskyi-web-error-starter` solves this by auto-registering a `@RestControllerAdvice` that catches all standard exception types and serialises them into a unified `ErrorResponse` JSON object. It also ships a base `AppException` class, a set of ready-to-use standard exceptions, and a `@PasswordsMatch` validation annotation — so consumer services need zero error-handling boilerplate.

---

## Features

- ✅ **Unified `ErrorResponse` record** — every error produces the same JSON shape: `status`, `code`, `message`, `timestamp`, `path`, `fieldErrors` (optional), `stackTrace` (optional).
- ✅ **`GlobalExceptionHandler`** — handles:
  - `AppException` and all its subclasses (custom application exceptions)
  - `MethodArgumentNotValidException` — `@Valid` / `@Validated` failures with per-field details
  - `HttpMessageNotReadableException` — malformed JSON request body
  - `HttpRequestMethodNotSupportedException` — wrong HTTP verb
  - `MethodArgumentTypeMismatchException` — path/query parameter type errors
  - `Exception` — catch-all fallback for unexpected errors
- ✅ **`SecurityExceptionHandler`** — conditionally registered when Spring Security is on the classpath. Handles `AccessDeniedException` (→ 403) and `AuthenticationException` (→ 401).
- ✅ **`AppException` base class** — extend it to define domain-specific exceptions with an HTTP status and a machine-readable error code.
- ✅ **Standard exceptions** — ready-to-throw out of the box:
  - `ResourceNotFoundException` — 404 / `RESOURCE_NOT_FOUND`
  - `ResourceAlreadyExistsException` — 409 / `RESOURCE_ALREADY_EXISTS`
  - `BadRequestException` — 400 / `BAD_REQUEST`
  - `ForbiddenOperationException` — 403 / `FORBIDDEN`
  - `UnauthorizedException` — 401 / `UNAUTHORIZED`
  - `InternalServerErrorException` — 500 / `INTERNAL_SERVER_ERROR`
- ✅ **`@PasswordsMatch`** — class-level Bean Validation constraint for DTO password-confirmation fields.
- ✅ **Security-sensitive defaults** — stack traces and rejected values are **disabled by default**; field-level errors are included by default.
- ✅ **Conditional** — the entire handler can be disabled via `app.web.error.enabled=false`.

---

## Technologies Used

| Technology | Version |
|---|---|
| Java | 25 (minimum: 21) |
| Spring Boot BOM | 4.1.0 |
| Spring Boot Starter Web | (BOM-managed) |
| Spring Boot Starter Validation | (BOM-managed) |
| Spring Security | (BOM-managed, optional) |
| Lombok | 1.18.46 |
| Gradle | (wrapper included) |

> **Java version note:** The library is compiled with JDK 25. Consumer services must use JDK **21 or later** (the minimum LTS version compatible with Spring Boot 4.x).

---

## Project Structure

```
lisovskyi-web-error-starter/
└── src/main/java/com/lisovskyi/web/error/autoconfigure/
    ├── WebErrorAutoConfiguration.java      # Auto-configuration entry point
    ├── ErrorProperties.java                # Configuration properties (prefix: app.web.error)
    ├── ErrorResponse.java                  # Unified error response record (JSON output)
    ├── FieldErrorDto.java                  # Per-field validation error detail
    ├── GlobalExceptionHandler.java         # @RestControllerAdvice for all general exceptions
    ├── SecurityExceptionHandler.java       # @RestControllerAdvice for Spring Security exceptions (conditional)
    ├── base/
    │   └── AppException.java              # Abstract base class for custom application exceptions
    ├── standard/
    │   ├── ResourceNotFoundException.java         # 404 RESOURCE_NOT_FOUND
    │   ├── ResourceAlreadyExistsException.java    # 409 RESOURCE_ALREADY_EXISTS
    │   ├── BadRequestException.java               # 400 BAD_REQUEST
    │   ├── ForbiddenOperationException.java       # 403 FORBIDDEN
    │   ├── UnauthorizedException.java             # 401 UNAUTHORIZED
    │   └── InternalServerErrorException.java      # 500 INTERNAL_SERVER_ERROR
    └── validation/
        ├── PasswordsMatch.java            # @PasswordsMatch class-level constraint annotation
        └── PasswordsMatchValidator.java   # Constraint validator implementation
```

---

## Prerequisites

- Java **21+** (compiled against JDK 25)
- Gradle (wrapper `gradlew` / `gradlew.bat` is bundled)
- A Spring Boot **4.1.0** consumer project with `spring-boot-starter-web`

---

## Installation

Build and publish the starter to the local Maven repository:

```bash
./gradlew publishToMavenLocal
```

### Gradle (Kotlin DSL)

```kotlin
// build.gradle.kts
repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation("com.lisovskyi:lisovskyi-web-error-starter:0.1.1")
}
```

### Maven

```xml
<!-- pom.xml -->
<repositories>
  <repository>
    <id>local</id>
    <url>file://${user.home}/.m2/repository</url>
  </repository>
</repositories>

<dependencies>
  <dependency>
    <groupId>com.lisovskyi</groupId>
    <artifactId>lisovskyi-web-error-starter</artifactId>
    <version>0.1.1</version>
  </dependency>
</dependencies>
```

No additional configuration is required. Auto-configuration activates automatically.

---

## Configuration

All properties are under the `app.web.error` prefix. All are **optional** — defaults are production-safe.

### With Spring Security (default)

When `spring-boot-starter-security` is on the classpath, the `SecurityExceptionHandler` bean is registered automatically and maps `AccessDeniedException` → 403 and `AuthenticationException` → 401.

```yaml
# application.yml — Spring Security present (default)
app:
  web:
    error:
      enabled: true                  # default: true
      include-stack-trace: false     # default: false — NEVER enable in production
      include-field-errors: true     # default: true
      include-rejected-values: false # default: false — disable if fields may hold sensitive data
```

### Without Spring Security

If your service does not include `spring-boot-starter-security`, the `SecurityExceptionHandler` bean is not registered (guarded by `@ConditionalOnClass`). Everything else works normally — `GlobalExceptionHandler` still handles all non-security exceptions.

```yaml
# application.yml — no Spring Security on classpath
app:
  web:
    error:
      enabled: true
      include-stack-trace: false
      include-field-errors: true
      include-rejected-values: false
```

No extra steps are needed. The starter detects the absence of Spring Security automatically.

> **Security note:** `include-stack-trace` and `include-rejected-values` are `false` by default. Enable `include-stack-trace` **only** in local development environments — stack traces may expose internal implementation details and package structure to clients.

---

## API Documentation

There is no REST API exposed by this starter itself. It intercepts exceptions thrown by consumer services and transforms them into the following response format.

### `ErrorResponse` JSON schema

```json
{
  "status": 404,
  "code": "RESOURCE_NOT_FOUND",
  "message": "User not found with id: '123e4567-e89b-12d3-a456-426614174000'",
  "timestamp": "2026-07-29T10:00:00.000Z",
  "path": "/api/users/123e4567-e89b-12d3-a456-426614174000",
  "fieldErrors": [
    {
      "field": "email",
      "rejectedValue": null,
      "message": "must be a valid email address"
    }
  ],
  "stackTrace": null
}
```

Fields `fieldErrors`, `rejectedValue`, and `stackTrace` are `@JsonInclude(NON_NULL)` — they are omitted from the response when `null` or disabled via configuration.

### Error codes by exception type

| Exception | HTTP Status | Code |
|---|---|---|
| `ResourceNotFoundException` | 404 | `RESOURCE_NOT_FOUND` |
| `ResourceAlreadyExistsException` | 409 | `RESOURCE_ALREADY_EXISTS` |
| `BadRequestException` | 400 | `BAD_REQUEST` |
| `ForbiddenOperationException` | 403 | `FORBIDDEN` |
| `UnauthorizedException` | 401 | `UNAUTHORIZED` |
| `InternalServerErrorException` | 500 | `INTERNAL_SERVER_ERROR` |
| `MethodArgumentNotValidException` | 400 | `VALIDATION_FAILED` |
| `HttpMessageNotReadableException` | 400 | `MALFORMED_JSON` |
| `HttpRequestMethodNotSupportedException` | 405 | `METHOD_NOT_ALLOWED` |
| `MethodArgumentTypeMismatchException` | 400 | `TYPE_MISMATCH` |
| `AccessDeniedException` | 403 | `FORBIDDEN` |
| `AuthenticationException` | 401 | `UNAUTHORIZED` |
| Any other `Exception` | 500 | `INTERNAL_SERVER_ERROR` |

---

## Usage Examples

### 1. Throw a standard exception

```java
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public User findById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
    }

    public User create(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new ResourceAlreadyExistsException("User with email '" + request.email() + "' already exists");
        }
        // ...
    }
}
```

### 2. Define a custom domain exception

```java
public class BookingConflictException extends AppException {
    public BookingConflictException(String message) {
        super(message, HttpStatus.CONFLICT, "BOOKING_CONFLICT");
    }
}

// Throw it anywhere — the handler catches AppException and its subclasses:
throw new BookingConflictException("The requested time slot is already taken");
```

Response:
```json
{
  "status": 409,
  "code": "BOOKING_CONFLICT",
  "message": "The requested time slot is already taken",
  "timestamp": "2026-07-29T10:00:00.000Z",
  "path": "/api/bookings"
}
```

### 3. Use `@PasswordsMatch` on a registration DTO

```java
@PasswordsMatch(
    originalPassword = "password",
    confirmPassword  = "confirmPassword",
    message          = "Passwords do not match"
)
public record RegisterRequest(
    @NotBlank String email,
    @NotBlank String password,
    @NotBlank String confirmPassword
) {}
```

```java
@PostMapping("/register")
public ResponseEntity<Void> register(@RequestBody @Valid RegisterRequest request) {
    authService.register(request);
    return ResponseEntity.status(HttpStatus.CREATED).build();
}
```

If the passwords do not match, the response is automatically:
```json
{
  "status": 400,
  "code": "VALIDATION_FAILED",
  "message": "Validation failed for one or more fields",
  "timestamp": "2026-07-29T10:00:00.000Z",
  "path": "/api/auth/register",
  "fieldErrors": [
    {
      "field": "passwordsMatch",
      "message": "Passwords do not match"
    }
  ]
}
```

---

## Known Limitations

- **`GlobalExceptionHandler` vs Spring's `DefaultHandlerExceptionResolver`** — Spring MVC has its own built-in handler for some exceptions (e.g., `MethodNotAllowedException`). The starter's `@RestControllerAdvice` takes precedence for all the exceptions listed in the error code table above, but any exception not listed there will still fall through to the catch-all `Exception` handler and return a 500.
- **`@PasswordsMatch` uses reflection** — the validator resolves field values via `getClass().getMethod(fieldName)`. If the annotated type does not expose a no-arg getter (or record accessor) for the specified field names, a `NoSuchMethodException` is thrown at validation time — not at startup. Verify field names match accessor names.
- **`SecurityExceptionHandler` requires Spring Security on the classpath** — if you add `spring-boot-starter-security` later and the handler does not appear, ensure `WebErrorAutoConfiguration` is not excluded via `spring.autoconfigure.exclude`.
- **Stack traces in responses** — `include-stack-trace=true` is safe in development but must never be enabled in production. Stack traces expose internal package structure, dependency versions, and logic paths that attackers can use for reconnaissance.

---

## Testing

```bash
./gradlew test
```

---

## Contributing

Contributions are welcome!

1. Fork the repository and create your feature branch from `main`.
2. Make sure the project builds and tests pass: `./gradlew build`.
3. Keep code style consistent with the existing conventions (Lombok, Java records, `@RestControllerAdvice`).
4. When adding new exception handlers, add a corresponding entry to the error codes table in this README.
5. Open a pull request describing what you changed and why.

---

## License

This project is licensed under the **Apache License 2.0** — see the [LICENSE](LICENSE) file for details.

Key points of Apache 2.0:
- ✅ Free to use, modify, and distribute
- ✅ Can be used in commercial and proprietary projects
- ✅ Patent grant — contributors grant users a license to any patents covering the contribution
- ✅ Must preserve copyright and license notices
- ✅ Changes to the source must be stated
