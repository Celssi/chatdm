# ChatDM - Solo RPG MCP Server for Claude

**ChatDM** is a powerful Model Context Protocol (MCP) server that transforms Claude into your personal tabletop RPG game master, oracle, and assistant. Play D&D, The One Ring, Brambletrek, and more—solo or with Claude as your GM.

## 🎲 What Can ChatDM Do?

### 🎮 **Multi-System RPG Support**
- **The One Ring RPG** (2nd Edition) - Adventure in Middle-earth with Strider Mode for solo play
- **Dungeons & Dragons 5e** (2024 Edition) - Complete D&D support with full 2024 rules
- **Brambletrek** - Cozy woodland creatures on heartwarming journeys
- **My Little Pony RPG** - Friendship-powered adventures in Equestria

### 📚 **Lightning-Fast PDF Search**
- Search across 26+ rulebooks and adventures in ~100ms
- Full-text search with stemming and BM25 relevance ranking
- Find rules, spells, monsters, lore, and mechanics instantly
- Regex support for complex pattern matching

### 🎭 **Oracle & Random Generation Tools**
- Yes/No questions with nuanced answers and probability weighting
- NPC generation (appearance, personality, occupation, behavior)
- Scene elements (locations, weather, atmosphere)
- Dungeon content (rooms, treasures, traps)
- Narrative prompts (actions, complications, events)
- Conversation tools (moods, topics, intents)
- Card draws (Tarot and playing cards)

### 📖 **Persistent Journal System**
- Character sheets saved across sessions
- Adventure logs with automatic timestamping
- NPC catalog with detailed information tracking
- Plot journals for story continuity
- All stored as simple text files in `~/.chatdm/journal/`

### 🧙 **System-Specific Guidance**
- Step-by-step prompts for character creation
- Combat resolution guides
- Journey and travel systems
- Social encounters and councils
- Leveling and progression
- DM/Loremaster guidance

---

## 🚀 Quick Start

### Prerequisites
1. **Java 21+** installed
2. **Maven** installed
3. **Claude Desktop** or compatible MCP client
4. **PDF Resources** (see Installation section)

### Installation

1. **Clone the repository:**
```bash
git clone https://github.com/yourusername/chatdm.git
cd chatdm
```

2. **Add your PDF resources:**
Place your RPG PDFs in the appropriate directories:
```
src/main/resources/pdfs/
├── dnd/           # D&D 5e 2024 books
├── lotr/          # The One Ring books
├── brambletrek/   # Brambletrek books
└── my-little-pony/  # MLP RPG books
```

See `src/main/java/fi/celssi/chatdm/service/GameResourceOracle.java` for expected filenames.

3. **Build the project:**
```bash
mvn clean package
```

This automatically builds the search index from your PDFs (takes 2-5 minutes).

4. **Configure Claude Desktop:**

Edit your Claude Desktop config file:
- **macOS**: `~/Library/Application Support/Claude/claude_desktop_config.json`
- **Windows**: `%APPDATA%\Claude\claude_desktop_config.json`

Add this MCP server configuration:
```json
{
  "mcpServers": {
    "chatdm": {
      "command": "java",
      "args": [
        "-jar",
        "/absolute/path/to/chatdm/target/chatdm-0.0.1-SNAPSHOT.jar"
      ]
    }
  }
}
```

Replace `/absolute/path/to/chatdm/` with your actual path.

5. **Restart Claude Desktop**

Look for the 🔨 icon indicating MCP tools are loaded.

---

## 📖 How to Use ChatDM with Claude

### Starting a New Adventure

Simply tell Claude what you want to play:

```
"I want to start a new The One Ring adventure in solo Strider Mode"
```

Claude will:
1. Use `tor_start_adventure_prompt` for structured guidance
2. Help you create or load a character
3. Initialize the journal system
4. Begin your adventure with an evocative opening scene

### Common Workflows

#### **1. Rule Lookups During Play**

```
"How does concentration work in D&D 5e 2024?"
```

Claude will:
- Use `ChatDM_search_resource` to find concentration rules
- Use `ChatDM_get_page` to read the relevant pages
- Explain the rules clearly with page references

#### **2. Solo Play with Oracles**

```
"I sneak past the guard. Does he notice me?"
```

Claude will:
- Use `ChatDM_likelihood` (setting it to "unlikely" based on context)
- Narrate the result incorporating the oracle answer
- Continue the story based on the outcome

#### **3. NPC Generation**

```
"I enter the tavern. Who's the bartender?"
```

Claude will:
- Use `ChatDM_npc_appearance`, `ChatDM_npc_personality`, etc.
- Create a memorable NPC with distinct characteristics
- Use `ChatDM_save_npc` to remember them for later

#### **4. Character Management**

```
"Save my character Thorin Oakenshield"
```

Claude will:
- Use `ChatDM_save_character` with full character details
- Confirm the save location
- Character is available for future sessions

#### **5. Adventure Logging**

As you play, Claude automatically:
- Uses `ChatDM_log_event` for significant moments
- Tracks quest progress, discoveries, and character development
- Creates a readable adventure log at `~/.chatdm/journal/adventures/`

---

## 🎯 Tool Reference

### 🎲 **Basic Oracles**
- `ChatDM_yes_or_no` - Binary questions with nuanced answers
- `ChatDM_likelihood` - Probability-weighted yes/no (almostCertain, likely, fiftyFifty, unlikely, almostImpossible)
- `ChatDM_roll_dice` - Roll any dice (d4, d6, d8, d10, d12, d20, d100)

### 🎭 **Generation Oracles**
- `ChatDM_npc_*` - Generate NPC appearance, personality, occupation, behavior
- `ChatDM_scene_*` - Generate locations, weather, time of day, atmosphere
- `ChatDM_dungeon_*` - Generate rooms, treasures, traps, directions
- `ChatDM_narrative_*` - Generate actions, complications, events, descriptors
- `ChatDM_conversation_*` - Generate moods, topics, intents for dialogue
- `ChatDM_card_draw` - Draw tarot or playing cards for inspiration

### 📚 **Resource Tools**
- `ChatDM_list_resources` - List all available rulebooks and adventures
- `ChatDM_search_resource` - Fast full-text search across rulebooks
- `ChatDM_get_page` - Read specific pages from resources

### 📖 **Journal Tools**
- `ChatDM_save_character` / `ChatDM_load_character` / `ChatDM_list_characters`
- `ChatDM_start_adventure` / `ChatDM_log_event` / `ChatDM_read_adventure` / `ChatDM_list_adventures`
- `ChatDM_save_npc` / `ChatDM_load_npc` / `ChatDM_update_npc` / `ChatDM_list_npcs`
- `ChatDM_save_plot` / `ChatDM_load_plot` / `ChatDM_list_plots`

### 🎮 **System-Specific Prompts**

**D&D 5e 2024:**
- `dnd_start_adventure_prompt` - Complete adventure start flow
- `dnd_create_character_prompt` - Character creation guidance
- `dnd_play_encounter_prompt` - Encounter resolution
- `dnd_combat_prompt` - Combat system reference
- `dnd_spellcasting_prompt` - Spellcasting mechanics
- `dnd_exploration_prompt` - Exploration and social interaction
- `dnd_leveling_prompt` - Leveling up guidance
- `dnd_dm_guide_prompt` - General DM tips
- `dnd_monster_running_prompt` - Running monsters tactically

**The One Ring RPG:**
- `tor_start_adventure_prompt` - Adventure start with Strider Mode
- `tor_create_hero_prompt` - Hero creation
- `tor_play_scene_prompt` - Scene resolution
- `tor_combat_prompt` - Combat guidance
- `tor_journey_prompt` - Journey system
- `tor_council_prompt` - Council encounters
- `tor_fellowship_phase_prompt` - Fellowship phase
- `tor_strider_mode_prompt` - Solo play reference
- `tor_patron_prompt` - Patron missions
- `tor_tables_prompt` - Solo oracle tables
- `tor_oracle_howto_prompt` - How to use oracles
- `tor_xp_milestones_prompt` - Experience milestones

**Brambletrek & My Little Pony:**
- `brambletrek_prompts` - Interactive gameplay guidance
- `mlp_prompts` - MLP RPG guidance

---

## 🎪 Example Play Session

**You:** "I want to start a new The One Ring adventure as a Ranger"

**Claude:** *Uses `tor_start_adventure_prompt`*
"Let's begin your journey in Middle-earth! I'll guide you through creating your Ranger hero using Strider Mode for solo play.

First, what's your Ranger's name and what drives them into the wilderness?"

**You:** "Arthadan, a Ranger seeking his lost brother"

**Claude:** *Uses `tor_create_hero_prompt`, then `ChatDM_save_character`*
"Excellent! I've saved Arthadan's character sheet. Let me check for a patron mission..."

*Uses `tor_patron_prompt`*

"Gilraen asks you to investigate Rangers gone missing in the wild. You set out from Bree on a cold morning..."

*Uses `tor_start_adventure` to initialize journal*

**You:** "I follow the tracks into the forest. Are they fresh?"

**Claude:** *Uses `ChatDM_likelihood` with "likely"*
"You examine the tracks carefully... *rolls* Yes! The tracks are fresh—no more than a day old. They lead northeast into the Chetwood..."

*Uses `ChatDM_log_event` to record this discovery*

---

## 🛠️ Advanced Features

### Custom Search Queries

Search supports natural language:
```
ChatDM_search_resource("hobbit journey rules", "the-one-ring")
ChatDM_search_resource("fireball spell damage", "dnd-5e-2024")
```

Search with regex (slower, but powerful):
```
ChatDM_search_resource("AC \\d+", "dnd-5e-2024", useRegex=true)
```

### Journal File Locations

All journals are stored in `~/.chatdm/journal/`:
- `characters/` - Character sheets (.txt)
- `adventures/` - Adventure logs (.md with timestamps)
- `npcs/` - NPC catalog (.txt)
- `plots/` - Plot journals (.txt)

You can read, edit, or backup these files directly!

### Performance Tips

- **System-wide searches are FAST**: ~100ms using SQLite FTS5
- **Regex searches are SLOW**: 3-20 seconds (scans all PDFs)
- **Use specific resource IDs** when possible for fastest searches
- **Search index rebuilds** automatically during `mvn clean package`

---

## 🎨 Customization

### Adding New Game Systems

1. Add PDFs to `src/main/resources/pdfs/yourgame/`
2. Register in `GameResourceOracle.java`:
```java
private void registerYourGame() {
    GameSystem system = createGameSystem("your-game", "Your Game Name",
        "Description of your game");

    addResource(system, "core", "Core Rulebook",
        "pdfs/yourgame/core.pdf", "core",
        "Detailed description of this resource");
}
```
3. Call `registerYourGame()` in the `init()` method
4. Rebuild: `mvn clean package`

### Adding Custom Oracle Tables

1. Create JSON file in `src/main/resources/`:
```json
{
  "yourData": ["Item 1", "Item 2", "Item 3"]
}
```

2. Add tool in a new or existing Oracle service:
```java
@Tool(name = "ChatDM_your_oracle", description = "...")
public String yourOracle() {
    return yourData[(int) (Math.random() * yourData.length)];
}
```

---

## 🐛 Troubleshooting

### "Search index not found"
- Run `mvn clean package` to rebuild the index
- Check that PDFs exist in `src/main/resources/pdfs/`

### "No results found"
- Try broader search terms
- Check spelling of game system ID
- Use `ChatDM_list_resources` to verify resource availability

### "Character/Adventure not found"
- Check `~/.chatdm/journal/` directory exists
- Verify file permissions
- Use `ChatDM_list_characters` or `ChatDM_list_adventures` to see saved items

### MCP Server Not Loading
- Verify Java 21+ is installed: `java -version`
- Check absolute path in Claude Desktop config
- Check Claude Desktop MCP logs for errors
- Restart Claude Desktop after config changes

---

## 📊 Architecture

```
chatdm/
├── ChatDmApplication.java          # Spring Boot entry point
├── model/                          # Data models
│   ├── GameSystem.java
│   ├── ResourceInfo.java
│   └── SearchResult.java
├── service/                        # MCP Tool services
│   ├── BasicOracle.java            # Dice, yes/no, likelihood
│   ├── NpcOracle.java              # NPC generation
│   ├── SceneOracle.java            # Scene elements
│   ├── DungeonOracle.java          # Dungeon content
│   ├── NarrativeOracle.java        # Story elements
│   ├── ConversationOracle.java     # Dialogue tools
│   ├── CardOracle.java             # Card draws
│   ├── GameResourceOracle.java     # PDF search
│   ├── JournalOracle.java          # Persistence
│   ├── DnDPromptsOracle.java       # D&D guidance
│   ├── TheOneRingPromptsOracle.java # TOR guidance
│   ├── BrambletrekPromptsOracle.java
│   ├── MyLittlePonyPromptsOracle.java
│   ├── PdfSearchEngine.java        # Regex search
│   └── SqliteSearchEngine.java     # FTS5 search
├── indexer/
│   └── PdfIndexBuilder.java        # Build search index
└── util/
    └── PdfTextCache.java           # PDF text extraction
```

---

## 🤝 Contributing

Contributions welcome! Areas for improvement:
- Additional RPG system support
- New oracle tables and generators
- Enhanced prompt libraries
- UI improvements
- Performance optimizations

---

## 📜 License

[Add your license here]

---

## 🙏 Acknowledgments

- Built with [Spring Boot](https://spring.io/projects/spring-boot) and [Spring AI](https://spring.io/projects/spring-ai)
- PDF text extraction with [Apache PDFBox](https://pdfbox.apache.org/)
- Full-text search with [SQLite FTS5](https://www.sqlite.org/fts5.html)
- Inspired by solo RPG tools like Ironsworn, Mythic GME, and MUNE

---

## 🎲 Ready to Play?

Start Claude Desktop, tell Claude what you want to play, and let the adventure begin!

*"Not all those who wander are lost."* - J.R.R. Tolkien
