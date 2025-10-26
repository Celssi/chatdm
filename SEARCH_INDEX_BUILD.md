# ChatDM Search Index Build Guide

## Overview

ChatDM uses a pre-built SQLite FTS5 (Full-Text Search) index for fast PDF searching. This document explains how to build
and maintain the search index.

## Build Process

### Automatic Build (Recommended)

The search index is automatically built during Maven's `process-resources` phase:

```bash
mvn clean compile
```

This will:

1. Process all PDF files in `src/main/resources/pdfs/`
2. Extract text from every page
3. Build a SQLite FTS5 index with BM25 ranking
4. Save the index to `target/classes/search_index.db`

### Manual Build

To rebuild the search index without compiling:

```bash
mvn process-resources
```

Or run the indexer directly:

```bash
mvn exec:java@build-search-index
```

## Build Time

Index build time depends on PDF count and size:

- **~40 PDFs (100MB total)**: ~2-5 minutes
- Per PDF: ~5-30 seconds depending on page count
- Progress is shown in console output

## What Gets Indexed

The indexer processes these game systems:

| Game System        | Directory              | PDFs      |
|--------------------|------------------------|-----------|
| The One Ring RPG   | `pdfs/lotr/`           | ~12 books |
| D&D 5e (2024)      | `pdfs/dnd/`            | ~7 books  |
| Brambletrek        | `pdfs/brambletrek/`    | ~4 books  |
| My Little Pony RPG | `pdfs/my-little-pony/` | ~3 books  |

## Database Schema

### FTS5 Virtual Table: `pdf_search_fts`

Stores searchable content with columns:

- `game_system` - Game system ID (e.g., "the-one-ring")
- `resource_id` - Unique resource identifier
- `resource_name` - Human-readable resource name
- `page_number` - Page number (1-indexed)
- `content` - Full page text content

Uses **Porter stemming** and **unicode61** tokenizer for fuzzy matching.

### Metadata Table: `pdf_metadata`

Stores resource information:

- `resource_id` (PRIMARY KEY)
- `game_system`
- `resource_name`
- `file_path`
- `total_pages`
- `indexed_at` (timestamp)

## Search Performance

### With SQLite FTS Index (After Build)

- Search time: **<100ms** per query
- Supports: Plain text queries with BM25 ranking
- Example: `"Lindon elves"` → instant results

### Fallback (Without Index)

- Search time: **3-20 seconds** per query
- Supports: Plain text + regex patterns
- Used when: Index not built OR regex search requested

## Troubleshooting

### Index Not Found

If you see: `⚠ Warning: Could not load search index`

**Solution**: Run `mvn process-resources` to build the index

### Outdated Index

After adding/updating PDFs, rebuild the index:

```bash
mvn clean compile
```

### Build Failures

Common issues:

1. **Missing PDFs**: Ensure PDFs exist in `src/main/resources/pdfs/`
2. **Corrupted PDF**: Check console output for error messages
3. **Out of memory**: Increase Maven memory: `export MAVEN_OPTS="-Xmx2g"`

## CI/CD Integration

### GitHub Actions Example

```yaml
steps:
  - name: Build search index
    run: mvn process-resources
    
  - name: Cache search index
    uses: actions/cache@v3
    with:
      path: target/classes/search_index.db
      key: search-index-${{ hashFiles('src/main/resources/pdfs/**/*.pdf') }}
```

### Docker Build

```dockerfile
FROM maven:3.9-eclipse-temurin-25

# Copy PDFs and source
COPY src/ /app/src/
COPY pom.xml /app/

# Build index during image build
RUN mvn process-resources

# Index is now included in image
```

## File Locations

| File                                                             | Purpose                  |
|------------------------------------------------------------------|--------------------------|
| `src/main/java/fi/celssi/chatdm/indexer/PdfIndexBuilder.java`    | Index builder            |
| `src/main/java/fi/celssi/chatdm/service/SqliteSearchEngine.java` | Search engine            |
| `target/classes/search_index.db`                                 | Generated index          |
| `pom.xml`                                                        | Maven exec plugin config |

## Adding New Game Systems

To index a new game system:

1. Add PDFs to `src/main/resources/pdfs/your-game/`
2. Edit `PdfIndexBuilder.java` and add:
   ```java
   indexGameSystem(conn, "your-game", "pdfs/your-game");
   ```
3. Rebuild: `mvn process-resources`

## Performance Tips

1. **Exclude binary files**: Don't index images/fonts (already filtered)
2. **Batch processing**: Index handles batching automatically
3. **Optimize after build**: Run `INSERT INTO pdf_search_fts(pdf_search_fts) VALUES('optimize')` (automatic)

## Support

For issues or questions, see the main README or file an issue.
