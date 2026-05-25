# Contributing to ChatDM

ChatDM is a Spring Boot MCP server exposing tools for solo TTRPG play and novel writing.

## Prerequisites

- Java 25
- Maven (wrapper included)
- For cloud-backed local dev: `gcloud auth application-default login`

## Local development

### STDIO MCP (default)

```bash
./mvnw spring-boot:run
```

Configure your MCP client to run the JAR with STDIO transport. See [MCP_CLOUD_SETUP.md](MCP_CLOUD_SETUP.md).

### GCS-backed local dev

```bash
SPRING_PROFILES_ACTIVE=gcs CHATDM_GCS_BUCKET=your-bucket ./mvnw spring-boot:run
```

## Running tests

```bash
./mvnw test
```

Integration tests that require PDFs/search index skip automatically when fixtures are unavailable.

JaCoCo report: `target/site/jacoco/index.html`

## Adding a new game system (PDF search)

1. Upload PDFs to GCS under `pdfs/{game-system-id}/`
2. Register the system in [GameResourceOracle.java](src/main/java/fi/celssi/chatdm/service/GameResourceOracle.java)
3. Add resource entries to [PdfIndexBuilder.java](src/main/java/fi/celssi/chatdm/indexer/PdfIndexBuilder.java)
4. Rebuild search index — see [SEARCH_INDEX_BUILD.md](SEARCH_INDEX_BUILD.md)

## Adding MCP tools

1. Create or extend an `@Service` class with `@Tool`-annotated methods
2. Register the bean in [ChatDmApplication.java](src/main/java/fi/celssi/chatdm/ChatDmApplication.java) `McpConfig.toolCallbackProvider`
3. Return `"Error: ..."` strings for validation failures (MCP convention)
4. Add unit tests with [InMemoryJournalStorage](src/test/java/fi/celssi/chatdm/storage/InMemoryJournalStorage.java) where applicable
5. Regenerate [docs/TOOLS.md](docs/TOOLS.md) (see script in repo or run the Python generator from README)

## Adding game prompts

1. Add markdown under `src/main/resources/prompts/{system}/`
2. Load via [PromptLoader](src/main/java/fi/celssi/chatdm/service/shared/PromptLoader.java)
3. Keep tool names in prompts aligned with registered `@Tool` names — see [PromptToolNamesTest](src/test/java/fi/celssi/chatdm/service/PromptToolNamesTest.java)

## Project layout

```
src/main/java/fi/celssi/chatdm/
├── service/           # Oracle tools (dice, scene, PDF search, novel, prompts)
├── service/journal/   # Campaign journal tools
├── service/wryterio/  # Wryterio integration
├── service/shared/    # PromptLoader, guards, helpers
├── storage/           # JournalStorage implementations
└── config/            # Web/CORS/user filters (cloud profile)
```

See [AGENTS.md](AGENTS.md) for AI-assistant tool workflows.
