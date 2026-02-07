# ADR-004: Layered Architecture Pattern

**Status**: Accepted
**Date**: 2026-02-07
**Deciders**: Engineering Team
**Technical Story**: Establish maintainable code organization

## Context

We need a code organization pattern that:
- Separates concerns clearly
- Enables independent testing of layers
- Follows established Spring Boot conventions
- Scales with team and codebase growth
- Is understandable by new team members

## Decision Drivers

* Maintainability and readability
* Testability (unit and integration tests)
* Team familiarity with patterns
* Spring Boot ecosystem alignment
* Separation of concerns
* Future scalability

## Considered Options

1. **Layered Architecture** (Controller → Service → Repository → Entity)
2. **Hexagonal Architecture** (Ports and Adapters)
3. **Clean Architecture** (Uncle Bob)
4. **Vertical Slices** (Feature-based organization)

## Decision Outcome

Chosen option: **Layered Architecture**, because it aligns with Spring Boot conventions, is well-understood by the team, and provides sufficient separation of concerns for our current scale.

### Implementation Details

```
Layer Structure:
┌─────────────────────────────────────────────────────────────┐
│                    Presentation Layer                        │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐         │
│  │ Controller  │  │    DTO      │  │  Validation │         │
│  └─────────────┘  └─────────────┘  └─────────────┘         │
├─────────────────────────────────────────────────────────────┤
│                    Business Layer                            │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐         │
│  │   Service   │  │  Security   │  │   Events    │         │
│  └─────────────┘  └─────────────┘  └─────────────┘         │
├─────────────────────────────────────────────────────────────┤
│                    Persistence Layer                         │
│  ┌─────────────┐  ┌─────────────┐                          │
│  │ Repository  │  │   Entity    │                          │
│  └─────────────┘  └─────────────┘                          │
└─────────────────────────────────────────────────────────────┘

Package Structure:
org.example/
├── controller/     # REST endpoints
├── dto/            # Data Transfer Objects
├── service/        # Business logic
├── repository/     # Data access
├── entity/         # JPA entities
├── security/       # Security filters & utilities
├── config/         # Configuration classes
├── exception/      # Custom exceptions
└── validation/     # Custom validators
```

### Layer Responsibilities

| Layer | Responsibility | Allowed Dependencies |
|-------|----------------|---------------------|
| Controller | HTTP handling, validation, response formatting | Service, DTO |
| Service | Business logic, transactions, orchestration | Repository, Entity, other Services |
| Repository | Data access, queries | Entity |
| Entity | Domain model, JPA mapping | None (POJO) |
| DTO | Data transfer, API contracts | None (Records) |

### Positive Consequences

* **Clear separation**: Each layer has single responsibility
* **Testability**: Layers can be unit tested in isolation
* **Familiarity**: Standard Spring pattern, easy onboarding
* **Maintainability**: Changes isolated to relevant layers
* **Framework alignment**: Matches Spring Boot conventions

### Negative Consequences

* **Indirection**: Data passes through multiple layers
* **Boilerplate**: Mapping between DTOs and Entities
* **Anemic domain**: Business logic in services, not entities
* **Horizontal growth**: Many files per feature

## Pros and Cons of the Options

### Layered Architecture (Chosen)

* Good, because aligns with Spring Boot conventions
* Good, because well-understood by team
* Good, because clear testing boundaries
* Good, because simple mental model
* Bad, because some mapping boilerplate
* Bad, because can lead to anemic domain model

### Hexagonal Architecture

* Good, because domain-centric design
* Good, because framework-independent core
* Good, because highly testable
* Bad, because more complex package structure
* Bad, because learning curve for team
* Bad, because overkill for current scale

### Clean Architecture

* Good, because strict dependency rules
* Good, because highly decoupled
* Bad, because significant boilerplate
* Bad, because complex for small applications
* Bad, because reduced team velocity initially

### Vertical Slices

* Good, because feature-complete units
* Good, because reduces cross-feature coupling
* Bad, because code duplication between slices
* Bad, because harder to enforce patterns
* Bad, because not aligned with Spring conventions

## Design Principles Applied

1. **Dependency inversion**: Controllers depend on service interfaces
2. **Single responsibility**: Each class has one reason to change
3. **Constructor injection**: All dependencies via constructor (immutable)
4. **Immutable DTOs**: Using Java Records for request/response objects

## Migration Path

If complexity grows, we can evolve to Hexagonal by:
1. Extracting domain services from application services
2. Introducing port interfaces for external dependencies
3. Moving to package-by-feature within hexagonal structure

## Links

* [Spring MVC Architecture](https://docs.spring.io/spring-framework/docs/current/reference/html/web.html)
* [Martin Fowler - Layered Architecture](https://martinfowler.com/bliki/LayeredArchitecture.html)
