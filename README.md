MCP server for solo TTRPG play and novel writing (~130 tools).

## What is ChatDM?

ChatDM is a **Model Context Protocol (MCP) server** — not a web app. You use it from Cursor, Claude Desktop, or other MCP clients. It provides:

- **RPG oracles**: dice, yes/no, likelihood, narrative/scene/NPC/dungeon generators
- **Campaign journal**: characters, adventures, NPCs, locations, campaigns (local or GCS)
- **Novel writing**: book/chapter/bio cloud storage, creative prompts
- **Wryterio integration**: two-way sync with [Wryterio](https://wryterio.com/)
- **PDF rule search**: FTS5 index over licensed rulebooks (TOR, D&D 5e, Brambletrek, MLP)

```
Cursor / Claude  ──MCP──►  ChatDM (Spring Boot)  ──►  GCS / local files
                                    │
                                    └──► Wryterio API
```

## Quick start

| Mode | Command |
|------|---------|
| Local STDIO | `./mvnw spring-boot:run` |
| Local + GCS data | `SPRING_PROFILES_ACTIVE=gcs CHATDM_GCS_BUCKET=your-bucket ./mvnw spring-boot:run` |
| Cloud | Deployed to Cloud Run — see [MCP_CLOUD_SETUP.md](MCP_CLOUD_SETUP.md) |

**Client setup**: [MCP_CLOUD_SETUP.md](MCP_CLOUD_SETUP.md)

**Tool workflows (especially novel/Wryterio)**: [AGENTS.md](AGENTS.md)

**Full tool catalog**: [docs/TOOLS.md](docs/TOOLS.md)

**Contributing**: [CONTRIBUTING.md](CONTRIBUTING.md)

## Tool categories

| Category | Examples |
|----------|----------|
| RPG basics | `ChatDM_roll_dice`, `ChatDM_yes_or_no`, `ChatDM_likelihood` |
| Generators | `ChatDM_action`, `ChatDM_scene_setup`, `ChatDM_generate_npc` |
| Journal | `ChatDM_save_character`, `ChatDM_start_adventure`, `ChatDM_log_event` |
| Novel | `ChatDM_sync_book`, `ChatDM_save_book_chapter`, `ChatDM_load_book_chapter` |
| Wryterio | `ChatDM_sync_wryterio_book_to_cloud`, `ChatDM_update_wryterio_chapter` |
| Rules search | `ChatDM_list_resources`, `ChatDM_search_resource` |
| Game prompts | `dnd_start_adventure_prompt`, `tor_play_scene_prompt`, … |

## Tests

```bash
./mvnw test
```

Cloud Build runs tests before Docker deploy ([cloudbuild.yaml](cloudbuild.yaml)).

## Architecture notes

- **Profiles**: default (STDIO + local journal), `gcs` (local against cloud bucket), `cloud` (Cloud Run SSE)
- **Storage**: `JournalStorage` abstraction — [LocalJournalStorage](src/main/java/fi/celssi/chatdm/storage/LocalJournalStorage.java) or [GcsJournalStorage](src/main/java/fi/celssi/chatdm/storage/GcsJournalStorage.java)
