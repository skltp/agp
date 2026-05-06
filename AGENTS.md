# AGENTS.md — AGP (Aggregeringsplattform)

## Architecture Overview

AGP is a **plugin-based SOAP aggregation platform** built with Spring Boot 3.5 + Apache Camel 4.x + CXF. It exposes SOAP endpoints for Swedish healthcare service contracts, fans out requests to multiple producers, and aggregates responses.

**Data flow:** Consumer → AGP inbound CXF endpoint → EI (Engagement Index) lookup → parallel producer calls via VP (Virtuell Plattform) → aggregate responses → return to consumer.

### Module Structure
- **agp-core** — Plugin API: `AgpServiceFactory<T>` interface and `AgServiceFactoryBase<E,T>` abstract class. Plugins implement these.
- **agp-application** — The runtime: Camel routes (`AgpRoute`, `AgpServiceRoutes`), processors, TAK cache, health indicators, validation logging, and Hawtio console.
- **agp-schemas** — WSDL/XSD for EI FindContent interaction.
- **agp-teststub** — Mock EI and TAK for local development/tests.
- **agp-test-service** / **agp-test-core** — In-tree test plugin (GetLaboratoryOrderOutcome) and test helpers.
- **agp-init** — Kubernetes init-container for Helm deployments.
- **report** — JaCoCo aggregate coverage report module (no source code).

### Plugin Repositories (separate repos, same workspace)
Each aggregating service (e.g., `GetAggregatedCareDocumentation.v3`, `GetAggregatedRequestExemptionStatuses`) is a plugin with four submodules:
- `*-schemas` — Generated JAXB types from WSDL (via `cxf-xjc-plugin`).
- `*-main` — Contains `*AgpServiceConfiguration` (extends `AgpServiceConfiguration`) and `*AgpServiceFactoryImpl` (extends `AgServiceFactoryBase`).
- `*-teststub` — Mock producers for integration testing.
- `*-runner` — Standalone Spring Boot app wrapping AGP + this plugin for isolated testing.

Plugins inherit `se.skltp.agp:agp-parent` as Maven parent; they reference `agp-core` at compile scope and the `*-all.jar` fat assembly is loaded at runtime via `-Dloader.path`.

## Build & Run

```powershell
# Build everything (from agp root):
.\mvnw clean install

# Run teststub (terminal 1):
cd agp-teststub; ..\mvnw spring-boot:run

# Run application (terminal 2):
cd agp-application; ..\mvnw spring-boot:run

# Run with plugins:
java -Dloader.path=C:\path\to\plugins\ -jar agp-application\target\agp-application-4.2.0-SNAPSHOT-exec.jar
```

For plugin repos: `mvn clean install` at root. The `-main` module produces a `*-all.jar` (assembly) which is the deployable plugin artifact.

## Key Conventions

- **Java 17**, compiled with `maven.compiler.release=17`.
- **Logging:** Log4j2 (NOT Logback). `spring-boot-starter-logging` is globally excluded. Use `@Log4j2` from Lombok. Some CXF interceptor classes use `@Slf4j` (routed via `log4j-slf4j2-impl`); prefer `@Log4j2` for new code.
- **Lombok:** Used throughout (`@Data`, `@Log4j2`, `@RequiredArgsConstructor`). Config at `lombok.config`.
- **License headers:** All `.java` files require LGPL-3.0 headers. Run `mvn license:check` / `mvn license:format` with profile `-Plicense`.
- **Test naming:** Unit tests `*Test.java` (surefire), integration tests `*IT.java` (failsafe). Tests run alphabetically.
- **Dependency versions:** ALL versions declared in parent POM `<properties>` + `<dependencyManagement>`. Child modules never declare versions.

## Creating a New Plugin

1. Extend `AgpServiceConfiguration` with `@Configuration @ConfigurationProperties(prefix = "yoursvcname.v1")`.
2. Set service name, WSDL paths, CXF classes, EI domain/categorization, TAK contract, and `serviceFactoryClass`.
3. Extend `AgServiceFactoryBase<RequestType, ResponseType>` implementing:
   - `getPatientId(req)` — extract patient ID from request.
   - `getSourceSystemHsaId(req)` — extract optional source system filter.
   - `aggregateResponse(List<ResponseType>)` — merge producer responses.
4. Package as `*-all.jar` using `maven-assembly-plugin`.

See `GetAggregatedCareDocumentation-v3-main` for a complete reference implementation.

## Testing Patterns

- Unit tests for plugins test the three factory methods independently: `CreateFindContent`, `CreateRequestList`, `CreateAggregatedResponse`.
- Integration tests use `agp-application` test-jar (provides Camel test infrastructure) + `agp-teststub` (mocks EI/TAK).
- Coverage: activate profile `test-coverage` for JaCoCo (`mvn verify -Ptest-coverage`).

## Configuration

Key runtime properties (in `application.properties` or externalized):
- `ei.findContentUrl` / `ei.logicalAddress` — Engagement Index connection.
- `ei.senderId` / `ei.connectTimeout` / `ei.receiveTimeout` / `ei.useAyncHttpConduit` — EI call tuning.
- `vp.instanceId` / `vp.defaultServiceURL` / `vp.defaultReceiveTimeout` / `vp.defaultConnectTimeout` — Producer call defaults.
- `vp.useAyncHttpConduit` — Enable async HTTP conduit for VP calls.
- `aggregate.timeout` — Overall aggregation timeout (ms).
- `takcache.endpoint.address` — TAK (service routing) cache source.
- Per-service overrides via `@ConfigurationProperties` prefix (e.g., `getaggregatedcaredocumentation.v3.*`).

### Validation Logging
- `vp.validationLog.services` — Set of service names to enable schema validation logging.
- `vp.validationLog.interval` — Interval (ms) between validation log summaries (default: 60000).
- `vp.validationLog.hashStrategy` — Hashing for validation messages: `sha256` (default, secure) or `noop` (plain text, dev only).
- Per-service: set `enableSchemaValidation=true` in `AgpServiceConfiguration` to activate inbound SOAP schema validation.

### Hawtio Console
- `hawtio.authentication.enabled` — Enable/disable Hawtio web console authentication (default: `false`).
- `hawtio.external.loginfile` — Path to external JAAS properties file for user credentials.
- Exposed via actuator at `/actuator/hawtio` (see `management.endpoints.web.exposure.include`).

See `doc/config/config.md` for full reference.

