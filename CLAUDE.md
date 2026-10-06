# Project Rules

## Workflow

- One session = one issue. Start each issue with a fresh session (`/clear`).
- Scope discipline: only touch files related to the current issue.

## Code style (ponytail full by default)

- Simplest solution that actually works. YAGNI: no speculative features, no "for later" scaffolding.
- No abstraction with a single caller, no new dependency for a few lines of code.
- Prefer stdlib / platform-native features over custom code.
- Deletion over addition, boring over clever.

## Implementation (TDD)

- Red → Green → Refactor: write a failing test first, then the minimal code to pass.
- Run the full test suite before claiming completion. A fix without a passing test is not done.

## Review

- Self-review the diff before finishing: check scope compliance and remove dead code.
- Report what was done concisely. No verbose explanations unless asked.

## Stack

- Java 17, Spring Boot 4.1.1, Spring Data JPA, PostgreSQL, Gradle
- Test: `./gradlew test` (test profile auto-activated)
- Build: `./gradlew clean bootJar`
- Local DB: `docker compose up -d`
