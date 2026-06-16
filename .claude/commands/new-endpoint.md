Scaffold a complete new domain endpoint in the jetlagtracker backend for: $ARGUMENTS

Follow the exact patterns from existing domain packages (team/, station/, challenge/). Create all files in a new package `com.nilskulawiak.jetlagtracker.<domain>/` where `<domain>` is the lowercase version of the entity name.

### Files to create

**`<Domain>.java`** — JPA entity:
- `@Entity @Getter @Setter`
- `@Id @GeneratedValue private UUID id`
- `@ManyToOne private Game game`
- Additional fields with `@Column(nullable = false)` as appropriate for the entity

**`<Domain>Repository.java`** — `public interface <Domain>Repository extends JpaRepository<<Domain>, UUID>` with at minimum:
- `List<<Domain>> findByGame(Game game)`
- `void deleteByGame(Game game)`

**`<Domain>Service.java`** — `@Service @RequiredArgsConstructor`:
- Inject `<Domain>Repository`, `GameRepository`, `GameActionService`
- `create<Domain>(UUID gameId, Create<Domain>Request)` — validate game is `CREATED`, build entity, save, log action, return response
- `delete<Domain>(UUID gameId, UUID entityId)` — validate ownership and game status, delete, log action
- `patch<Domain>(UUID gameId, UUID entityId, Patch<Domain>Request)` — validate ownership and game status, apply non-null fields, save, return response
- Every method validates that the entity belongs to the given game
- Call `gameActionService.log(game, GameActionType.<DOMAIN>_CREATED, ...)` on every state change

**`<Domain>Controller.java`** — `@RestController @RequestMapping("/games/{gameId}/<domains>") @RequiredArgsConstructor`:
- `POST /` → `@ResponseStatus(HttpStatus.CREATED)` returns `<Domain>Response`
- `DELETE /{entityId}` → `@ResponseStatus(HttpStatus.NO_CONTENT)` returns `void`
- `PATCH /{entityId}` → returns `<Domain>Response`
- Use `@Valid` on `@RequestBody` for POST

**`Create<Domain>Request.java`** — Java record. Use `@NotBlank` on Strings, `@NotNull` on other required fields.

**`Patch<Domain>Request.java`** — Java record with all fields nullable (null = no change, consistent with patch semantics).

**`<Domain>Response.java`** — record with a static factory:
```java
public static <Domain>Response from(<Domain> entity) { ... }
```
Always include `id` (UUID) and `gameId` (entity.getGame().getId()).

**`GameActionType.java`** — add `<DOMAIN>_CREATED` (and any other action types needed, e.g. `<DOMAIN>_DELETED`).

### After creating the files

Ask whether to also add the corresponding TypeScript types to `../jetlagtracker-mobile/types/game.ts` and the API functions to `../jetlagtracker-mobile/api/gameApi.ts`, following the same patterns as the existing entries there.
