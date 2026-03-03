MCP for solo TTRPGs

## Tool Overview

ChatDM exposes MCP tools for solo RPG play and novel writing. Main categories:

- **RPG**: Dice (`ChatDM_roll_dice`), yes/no (`ChatDM_yes_or_no`), narrative generators
- **Journal**: Characters, adventures, NPCs, locations, campaigns
- **Novel**: Books, chapters, bios (character/place/item) in cloud storage
- **Wryterio**: Integration with [Wryterio](https://wryterio.com/) novel-writing app
- **Search**: PDF resource lookup and full-text search

For detailed tool usage and workflows—especially novel-related tools—see **[AGENTS.md](AGENTS.md)**.

**Configuration**: See [MCP_CLOUD_SETUP.md](MCP_CLOUD_SETUP.md) for connecting Cursor, Claude, or other MCP clients.

## Local development with GCS

PDFs, search index, and journal data live in Google Cloud Storage. To run locally against cloud data:

```bash
SPRING_PROFILES_ACTIVE=gcs CHATDM_GCS_BUCKET=your-bucket ./mvnw spring-boot:run
```

Or with `GOOGLE_CLOUD_PROJECT` set (uses `{project}-chatdm-resources` bucket). Requires `gcloud auth application-default login`.