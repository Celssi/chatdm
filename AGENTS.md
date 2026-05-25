# ChatDM - AI Assistant Guide

ChatDM is an MCP (Model Context Protocol) server that exposes tools for solo TTRPG play and novel writing. This guide helps AI assistants understand how to use the tool ecosystem effectively.

## Connection

- **Configuration**: See [MCP_CLOUD_SETUP.md](MCP_CLOUD_SETUP.md) for Cursor, Claude Desktop, and cloud connection details.
- **Cloud URL**: https://chatdm-334017779319.europe-north1.run.app

---

## Tool Categories

| Category | Purpose | Key Tools |
|----------|---------|-----------|
| **RPG / Basic** | Dice rolls, yes/no questions, likelihood | `ChatDM_roll_dice`, `ChatDM_yes_or_no`, `ChatDM_likelihood` |
| **Narrative** | Action, subject, complication generators | `ChatDM_action`, `ChatDM_complication`, `ChatDM_random_event` |
| **Scene** | Location, atmosphere, weather, time | `ChatDM_scene_setup`, `ChatDM_location_type`, `ChatDM_atmosphere` |
| **NPC** | Behavior, appearance, personality | `ChatDM_generate_npc`, `ChatDM_npc_behavior` |
| **Dungeon** | Rooms, treasure, traps, direction | `ChatDM_dungeon_room`, `ChatDM_treasure` |
| **Conversation** | Topic, mood, intent | `ChatDM_conversation_setup` |
| **Cards** | Playing cards, tarot | `ChatDM_draw_playing_cards`, `ChatDM_draw_tarot_cards` |
| **Journal** | Characters, adventures, NPCs, locations, campaigns | `ChatDM_save_character`, `ChatDM_start_adventure`, `ChatDM_log_event`, delete/update tools, etc. |
| **Novel** | Books, bios, chapters (cloud storage) | `ChatDM_sync_book`, `ChatDM_save_book_chapter`, `ChatDM_load_book_chapter`, `ChatDM_save_book_bio` |
| **Wryterio** | Novel writing app integration | `ChatDM_list_wryterio_books`, `ChatDM_sync_wryterio_book_to_cloud`, etc. |
| **Search** | PDF resources, rules lookup | `ChatDM_list_resources`, `ChatDM_search_resource` |
| **Prompts** | Game-specific and novel prompts | `novel_what_happens_next_prompt`, `tor_play_scene_prompt`, etc. |

---

## Novel Tools (Main Focus)

### Sync vs Fetch

| Tool | Behavior |
|------|----------|
| `ChatDM_fetch_wryterio_book` | Returns raw markdown **only**. Does **NOT** save to storage. |
| `ChatDM_sync_wryterio_book_to_cloud` | Fetches from Wryterio **and** saves chapters, metadata, and story elements to cloud. Use when user wants to "load", "sync", or "save" a book. |

**Rule**: When user says "load my book", "lataa kirja", "sync from Wryterio", use `ChatDM_sync_wryterio_book_to_cloud`. Do **NOT** use `ChatDM_fetch_wryterio_book` for loading—it only returns content and does not persist.

### Book Workflow (Markdown)

1. Sync: `ChatDM_sync_book` — pass `bookMarkdown` (H2 `## ` for chapter splits) and optional `bookTitle`
2. List: `ChatDM_list_books` — discover available books
3. Load: `ChatDM_load_book_chapter` — get chapter content by `bookName` and `chapterIndex` (1-based)
4. Save one chapter: `ChatDM_save_book_chapter` — update a single chapter without re-syncing the whole book
5. Append chapter: `ChatDM_append_book_chapter` — add a new chapter with the next index

### Bio Workflow

Bios are character, place, or item notes stored per book.

1. List: `ChatDM_list_book_bios` — `bookName`, optional `bioType` (character/place/item)
2. Load: `ChatDM_load_book_bio` — before creative prompts, load bio for context
3. Save: `ChatDM_save_book_bio` — `bookName`, `bioType`, `name`, `bio`

### Creative Prompts

- **What happens next**: Call `ChatDM_load_book_chapter` first to get `recentText`. Then call `novel_what_happens_next_prompt` with that text. Returns a prompt string—pass to LLM.
- **Character dialogue**: Call `ChatDM_load_book_bio` first if character has a bio. Then call `novel_character_dialogue_prompt` with `characterName`, `sceneContext`, and optional `characterBio`. Returns prompt string for LLM.

### Wryterio Token

Required for Wryterio tools. Sources:

- **Header**: `X-Wryterio-Token` in MCP config (preferred)
- **Parameter**: `wryterioToken` passed explicitly when header is not set

### Data Loss Prevention

When updating Wryterio chapter **metadata only** (title, description, targetWordCount):

- **Never** pass the `content` parameter.
- **Never** pass `content: null`, `content: ""`, or `content: "null"` — these overwrite and destroy chapter text.
- Omit `content` entirely when updating only metadata.
- Use `ChatDM_get_wryterio_chapter` before updating if unsure.

### Chapter Metadata vs Content

| Tool | Returns |
|------|---------|
| `ChatDM_get_wryterio_chapter` | Metadata (title, description, targetWordCount) |
| `ChatDM_fetch_wryterio_chapter` | Chapter content (markdown) |

---

## Delete Tools

| Tool | Scope |
|------|-------|
| `ChatDM_delete_book_bio` | Single bio |
| `ChatDM_delete_book_chapter` | Single chapter |
| `ChatDM_delete_book` | Entire book and all chapters/bios |
