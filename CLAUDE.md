# Jetlag Tracker — Backend

Spring Boot 4 REST API tracking live game state for Jet Lag: The Game (Taiwan: Rail Rush season).

## Commands

```bash
# Run (requires DB_PASSWORD env var and PostgreSQL on :5432)
./mvnw spring-boot:run

# Run with Docker (no local Java/PostgreSQL needed)
docker compose up --build

# Test
./mvnw verify

# Build jar only
./mvnw package -DskipTests
```

Database: PostgreSQL at `localhost:5432/jetlag`, user `postgres`, password from `$DB_PASSWORD`.  
`SPRING_DATASOURCE_URL` overrides the whole JDBC URL (used by Docker Compose).  
Schema is managed by Hibernate `ddl-auto=update` — no migration files.

## Project Structure

```
src/main/java/com/nilskulawiak/jetlagtracker/
├── game/        Game entity, lifecycle (CREATED→STARTED→DONE), state aggregation
├── team/        Team entity and chip balance
├── station/     Station entity and chip placement logic
├── challenge/   Challenge entity, attempt tracking, all reward mechanics
├── action/      Append-only game action log (GameActionService writes, never deletes)
├── preset/      Loads JSON preset files from src/main/resources/presets/
├── config/      CORS (WebConfig)
└── common/      GlobalExceptionHandler → ErrorResponse
```

Each domain package follows the same layout: `Entity`, `Repository`, `Service`, `Controller`, `*Request` records, `*Response` records.

## Domain Rules

### Chip placement (`StationService`)
- After placement, a team's total on a station must exceed the maximum opponent total by **at least 1 and at most 5**. Ties are intentionally forbidden — you can only place chips if doing so puts you strictly in the lead.
- Chips placed are deducted from the team's `availableChips` balance.
- A team cannot place chips they don't have.

### Challenge lifecycle
```
CREATED → AVAILABLE → DONE
```
- Only `AVAILABLE` challenges can be started, completed, or failed.
- A team must call `/start` before `/complete` or `/fail` (creates a `ChallengeAttempt` with `IN_PROGRESS`).
- A team can only have one attempt per challenge.
- When a challenge reaches `DONE`, a random `CREATED` challenge is promoted to `AVAILABLE`.

### Challenge types (reward logic in `ChallengeService`)
| Type | Reward on complete |
|---|---|
| `CHIPS` | `+reward` chips to team |
| `MULTIPLIER` | `availableChips = availableChips * reward / 100` |
| `STEAL` | Steals `enemyTeam.availableChips * reward / 100` chips from enemy (requires `enemyTeamId` in request) |
| `CALL_YOUR_SHOT` | Team provides `callShot` when calling `/complete`; earns `callShot × reward` chips |

### Failing a challenge
- Marks the attempt `FAILED` and increases the challenge's `reward` by 50% (`Math.ceil(reward * 1.5)`).
- Challenge becomes `DONE` and a replacement is surfaced only when **all teams** have failed it.

## Conventions
- Lombok everywhere (`@Data`, `@RequiredArgsConstructor`, etc.) — no manual getters/setters.
- Request bodies are Java records with Bean Validation annotations.
- Services own all business logic; controllers only delegate and return HTTP responses.
- `GameActionService.log()` is called at the end of every state-changing operation.
- Tests: unit tests with Mockito (`*ServiceTest`), integration tests with Spring context (`*IntegrationTest`).
