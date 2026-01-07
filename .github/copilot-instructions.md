# Copilot Instructions - Hexagonal Backend (Java)

## Project Context

**RappiDrive** is a white-label ride-hailing platform (Uber-like business model) being developed as an MVP.

**Key Characteristics:**
- **Multi-tenant/White-label**: Platform can be branded and deployed for different companies/clients
- **Core Features**: Driver/rider matching, real-time tracking, trip management, payment processing
- **MVP Scope**: Focus on essential features first - ride booking, driver assignment, basic tracking
- **Typical Domain Entities**: Driver, Passenger, Trip/Ride, Location, Payment, Tenant/Company

**Architectural Implications:**
- Multi-tenancy must be considered in data isolation (tenant_id in aggregates)
- Real-time requirements (WebSocket/SSE for tracking, notifications)
- Geospatial queries (PostgreSQL PostGIS extension)
- Eventual consistency for some operations (matching, availability)
- Configuration per tenant (pricing rules, service areas, branding)

## Architecture Principles - NON-NEGOTIABLE

This project follows **Hexagonal Architecture (Ports & Adapters)** and **SOLID principles** with ZERO tolerance for violations. Every code contribution must demonstrate understanding of these concepts.

### Mandatory Rules
1. **Domain Layer Purity**: NEVER import Spring, JPA, Jackson, or ANY framework in `domain/` package
2. **Dependency Direction**: Dependencies MUST flow inward only (Infrastructure → Application → Domain)
3. **Interface Segregation**: Ports must be small, focused interfaces - one responsibility per port
4. **No Leaky Abstractions**: JPA entities, HTTP requests, external APIs stay in infrastructure - NEVER leak to domain
5. **Constructor Injection Only**: All dependencies injected via constructor (enables testability, immutability)
6. **Domain Logic in Domain**: Business rules, validations, and invariants belong ONLY in domain layer

### Violations That Will Be Rejected
- `@Entity`, `@Table`, `@Column` annotations in domain entities
- `@RestController`, `@Service`, `@Component` in domain or application layers
- Direct database access from use cases
- Use cases depending on concrete implementations instead of ports
- Domain entities with public setters or anemic models
- Framework exceptions bubbling up to domain layer

## Architecture Overview

This project follows **Hexagonal Architecture** (Ports & Adapters) using Java:

- **Domain Layer** (`com.rappidrive.domain`): Pure business logic, entities, value objects, and domain services. No external dependencies (no Spring, no frameworks).
- **Application Layer** (`com.rappidrive.application`): Use cases and application services that orchestrate domain logic. Defines ports (interfaces).
- **Infrastructure Layer** (`com.rappidrive.infrastructure`): Adapters implementing ports - databases, REST clients, message queues, external services.
- **Presentation Layer** (`com.rappidrive.presentation`): REST controllers, DTOs, request/response handlers.

### Dependency Rule
Dependencies flow **inward only**: Infrastructure → Application → Domain. Domain must never depend on outer layers or frameworks.

## Key Patterns & Conventions

### Package Structure
```
src/main/java/com/rappidrive/
├── domain/                    # Business entities and logic (framework-free)
│   ├── entities/              # Domain entities
│   ├── valueobjects/          # Value objects (Email, CPF, Money, etc.)
│   ├── services/              # Domain services
│   └── exceptions/            # Domain exceptions
├── application/               # Use cases and port interfaces
│   ├── usecases/              # Use case implementations
│   └── ports/
│       ├── input/             # Driving ports (use case interfaces)
│       └── output/            # Driven ports (repository, external service interfaces)
├── infrastructure/            # Port implementations (framework-dependent)
│   ├── persistence/           # JPA entities, repositories
│   ├── adapters/              # External service adapters
│   ├── messaging/             # Message queue adapters
│   └── config/                # Spring configuration, beans
└── presentation/              # REST API layer
    ├── controllers/           # Spring REST controllers
    ├── dto/                   # Request/Response DTOs
    └── mappers/               # DTO ↔ Domain mappers
```

### Naming Conventions
- **Use Cases**: `{Action}{Entity}UseCase` (e.g., `CreateUserUseCase`, `GetOrderByIdUseCase`)
- **Input Ports**: `{Action}{Entity}InputPort` (e.g., `CreateUserInputPort`)
- **Output Ports**: `{Entity}RepositoryPort`, `{Service}Port` (e.g., `UserRepositoryPort`, `EmailServicePort`)
- **Adapters**: `{Implementation}{Adapter}` (e.g., `JpaUserRepositoryAdapter`, `SendGridEmailAdapter`)
- **DTOs**: `{Entity}{Action}Request/Response` (e.g., `CreateUserRequest`, `UserResponse`)
- **Domain Entities**: PascalCase without suffixes (e.g., `User`, `Order`, `Product`)
- **Value Objects**: Descriptive names (e.g., `Email`, `CPF`, `Money`, `Address`)

### Use Case Pattern
Each use case should:
1. Implement an input port interface
2. Depend only on output ports (constructor injection)
3. Return domain entities or custom response objects
4. Handle a single business operation
5. Be annotated with `@UseCase` (custom annotation) or `@Service` in infrastructure layer

Example:
```java
// application/ports/input/CreateUserInputPort.java
public interface CreateUserInputPort {
    User execute(CreateUserCommand command);
}

// application/usecases/CreateUserUseCase.java
public class CreateUserUseCase implements CreateUserInputPort {
    private final UserRepositoryPort userRepository;
    private final EmailServicePort emailService;
    
    public CreateUserUseCase(UserRepositoryPort userRepository, 
                            EmailServicePort emailService) {
        this.userRepository = userRepository;
        this.emailService = emailService;
    }
    
    @Override
    public User execute(CreateUserCommand command) {
        // Domain logic here
        User user = new User(command.name(), command.email());
        User savedUser = userRepository.save(user);
        emailService.sendWelcomeEmail(savedUser.getEmail());
        return savedUser;
    }
}
```

### Repository Pattern
- Define repository interfaces in `application/ports/output/` (e.g., `UserRepositoryPort`)
- Implement in `infrastructure/persistence/` (e.g., `JpaUserRepositoryAdapter`)
- Ports work with domain entities only
- Adapters convert between JPA entities and domain entities
- Use Spring Data JPA in adapters, never expose JPA annotations to domain

Example:
```java
// application/ports/output/UserRepositoryPort.java
public interface UserRepositoryPort {
    User save(User user);
    Optional<User> findById(UserId id);
    Optional<User> findByEmail(Email email);
}

// infrastructure/persistence/JpaUserRepositoryAdapter.java
@Component
public class JpaUserRepositoryAdapter implements UserRepositoryPort {
    private final SpringDataUserRepository jpaRepository;
    private final UserMapper mapper;
    
    @Override
    public User save(User user) {
        UserJpaEntity entity = mapper.toJpaEntity(user);
        UserJpaEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }
}
```

### Error Handling
- **Domain exceptions**: Extend `DomainException` (unchecked), placed in `domain/exceptions/`
  - Examples: `InvalidEmailException`, `UserNotFoundException`, `InsufficientBalanceException`
- **Application exceptions**: Use case specific, extend `ApplicationException`
- **Infrastructure exceptions**: Wrap external errors at adapter boundaries (never leak JPA/HTTP exceptions)
- Use `@ControllerAdvice` in presentation layer to handle exceptions globally
- Return proper HTTP status codes: 400 for validation, 404 for not found, 409 for conflicts, 500 for unexpected errors

## Development Workflows

### Build & Run
- **Build**: `mvn clean install` or `./gradlew build`
- **Run**: `mvn spring-boot:run` or `./gradlew bootRun`
- **Tests**: `mvn test` or `./gradlew test`
- **Package**: `mvn package` (generates JAR in `target/`)

### Adding New Features
1. **Domain First**: Define entities/value objects in `domain/` (no annotations, pure Java)
2. **Output Ports**: Create repository/service interfaces in `application/ports/output/`
3. **Input Port**: Define use case interface in `application/ports/input/`
4. **Use Case**: Implement business logic in `application/usecases/`
5. **Adapters**: Implement ports in `infrastructure/` (JPA, REST clients, etc.)
6. **Presentation**: Create controller and DTOs in `presentation/`
7. **Configuration**: Wire beans in `infrastructure/config/` using `@Configuration`

### Testing Strategy
- **Unit Tests**: Domain logic and use cases with mocked ports (JUnit 5 + Mockito)
- **Integration Tests**: Test adapters with `@DataJpaTest`, `@WebMvcTest`, Testcontainers (PostgreSQL)
- **E2E Tests**: `@SpringBootTest` with full context, test complete flows
- **Architecture Tests**: Use ArchUnit to enforce hexagonal boundaries

## Project-Specific Rules

- **No Framework in Domain**: Domain layer must be pure Java - no Spring, JPA, Jackson, or any framework annotations
- **Dependency Injection**: Use constructor-based DI (required by hexagonal architecture)
- **Immutability**: Prefer immutable value objects and entities where possible
- **Database Access**: 
  - PostgreSQL accessed only through infrastructure layer adapters
  - Use Spring Data JPA repositories, never expose JPA entities to domain
  - Prefer native PostgreSQL features (JSONB, arrays, etc.) when appropriate
  - Consider PostGIS extension for geospatial queries (driver location, service areas)
- **Multi-tenancy**:
  - Domain entities should include tenant context where needed
  - Infrastructure layer handles tenant isolation at database level
  - Never expose data across tenants in queries
- **Validation**: 
  - Input validation in presentation layer (Bean Validation annotations on DTOs)
  - Business rules validation in domain layer (throw domain exceptions)
- **Value Objects**: Use value objects for domain concepts (Email, CPF, Money, Location, TripStatus) instead of primitives
- **No Setters in Domain**: Domain entities should use constructors and behavior methods, not setters
- **Mappers**: Always map between DTOs and domain entities - never expose domain directly via API

## Common Tasks

### Create a new domain entity
1. Create class in `domain/entities/` (no annotations, pure Java)
2. Define value objects for domain concepts
3. Add business methods (not just getters/setters)

### Add a new use case
1. Create interface in `application/ports/input/` (e.g., `CreateOrderInputPort`)
2. Implement in `application/usecases/` (e.g., `CreateOrderUseCase`)
3. Create `@Configuration` class in `infrastructure/config/` to wire dependencies

### Add external integration (REST API, message queue, etc.)
1. Define port interface in `application/ports/output/`
2. Implement adapter in `infrastructure/adapters/`
3. Annotate adapter with `@Component` or `@Service`
4. Inject into use cases that need it

### Add REST endpoint
1. Create request/response DTOs in `presentation/dto/`
2. Create controller in `presentation/controllers/` with `@RestController`
3. Inject input port (use case interface)
4. Create mapper to convert DTO ↔ Domain

## Technology Stack

- **Language**: Java 17+
- **Framework**: Spring Boot 3.x
- **Build Tool**: Maven or Gradle
- **Database**: PostgreSQL with Spring Data JPA/Hibernate (in infrastructure layer only)
- **Testing**: JUnit 5, Mockito, Testcontainers (PostgreSQL), ArchUnit
- **Validation**: Bean Validation (Jakarta Validation)
- **Migration**: Flyway or Liquibase for database versioning

## References

- Clean Architecture by Robert C. Martin
- Hexagonal Architecture pattern by Alistair Cockburn
- Get Your Hands Dirty on Clean Architecture by Tom Hombergs
