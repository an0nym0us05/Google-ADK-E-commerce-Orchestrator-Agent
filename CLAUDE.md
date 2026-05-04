# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

---

## Commands

```bash
# Build (skip tests for speed)
mvn clean package -DskipTests

# Run all tests
mvn test

# Run a single test class
mvn test -Dtest=OrderToolsTest

# Run a single test method
mvn test -Dtest=OrderToolsTest#getOrderById_existingOrder_containsId

# Run the application (GOOGLE_API_KEY must be set)
mvn exec:java -Dexec.mainClass="com.ecommerce.support.Main"
```

---

## Technology Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Build | Maven |
| AI Agent Framework | Google Agent Development Kit (ADK) 1.0.0 — `com.google.adk` |
| LLM | Gemini 2.5 Flash via Google AI Studio (`GOOGLE_API_KEY`) |
| Dev UI | `google-adk-dev` 1.0.0 — Spring Boot embedded server on port 8080 |
| Async/streaming | RxJava 3 (`Flowable`, `blockingForEach`) |
| Testing | JUnit 5 (Jupiter) + Testcontainers |
| Data models | Java records |
| Session state | `InMemorySessionService` (ADK built-in) |
| Database (optional) | PostgreSQL via Spring JDBC + HikariCP + Flyway |

---

## Architecture

### Multi-Agent Orchestration Pattern

This project implements the **Orchestrator → Specialist Sub-Agent** pattern from Google ADK. A single top-level agent receives all user messages and routes them to domain-specialist agents using `AgentTool`. Each sub-agent is a fully independent `LlmAgent` with its own system prompt, model instance, and tool set.

```
User input
    │
    ▼
SupportOrchestratorAgent          ← handles greetings; routes everything else
    ├── AgentTool(OrderAgent)      ← order lookup, history, shipment tracking
    ├── AgentTool(RefundAgent)     ← refund status, create refund, refund history
    └── AgentTool(ProductAgent)    ← product search, details, stock availability
```

The orchestrator uses the sub-agent's `.description()` field to decide routing — the LLM reads it at inference time, so descriptions must be precise and distinct.

### Dual Interface: Dev UI + CLI

`Main.java` wires everything and starts two interfaces in the same JVM:

1. **Dev UI** (`AdkWebServer.start(orchestrator)`) — runs on a background non-daemon thread. Boots a Spring Boot server on port 8080 with a visual playground for testing agents, inspecting events, and visualising the agent graph.
2. **CLI loop** — blocks the main thread with a `Scanner`. Uses `Runner.runAsync()` which returns an RxJava `Flowable<Event>`, iterated with `blockingForEach`. Only `event.finalResponse()` events are printed.

Both interfaces share the same `orchestrator` agent instance but have **separate session contexts** — the Dev UI manages its own sessions internally while the CLI creates one `InMemorySessionService` session at startup.

### Wiring Order in Main.java

```
DATABASE_URL set? ──yes──▶ JdbcRepository (HikariCP + Flyway migrate)
                  ──no───▶ MockRepository

Repository → *Tools → *Agent.create() → SupportOrchestratorAgent.create()
                                                    │
                          ┌─────────────────────────┤
                          │                         │
                Runner.builder()             AdkWebServer.start()
                (CLI path)                   (Dev UI path)
```

---

## Key Patterns

### Static Tool Method Pattern (FunctionTool constraint)

Google ADK's `FunctionTool.create(Class, "methodName")` only works with **static** methods. However, tools need access to repository instances (not singletons by default). The workaround used across all three tool classes:

```java
public class OrderTools {
    private static volatile OrderTools INSTANCE;   // singleton holder

    public static void register(OrderTools instance) { INSTANCE = instance; }

    // Static entry point — called by ADK at inference time
    @Annotations.Schema(name = "getOrderById", description = "...")
    public static String getOrderByIdTool(String orderId) {
        return INSTANCE.getOrderById(orderId);     // delegates to instance
    }

    // Instance method — used directly in unit tests
    public String getOrderById(String orderId) { ... }
}
```

`register()` is called inside each agent's `create()` factory method. The `@Annotations.Schema` name is what the LLM sees — keep it a clean verb phrase with no "Tool" suffix (the suffix is only on the Java method to distinguish it).

### Repository Interface → Two Implementations

All data access goes through interfaces (`OrderRepository`, `ProductRepository`, `RefundRepository`). There are two implementations:

- `repository/mock/` — in-memory, zero-dependency, used when `DATABASE_URL` is not set
- `repository/jdbc/` — Spring `JdbcTemplate` backed by PostgreSQL, selected at startup when all three DB env vars are present

Adding a third implementation (e.g. a different database) requires only a new class — no changes to tools or agents.

### Agent Factory Method Pattern

Each agent class (`OrderAgent`, `RefundAgent`, `ProductAgent`, `SupportOrchestratorAgent`) has a private constructor and a single static `create(...)` factory. This prevents accidental instantiation and keeps the agent graph assembly in one place per agent.

---

## Database Configuration

When `DATABASE_URL`, `DATABASE_USER`, and `DATABASE_PASSWORD` are all set, `Main` uses the JDBC repositories and runs Flyway migrations at startup.

```bash
export DATABASE_URL=jdbc:postgresql://localhost:5432/ecommerce
export DATABASE_USER=postgres
export DATABASE_PASSWORD=secret
mvn exec:java -Dexec.mainClass="com.ecommerce.support.Main"
```

Flyway migrations live in `src/main/resources/db/migration/`:

| Migration | Description |
|---|---|
| `V1__create_schema.sql` | Creates `orders`, `products`, `refunds` tables with indexes |
| `V2__seed_data.sql` | Seeds the same data as the mock repositories |

Schema notes: monetary columns (`price`, `total`) use `NUMERIC(12,2)`; `orders.items` is stored as `JSONB`.

---

## Data Model

All models are Java `record` types (immutable):

| Record | Key Fields |
|---|---|
| `Order` | `id`, `customerId`, `status`, `items (List<String>)`, `total`, `createdAt`, `estimatedDelivery` |
| `Refund` | `id`, `orderId`, `customerId`, `status`, `reason`, `createdAt` |
| `Product` | `id`, `name`, `description`, `price`, `stockQuantity`, `category` |

Order statuses: `PROCESSING`, `SHIPPED`, `DELIVERED`, `CANCELLED`
Refund statuses: `PENDING`, `COMPLETED`

### Seeded Data Reference

Both mock repositories and `V2__seed_data.sql` are seeded with the same data:

- Customers: `CUST-001`, `CUST-002`, `CUST-003`
- Orders: `ORD-001` (SHIPPED) · `ORD-002` (DELIVERED) · `ORD-003` (PROCESSING) · `ORD-004` (CANCELLED) · `ORD-005` (DELIVERED)
- Refunds: `REF-001` (COMPLETED, CUST-001) · `REF-002` (COMPLETED, CUST-002) · `REF-003` (PENDING, CUST-003)
- Products: `PROD-001` to `PROD-010` across Electronics, Accessories, Footwear, Kitchen categories

---

## Testing Approach

### Unit tests (`tools/`)

Exercise **instance methods** directly — no mocking frameworks, no Spring context, no LLM calls. Each test class:

1. Instantiates a `Mock*Repository` in `@BeforeEach`
2. Constructs the `*Tools` instance with it
3. Calls the public instance method and asserts on the returned string

Fast and fully offline. The static tool methods are not tested separately since they are thin delegators.

### Integration tests (`repository/jdbc/`)

`JdbcOrderRepositoryIT`, `JdbcProductRepositoryIT`, `JdbcRefundRepositoryIT` use **Testcontainers** to spin up a real `postgres:16` container. Each test runs `flyway.clean(); flyway.migrate()` in `@BeforeEach` for full isolation. Requires Docker at test time.

---

## Where This Architecture Applies

This codebase is a reference implementation for the following real-world scenarios:

**Customer Support Automation** — Any domain with clearly separated support intents (billing, shipping, account, technical) maps directly to this orchestrator → specialist pattern. The orchestrator becomes a router; each specialist owns its data and tools.

**Internal Enterprise Assistants** — HR bots (leave, payroll, policy sub-agents), IT helpdesks (ticketing, VPN, hardware sub-agents), or finance assistants (expenses, invoicing, approval sub-agents) all fit the same shape.

**Domain-Isolated Tool Ownership** — When different teams own different data domains, each team can own one sub-agent + tools + repository, with the orchestrator as a shared contract. Changes to the refund flow don't touch the order agent.

**Replacing Mock Repositories** — The repository interface layer makes this production-ready: the `repository/jdbc/` package already provides Spring JDBC implementations backed by PostgreSQL, selected at runtime via env vars.

**Extending with New Intents** — Adding a new support domain (e.g. shipping carrier integration) requires: a new `*Repository` interface + mock, a new `*Tools` class, a new `*Agent` factory, and one `AgentTool.create(newAgent)` line in `SupportOrchestratorAgent`.

---

## To-dos (Future Work)

- Extract IT test boilerplate into a shared base class (`AbstractJdbcRepositoryIT`)
- Replace explicit column lists in JDBC repositories (currently using `SELECT *`)
- Add a `DATABASE_URL` command in the `Commands` section once a local Docker Compose setup is added
