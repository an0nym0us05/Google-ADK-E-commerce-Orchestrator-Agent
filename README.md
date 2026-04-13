# Google ADK — E-Commerce Orchestrator Agent

A multi-agent customer support system for an e-commerce store, built with [Google Agent Development Kit (ADK)](https://google.github.io/adk-docs/) in Java. The orchestrator routes incoming customer queries to specialized sub-agents for orders, refunds, and products.

---

## Architecture

```
User
 └── SupportOrchestratorAgent  (gemini-2.5-flash)
       ├── OrderAgent           — order lookup, history, shipment tracking
       ├── RefundAgent          — refund status, new refund requests
       └── ProductAgent         — product search, details, stock availability
```

Each sub-agent is backed by tool methods that query in-memory mock repositories, making the system runnable out-of-the-box without any external database.

---

## Prerequisites

| Requirement | Version |
|-------------|---------|
| Java | 17+ |
| Maven | 3.8+ |
| Google AI Studio API key | [Get one here](https://aistudio.google.com/apikey) |

---

## Setup

### 1. Clone the repository

```bash
git clone https://github.com/an0nym0us05/Google-ADK-E-commerce-orchestrator-agent.git
cd Google-ADK-E-commerce-orchestrator-agent
```

### 2. Set your API key

```bash
# Linux / macOS
export GOOGLE_API_KEY="your-api-key-here"

# Windows (Command Prompt)
set GOOGLE_API_KEY=your-api-key-here

# Windows (PowerShell)
$env:GOOGLE_API_KEY="your-api-key-here"
```

### 3. Build the project

```bash
mvn clean package -DskipTests
```

---

## Running

```bash
mvn exec:java -Dexec.mainClass="com.ecommerce.support.Main"
```

This starts two interfaces simultaneously:

- **Dev UI** — `http://localhost:8080` (visual agent playground powered by `google-adk-dev`)
- **CLI loop** — interactive terminal chat in the same process

### CLI usage

```
E-Commerce Support Agent ready. Type 'quit' to exit.

You: hi
Agent: Hello! Welcome to our support. I can help you with:
       - Orders: track, view, or manage your orders
       - Refunds: check status or request a refund
       - Products: search products or check availability

You: what's the status of order ORD-001?
Agent: Order ORD-001 has been shipped. Estimated delivery: 2026-04-14.

You: quit
Goodbye!
```

---

## Sample Data

The mock repositories are pre-seeded with the following data:

### Orders

| Order ID | Customer | Status | Items | Total |
|----------|----------|--------|-------|-------|
| ORD-001 | CUST-001 | SHIPPED | Wireless Headphones, USB-C Cable | $129.99 |
| ORD-002 | CUST-001 | DELIVERED | Running Shoes | $89.99 |
| ORD-003 | CUST-002 | PROCESSING | Laptop Stand, Keyboard | $74.50 |
| ORD-004 | CUST-002 | CANCELLED | Smartwatch | $199.00 |
| ORD-005 | CUST-003 | DELIVERED | Coffee Maker, Coffee Beans | $55.00 |

### Customers: `CUST-001`, `CUST-002`, `CUST-003`

---

## Project Structure

```
src/
├── main/java/com/ecommerce/support/
│   ├── Main.java                        # Entry point — wires agents, starts Dev UI + CLI
│   ├── orchestrator/
│   │   └── SupportOrchestratorAgent.java
│   ├── agents/
│   │   ├── OrderAgent.java
│   │   ├── ProductAgent.java
│   │   └── RefundAgent.java
│   ├── tools/
│   │   ├── OrderTools.java
│   │   ├── ProductTools.java
│   │   └── RefundTools.java
│   ├── model/
│   │   ├── Order.java
│   │   ├── Product.java
│   │   └── Refund.java
│   └── repository/
│       ├── OrderRepository.java
│       ├── ProductRepository.java
│       ├── RefundRepository.java
│       └── mock/
│           ├── MockOrderRepository.java
│           ├── MockProductRepository.java
│           └── MockRefundRepository.java
└── test/java/com/ecommerce/support/
    ├── repository/mock/
    └── tools/
```

---

## Running Tests

```bash
mvn test
```

---

## Key Design Decisions

- **Static tool methods** — Google ADK's `FunctionTool` only supports static methods. A singleton `register()` pattern bridges instance-held repositories to static tool entry points.
- **Shared session service** — The CLI loop and Dev UI operate on separate session contexts; the CLI uses a manually created `InMemorySessionService` session while the Dev UI manages its own.
- **Runner.builder()** — Uses the non-deprecated builder API introduced in ADK 1.0.0.

---

## Dependencies

| Artifact | Version |
|----------|---------|
| `com.google.adk:google-adk` | 1.0.0 |
| `com.google.adk:google-adk-dev` | 1.0.0 |
| `org.junit.jupiter:junit-jupiter` | 5.10.1 |
