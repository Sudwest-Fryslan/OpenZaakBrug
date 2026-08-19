# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this is

Open Zaakbrug (`zds-to-zgw`) is a Spring Boot (Java 11) SOAP translation service. It sits between legacy
ZDS/StUF-based "zaaksysteem" applications (Dutch municipal case-management systems) and a modern ZGW-API
backend (e.g. [OpenZaak](https://openzaak.org/)), letting old applications keep speaking the old StUF/ZDS
protocol while the actual case/document storage moves to the new ZGW standard. Domain and code comments are
largely in Dutch.

The service supports three modes, selected purely via URL path prefix / config, not separate deployments:
- **proxy** — passes SOAP messages straight through to the legacy zaaksysteem (used to validate traffic
  keeps flowing before cutting over).
- **translate** — converts ZDS/StUF SOAP requests to ZGW API calls and translates the ZGW response back to
  ZDS/StUF XML.
- **replicate** — does both: translates to ZGW *and* mirrors the same message to the legacy system, and can
  lazily copy a zaak/document from the legacy system into ZGW on first reference. Used during migration so
  the two systems stay in sync until a full data migration is done.

See `docs/Workings of Replication.md` and `docs/ReplicationHandler.md` for the migration-stage rationale.

## Build, test, run

```
./mvnw install -Dmaven.javadoc.skip=true    # build (also runs tests)
./mvnw test                                  # run tests only
./mvnw test -Dtest=ConverterFactoryTests      # run a single test class
./mvnw test -Dtest=ConverterFactoryTests#someMethod   # run a single test method
./mvnw spring-boot:run                       # run locally (uses src/main/resources/application.properties + config.json)
```

- Java 11, Maven wrapper (`mvnw`/`mvnw.cmd`) — don't assume a global Maven install.
- CI (`.github/workflows/main.yml`) runs `mvn -B package`, then builds/starts the Docker image
  (`docker_start.sh`) as an integration smoke test, then publishes to Docker Hub / Nexus on push to
  `master`/`release-*`.
- Local config: copy `application.properties_example`-style files and `config.json_example` to
  `application.properties` / `config.json` (both already exist checked in for local dev; see
  `docs/Installing Open Zaakbrug.md` and `docs/Configuratie.md`).
- `.editorconfig` sets tabs for most files but **spaces, 4-wide indent for `*.java`**.

### Ladybug regression tests

`src/test/java/.../LadybugTests.java` replays recorded SOAP request/response fixtures stored as XML under
`src/test/resources/ladybug/*/`. These run as part of the normal test suite (`@SpringBootTest`) and compare
actual vs. expected XML reports; a diff fails the build. If a change intentionally alters behavior:
1. Run the app locally and open `http://localhost:8080/debug` → **Test** tab → **Run**, refresh until done.
2. **Compare** to inspect the diff (values ignored during comparison show as `IGNORED`, configured via
   `src/main/resources/transform-ladybug-report.xslt`).
3. **Replace** to overwrite the fixture under `src/test/resources/ladybug/` with the new actual output.

Full details: `docs/LadybugTests.md`.

## Architecture

Everything hangs off one Spring `@Controller`, `SoapController` (`controller/SoapController.java`), which
exposes a single catch-all endpoint:

```
POST /{modus}/{version}/{protocol}/{endpoint}   (SOAPAction header selects the operation)
```

Request flow for every incoming SOAP call:

1. **`ConverterFactory`** (`converter/ConverterFactory.java`) looks up a `Translation` config entry by
   matching the request path + SOAPAction against `config.json`'s `translations` array, then instantiates
   the `implementation` class named there **via reflection** (constructor
   `(RequestResponseCycle, Translation, ZaakService)`). This is how `proxy` / `translate` / `replicate`
   variants of the same operation resolve to different `Converter` subclasses without any `if/switch` in
   Java — routing lives entirely in `config.json`.
2. **`RequestHandlerFactory`** wraps that `Converter` in a `RequestHandler` — again instantiated by
   reflection, class name taken from `config.json`'s top-level `requestHandlerImplementation`
   (currently `requesthandler.impl.LoggingRequestHandler`, aka the "ReplicationRequestHandler" in the docs).
   This is where request/response logging to the DB (`REQUEST_RESPONSE_CYCLE`, `ZGW_REQUEST_RESPONSE_CYCLE`,
   `ZDS_REQUEST_RESPONSE_CYCLE` tables) and replication-to-legacy behavior live — kept separate from
   `Converter` so translation logic stays independent of persistence/logging concerns.
3. `handler.execute()` runs the actual conversion; the Ladybug `Debugger` wraps the whole call
   (`startpoint`/`infopoint`/`endpoint`/`abortpoint`) to record a replayable report for the debug UI and the
   Ladybug regression tests.

Key packages (`src/main/java/nl/haarlem/translations/zdstozgw/`):
- `converter/` — the `Converter` abstract base + `impl/proxy`, `impl/translate`, `impl/replicate`,
  `impl/emulate` (one class per StUF operation, e.g. `CreeerZaakTranslator`, `CreeerZaakReplicator`).
  `replicate/*Replicator` classes typically extend/reuse the matching `translate/*Translator` and add the
  legacy-system mirroring + on-the-fly copy behavior.
- `requesthandler/` — `RequestHandler` abstraction + `impl/BasicRequestHandler` (no extra logic) and
  `impl/LoggingRequestHandler` + `impl/logging/*` (DB persistence of request/response cycles).
- `translation/zds/` — StUF/ZDS-side XML models (JAXB, under `model/`, `model/namespace/`), `ZaakService`,
  and the legacy SOAP client (`client/`).
- `translation/zgw/` — ZGW API-side models and `ZGWClient`/`ZgwAuthorization` (the HTTP client talking to
  OpenZaak-style ZGW APIs). `ZgwAuthorization.supportsExpand()` auto-detects whether the backend supports
  the ZGW `expand` query parameter (added in Zaken API 1.5 / Documenten API 1.4, i.e. Open Zaak ≥1.11.0) by
  reading the `API-version` response header from the catalogus lookup every request already makes — no
  config needed. `ZGWClient` only sends `expand=...` when this is true; `ZaakService` falls back to the
  pre-expand per-item lookups (`getZaakTypeByUrl`, `getRollenByZaakUrl`, etc.) when it's false. This matters
  because an unsupported `expand` param isn't silently ignored — older Open Zaak rejects the whole request
  with HTTP 400. See `ExpandFallbackLiveTest` (`@Ignore`d, needs a local pre-1.11.0 Open Zaak instance) for
  how to verify this against a real old backend.
- `config/` — `ConfigService` loads/parses `config.json` into `config/model/*` (this is what
  `ConverterFactory`/`RequestHandlerFactory` query); also JWT/auth config (`StufHeader`, `SpringContext`).
- `debug/` — Ladybug integration (`Debugger`, `DebugServlet`, `Rerunner`) powering the `/debug` UI and the
  recorded XML test fixtures.

Because both factories select implementations by fully-qualified class name from `config.json`, adding a new
translated operation or a new proxy/translate/replicate variant of an existing one is primarily a config +
new-class exercise, not a routing-code change — check `config.json_example` for the pattern before wiring up
a new operation.
