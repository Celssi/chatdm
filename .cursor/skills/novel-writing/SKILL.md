---
name: novel-writing
description: Manages novels in ChatDM - sync books to cloud, character/place/item bios, creative prompts, Wryterio integration. Use when user mentions novel, book, chapter, character bio, "what happens next", "what could X say", Wryterio, or deleting book data.
---

# Novel Writing with ChatDM

## When to Use

- User provides book markdown to sync
- User wants to add/update character, place, or item bios
- User asks "what could happen in the next few paragraphs" or "what could Character X say next"
- User mentions Wryterio or syncing from Wryterio
- User wants to delete a bio, chapter, or entire book

## Workflow

### Syncing a Book

1. **From markdown**: Use `ChatDM_sync_book` with `bookMarkdown` and optional `bookTitle`
2. **From Wryterio**: Use `ChatDM_list_wryterio_books` to discover books, then **`ChatDM_sync_wryterio_book_to_cloud`** with `bookId`. This saves chapters to storage. Do NOT use `ChatDM_fetch_wryterio_book` when user wants to load/lataa a book—fetch only returns content, it does not save.
3. **List chapters** (before sync or single chapter): `ChatDM_list_wryterio_chapters` — returns index and title for each chapter.
4. **Single chapter from Wryterio**: `ChatDM_fetch_wryterio_chapter` — fetches one chapter by index (1-based). Use when book is too large or user wants only one chapter.

### Writing to Wryterio (two-way integration)

- **Create book**: `ChatDM_create_wryterio_book` — title
- **Get/update metadata**: `ChatDM_get_wryterio_book`, `ChatDM_update_wryterio_book` — title, author, description
- **Add/update chapters**: `ChatDM_add_wryterio_chapter`, `ChatDM_update_wryterio_chapter` — content as markdown; supports title, description, targetWordCount. **CRITICAL**: When updating only metadata (title, description, targetWordCount), do NOT pass content—omit it entirely. Passing empty content will overwrite and clear the chapter text.
- **Read chapter metadata**: `ChatDM_get_wryterio_chapter` — returns title, description, targetWordCount
- **Story elements**: `ChatDM_list_wryterio_story_elements`, `ChatDM_add_wryterio_story_element`, `ChatDM_update_wryterio_story_element`, `ChatDM_delete_wryterio_story_element`
- **Plot timeline (Juonen aikajana)**: `ChatDM_get_wryterio_plot_timeline`, `ChatDM_update_wryterio_plot_timeline` — Save the Cat structure

### Managing Bios

- **Save**: `ChatDM_save_book_bio` — bookName, bioType (character/place/item), name, bio
- **List**: `ChatDM_list_book_bios` — bookName, optional bioType filter
- **Load**: `ChatDM_load_book_bio` — bookName, bioType, name
- **Delete**: `ChatDM_delete_book_bio` — bookName, bioType, name

### Creative Prompts

- **What happens next**: `novel_what_happens_next_prompt` — pass recentText, optional bookName/chapterIndex. Agent loads chapter with `ChatDM_load_book_chapter` first, then generates suggestions.
- **Character dialogue**: `novel_character_dialogue_prompt` — characterName, sceneContext, optional characterBio (load with `ChatDM_load_book_bio`)

### Wryterio Token

Token comes from MCP config header `X-Wryterio-Token`. No need to pass in chat. If header not set, pass `wryterioToken` explicitly.

### Delete Tools

- `ChatDM_delete_book_bio` — one bio
- `ChatDM_delete_book_chapter` — one chapter
- `ChatDM_delete_book` — entire book and all chapters/bios
