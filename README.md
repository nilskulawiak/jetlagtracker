# Jetlag Tracker — Backend

A Spring Boot REST API for tracking live game state in [Jet Lag: The Game](https://en.wikipedia.org/wiki/Jet_Lag:_The_Game) — specifically modelled on the [Taiwan: Rail Rush](https://jetlag.fandom.com/wiki/Taiwan:_Rail_Rush) season.

## What is Jet Lag: The Game?

Jet Lag is a travel competition show where two teams race across a country by train, claiming stations and completing challenges to earn chips. The team that controls the most stations wins.

This backend tracks all of that state — which stations each team owns, how many chips they hold, which challenges are available, and a full action log — so that both teams always have an accurate picture of the current game, regardless of where they are.

## Features

- **Game presets** — load a pre-configured map (stations, coordinates, challenges) from a JSON file; includes a Taiwan Rail Rush preset
- **Multi-team support** — any number of teams per game
- **Station chip placement** — enforces that the placing team's total must exceed the opponent's by at least 1 and at most 5 (ties not allowed); deducts chips from the placing team's balance
- **Challenge lifecycle** — challenges move through `CREATED → AVAILABLE → DONE`; a team must explicitly start a challenge before completing or failing it; when a challenge is resolved, replacement challenges are surfaced automatically
- **Challenge types** — `CHIPS` (flat reward), `MULTIPLIER` (scales existing balance), `STEAL` (transfers chips from opponent), `CALL_YOUR_SHOT` (team declares a count when completing; earns `callShot × reward` chips)
- **Action log** — append-only audit trail of every game event (chip placements, challenge outcomes, etc.)
- **Game state endpoint** — single endpoint returning the complete current state: teams, stations, chip counts, and active challenges

> **Scope note:** In the real show, teams can only pass through a station they own and can only attempt a challenge if they are physically at that location. Location tracking is intentionally out of scope for this app.

## Tech Stack

- Java 25, Spring Boot 4
- Spring Data JPA + PostgreSQL
- Bean Validation (`jakarta.validation`)
- Lombok
- Maven

## Getting Started

Choose one of the two options below.

### Option 1: Docker (recommended)

No Java or PostgreSQL installation required. Create a `.env` file in the project root with a password of your choice:

```
DB_PASSWORD=yourpassword
```

Then:

```bash
docker compose up --build
```

### Option 2: Manual

#### Prerequisites

- Java 25+
- PostgreSQL running locally on port 5432 with a database named `jetlag`

#### Configuration

The application reads the database password from an environment variable. Set it before running:

```powershell
# PowerShell (current session)
$env:DB_PASSWORD = "your_password"
```

Or permanently:

```powershell
[System.Environment]::SetEnvironmentVariable("DB_PASSWORD", "your_password", "User")
```

#### Run

```bash
./mvnw spring-boot:run
```

---

The API will be available at `http://localhost:8080`.

## API Overview

### Presets
| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/presets` | List available game presets |

### Games
| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/games` | Create a game manually |
| `POST` | `/games/from-preset` | Create a game from a preset |
| `POST` | `/games/{gameId}/start` | Start a game |
| `PATCH` | `/games/{gameId}` | Update game settings |
| `DELETE` | `/games/{gameId}` | Delete a game and all its data |
| `GET` | `/games` | List all games |
| `GET` | `/games/{gameId}/state` | Full game state (teams, stations, challenges) |
| `GET` | `/games/{gameId}/actions` | Action log |

### Teams
| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/games/{gameId}/teams` | Add a team |
| `PATCH` | `/games/{gameId}/teams/{teamId}` | Update a team |
| `DELETE` | `/games/{gameId}/teams/{teamId}` | Delete a team |

### Stations
| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/games/{gameId}/stations` | Add a station |
| `PATCH` | `/games/{gameId}/stations/{stationId}` | Update a station |
| `DELETE` | `/games/{gameId}/stations/{stationId}` | Delete a station |
| `POST` | `/games/{gameId}/stations/{stationId}/chips` | Place chips on a station |

### Challenges
| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/games/{gameId}/challenges` | Add a challenge |
| `PATCH` | `/games/{gameId}/challenges/{challengeId}` | Update a challenge |
| `DELETE` | `/games/{gameId}/challenges/{challengeId}` | Delete a challenge |
| `POST` | `/games/{gameId}/challenges/{challengeId}/start` | Start a challenge |
| `POST` | `/games/{gameId}/challenges/{challengeId}/complete` | Mark a started challenge as completed by a team |
| `POST` | `/games/{gameId}/challenges/{challengeId}/fail` | Mark a started challenge as failed by a team |

## Project Structure

```
src/main/java/com/nilskulawiak/jetlagtracker/
├── game/          Game entity, lifecycle management, state aggregation
├── team/          Team entity and chip balance management
├── station/       Station entity and chip placement logic
├── challenge/     Challenge entity, attempt tracking, reward mechanics
├── action/        Append-only game action log
├── preset/        JSON-based game preset loading
├── config/        CORS configuration
└── common/        Shared exception handling
```
