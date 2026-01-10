# Copilot Instructions - Hexagonal Backend (Java)

## Project Context

**RappiDrive** is a white-label ride-hailing platform (Uber-like business model) MVP built with **Hexagonal Architecture** (Ports & Adapters).

**Current State (as of January 2026):**
- ✅ Full hexagonal architecture scaffold complete
- ✅ Core entities implemented: `Driver`, `Passenger`, `Trip`, `Vehicle`, `Payment`, `Rating`, `Notification`
- ✅ 20+ use cases across driver, trip, payment, rating domains
- ✅ Domain events & Outbox pattern for reliable event publishing
- ✅ Virtual Threads (Java 21) enabled for HTTP requests and async operations
- ✅ Parallel query execution using `ParallelExecutor` utility
- ✅ PostGIS integration for geospatial driver queries (>10K drivers, <50ms queries)
- ✅ Comprehensive test coverage (unit, integration, E2E, architecture tests)

**Key Characteristics:**
- **Multi-tenancy**: All aggregates include `TenantId` for data isolation
- **Performance**: Virtual threads, parallel I/O, PostGIS spatial indexes
- **Reliability**: Outbox pattern ensures event delivery even on service crashes
- **Domain-driven**: Rich domain models with behavior-driven entities

## Architecture Principles - NON-NEGOTIABLE

This project follows **Hexagonal Architecture** (Ports & Adapters) and **SOLID principles** with ZERO tolerance for violations.

### Mandatory Rules
1. **Domain Layer Purity**: NEVER import Spring, JPA, Jackson, or ANY framework in `domain/` package
2. **Dependency Direction**: Dependencies MUST flow inward only (Infrastructure → Application → Domain)
3. **Interface Segregation**: Ports must be small, focused interfaces - one responsibility per port
4. **No Leaky Abstractions**: JPA entities, HTTP requests, external APIs stay in infrastructure
5. **Constructor Injection Only**: All dependencies injected via constructor (enables testability)
6. **Domain Logic in Domain**: Business rules, validations, invariants belong ONLY in domain layer
7. **Immutable Value Objects**: Value objects like `Email`, `CPF`, `Money`, `Location` are immutable

### Violations That Will Be Rejected
- `@Entity`, `@Table`, `@Column` annotations in domain entities
- `@RestController`, `@Service`, `@Component` in domain or application layers
- Direct database access from use cases (must go through ports)
- Use cases depending on concrete implementations instead of ports
- Domain entities with public setters or anemic models (no behavior)
- Framework exceptions bubbling up to domain layer
- Framework imports in any domain/ classes

## Architecture Overview

This project strictly follows **Hexagonal Architecture** (Ports & Adapters):

- **Domain Layer** (`com.rappidrive.domain`): Pure business logic, rich entities, value objects, domain services, enums. Zero framework dependencies.
- **Application Layer** (`com.rappidrive.application`): Use cases implementing input ports, application services, `ParallelExecutor` for concurrent operations, custom exceptions.
- **Infrastructure Layer** (`com.rappidrive.infrastructure`): Port adapters (JPA repositories, external service clients), mappers, Spring configuration, messaging, monitoring.
- **Presentation Layer** (`com.rappidrive.presentation`): REST controllers, DTOs, mappers, exception handlers.

### Dependency Rule (Sacred)
Dependencies flow **inward only**: Infrastructure → Application → Domain. Domain is completely isolated from external dependencies.

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

### Prerequisites & Setup
- **Java**: 21+ (LTS) - Required for virtual threads
- **Build**: Maven 3.8+
- **Database**: PostgreSQL 16 + PostGIS 3.4 (via Docker)
- **Start DB**: `docker-compose up -d` (starts PostgreSQL at localhost:5432 + pgAdmin at localhost:5050)

### Build & Run Commands
```bash
# Full build (includes tests)
mvn clean install

# Run application
mvn spring-boot:run

# Run only unit tests
mvn test

# Run integration + E2E tests (uses Testcontainers)
mvn verify

# Package JAR
mvn package -DskipTests
```

### Key Project Features to Understand Before Coding
1. **Virtual Threads** (Java 21): Enabled by default (`spring.threads.virtual.enabled=true`)
   - All HTTP requests run on virtual threads
   - Use for I/O-heavy operations
   - No special code needed - Spring Boot handles it

2. **ParallelExecutor** (Concurrent Operations)
   - Located: `com.rappidrive.application.concurrency.ParallelExecutor`
   - Methods: `executeAll()`, `executeRace()`, `mapParallel()`
   - Used in: `FindAvailableDriversUseCase` for querying multiple geographic zones
   - Works seamlessly with virtual threads

3. **Outbox Pattern** (Reliable Event Publishing)
   - Domain events automatically captured in `outbox_event` table
   - `OutboxPublisher` processes pending events asynchronously
   - Ensures events are published even if service crashes
   - Key classes: `DomainEvent`, `OutboxEventJpaEntity`, `OutboxPublisher`, `EventDispatcherPort`

4. **PostGIS Spatial Queries** (Geospatial Operations)
   - Used in: `FindAvailableDriversUseCase` for near-me driver search
   - Query: `ST_DWithin()` with KNN operator (`<->`)
   - Performance: <50ms for 10,000+ drivers with proper indexes
   - Location stored in domain as `Location` value object (lat/lon)

5. **Multi-Tenancy**
   - Every aggregate root has `TenantId` field
   - Tenant isolation enforced at repository level (WHERE tenant_id = ?)
   - Never query/access data across tenants

### Adding New Features (Step-by-Step)
1. **Domain First**: Create entities/value objects in `domain/` (no annotations, pure Java)
   - Behavior-rich classes with invariant enforcement
   - Use value objects for domain concepts (not primitives)
   
2. **Output Ports**: Define repository/service interfaces in `application/ports/output/`
   - Small, focused interfaces (one responsibility)
   
3. **Input Port**: Create use case interface in `application/ports/input/` (e.g., `FindAvailableDriversInputPort`)
   
4. **Use Case**: Implement in `application/usecases/` (e.g., `FindAvailableDriversUseCase`)
   - Depend only on output ports via constructor injection
   - Use `ParallelExecutor` for parallel operations if needed
   
5. **Adapters**: Implement ports in `infrastructure/`
   - JPA adapters in `infrastructure/persistence/adapters/`
   - External service adapters in `infrastructure/adapters/`
   - Always map between JPA and domain entities
   
6. **Configuration**: Wire beans in `infrastructure/config/BeanConfiguration.java`
   
7. **Presentation**: Create REST controller + DTOs
   - Controllers in `presentation/controllers/`
   - DTOs in `presentation/dto/`
   - Mappers in `presentation/mappers/`

### Testing Strategy
- **Unit Tests**: Domain logic + use cases with Mockito
  - Located: `src/test/java/com/rappidrive/domain/` and `.../application/usecases/`
  - Mock output ports
  
- **Integration Tests**: Adapters + repositories with `@DataJpaTest`, Testcontainers
  - Located: `src/test/java/com/rappidrive/infrastructure/`
  - Tests actual database interactions
  
- **E2E Tests**: Full application context with `@SpringBootTest`
  - Located: `src/test/java/com/rappidrive/e2e/`
  - Tests complete user workflows
  
- **Architecture Tests**: Enforce hexagonal boundaries with ArchUnit
  - Located: `src/test/java/com/rappidrive/architecture/HexagonalArchitectureTest.java`
  - Validates no framework imports in domain/

## Project-Specific Rules & Patterns

### Critical Patterns in This Codebase

**1. Domain Events & Outbox Pattern**
- Domain entities emit events (e.g., `DriverCreatedEvent`, `TripCompletedEvent`)
- Use: `DomainEventsCollector.collect(event)` in domain methods
- Events automatically captured in `outbox_event` table during transaction
- `OutboxPublisher` asynchronously publishes to `EventDispatcherPort`
- Example: [TripCompletionService.java](src/main/java/com/rappidrive/domain/services/TripCompletionService.java)

**2. Parallel Execution Pattern** 
- `ParallelExecutor.mapParallel()` for transforming collections in parallel
- `ParallelExecutor.executeAll()` for multiple tasks that must all succeed
- Used in: `FindAvailableDriversUseCase` to query multiple geographic zones concurrently
- Exception handling: Exceptions propagated after all tasks complete
- Example: [FindAvailableDriversUseCase.java](src/main/java/com/rappidrive/application/usecases/driver/FindAvailableDriversUseCase.java)

**3. Value Objects with Converters**
- All value objects immutable with `@Immutable` (if needed)
- Database converters bridge JPA and domain value objects
- Example: `EmailConverter`, `CPFConverter`, `PhoneConverter`, `TenantIdConverter`
- Located: `infrastructure/persistence/converters/`

**4. Multi-Tenancy Enforcement**
- `TenantId` appears in: Driver, Passenger, Trip, Vehicle, Payment, Rating, Notification
- Repository queries ALWAYS include `WHERE tenant_id = ?`
- Never expose data across tenants - verify in every query
- Configuration handled at infrastructure layer, transparent to domain

**5. Exception Hierarchy**
- `DomainException` (unchecked) for business rule violations
  - Examples: `DriverNotFoundException`, `InvalidFareException`, `TripConcurrencyException`
- `ApplicationException` for use case specific errors
- `GlobalExceptionHandler` maps to HTTP status codes in `presentation/exception/`
- HTTP Status Mapping:
  - 400: Domain validation errors (Bad Request)
  - 404: Not found exceptions
  - 409: Conflict (duplicate email, race conditions, state conflicts)
  - 500: Unexpected infrastructure errors

**6. Mapper Patterns**
- Infrastructure mappers: JPA entity ↔ Domain entity
  - Located: `infrastructure/persistence/mappers/` and `infrastructure/persistence/fare/`
  - Example: [DriverMapper.java](src/main/java/com/rappidrive/infrastructure/persistence/mappers/DriverMapper.java)
- Presentation mappers: DTO ↔ Domain entity
  - Located: `presentation/mappers/`
  - Example: [DriverDtoMapper.java](src/main/java/com/rappidrive/presentation/mappers/DriverDtoMapper.java)
- Always convert at boundaries - never expose domain entities directly via REST API

**7. Domain Services**
- Used for complex business logic that spans multiple entities
- Located: `domain/services/`
- Examples: `StandardFareCalculator`, `RatingValidationService`, `TripCompletionService`
- Stateless, injected by name/type where needed

**8. Enums in Domain**
- Located: `domain/enums/`
- Examples: `DriverStatus`, `TripStatus`, `PaymentStatus`, `RatingType`, `VehicleType`
- Use in domain entities and value objects

### No Framework in Domain
- Domain layer: Pure Java only
- Allowed in domain: Standard Java, custom exceptions, value objects
- Forbidden in domain: `@Entity`, `@SpringBootTest`, `@Autowired`, Lombok annotations

### Database
- PostgreSQL 16 + PostGIS 3.4 for geospatial
- JPA/Hibernate only in `infrastructure/persistence/`
- Domain entities are separate from JPA entities
- Migrations: Flyway scripts in `src/main/resources/db/migration/`

### Validation
- **Input validation**: DTO level in `presentation/dto/` with Bean Validation annotations
- **Business rule validation**: Domain layer (throw domain exceptions)
- Value objects validate in constructors (e.g., `Email`, `CPF`, `Phone`)

### Configuration
- Application profiles: `application-dev.yml`, `application-test.yml`, `application-prod.yml`
- Virtual threads enabled: `spring.threads.virtual.enabled=true`
- Bean configuration centralized: `infrastructure/config/BeanConfiguration.java`, `ParallelExecutorConfiguration.java`
- Database pooling monitored: `ConnectionPoolMonitor`, `ConnectionPoolHealthIndicator`

## Common Tasks

### Create a new domain entity
1. Create class in `domain/entities/` (no annotations, pure Java)
2. Include `TenantId` for multi-tenancy
3. Add constructor validation (throw `IllegalArgumentException`)
4. Define value objects for domain concepts
5. Add behavior methods (not just getters/setters)
6. Example: [Driver.java](src/main/java/com/rappidrive/domain/entities/Driver.java) (lines 1-80)

### Add a new use case
1. Create interface in `application/ports/input/` (e.g., `CreateOrderInputPort`)
2. Implement in `application/usecases/` (e.g., `CreateOrderUseCase`)
3. Use constructor injection for output ports
4. Create `@Configuration` class in `infrastructure/config/` to wire dependencies
5. Example: [CreateTripUseCase.java](src/main/java/com/rappidrive/application/usecases/trip/CreateTripUseCase.java)

### Add external integration (REST API, message queue, etc.)
1. Define port interface in `application/ports/output/` (e.g., `PaymentGatewayPort`)
2. Implement adapter in `infrastructure/adapters/`
3. Annotate adapter with `@Component` or `@Service`
4. Inject into use cases that need it
5. Example: [MockPaymentGatewayAdapter.java](src/main/java/com/rappidrive/infrastructure/adapters/MockPaymentGatewayAdapter.java)

### Add REST endpoint
1. Create request/response DTOs in `presentation/dto/`
2. Create controller in `presentation/controllers/` with `@RestController`
3. Inject input port (use case interface)
4. Create mapper to convert DTO ↔ Domain
5. Use `@ExceptionHandler` or rely on `GlobalExceptionHandler`
6. Example: [TripController.java](src/main/java/com/rappidrive/presentation/controllers/TripController.java)

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
