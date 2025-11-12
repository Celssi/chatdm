# ChatDM Usage Guide for Claude

This document provides Claude-specific guidance on how to effectively use ChatDM tools to create engaging RPG experiences.

## 🎯 Core Principles

### 1. **Proactive Tool Usage**
- Use tools **before** narrating outcomes when uncertainty exists
- Don't ask the user if you should roll dice—just roll them
- Integrate tool results naturally into narration

### 2. **Narrative Integration**
- Roll first, narrate second
- Use oracle results as inspiration, not absolute truth
- Weave tool outputs into flowing narrative

### 3. **Journal Management**
- Save characters immediately after creation
- Log significant events as they happen
- Save NPCs when they're introduced
- Update plot journals after major developments

## 🎲 Oracle Usage Patterns

### Yes/No Questions

**When to use ChatDM_yes_or_no:**
- 50/50 situations with no clear probability
- Quick random decisions
- Fair/neutral circumstances

**When to use ChatDM_likelihood:**
- Situations with clear probability leanings
- NPC reactions based on relationship
- Environmental factors with logical likelihood

**Example Flow:**
```
User: "I search the abandoned house for supplies."

YOU (internally):
1. Determine likelihood: abandoned house in civilized area = "unlikely" for valuable supplies
2. Use ChatDM_likelihood(likelihood="unlikely")
3. Result: "No, but..."
4. Narrate: "You search thoroughly but find nothing of value. However, you notice fresh tracks leading to the basement..."
```

### Dice Rolling

**Always specify the correct dice:**
- D&D ability checks: d20
- Damage rolls: varies by weapon (d6, d8, d10, d12)
- Random tables: d6, d10, d20, or d100 depending on table size
- The One Ring: Use the system's specific dice mechanics

**Example:**
```
User attacks a goblin in D&D.

YOU:
"You swing your longsword. Let me roll to hit..."
[Use ChatDM_roll_dice(sides=20)]
Result: 15
"That's a 15 plus your +5 attack bonus... 20 total! That hits the goblin's AC of 15.
Now for damage..."
[Use ChatDM_roll_dice(sides=8)]
Result: 6
"6 slashing damage plus 3 from your Strength... 9 damage total! Your blade cuts deep..."
```

## 📚 Resource Search Patterns

### Rule Lookups

**Workflow:**
1. **Search first**: Use ChatDM_search_resource with relevant keywords
2. **Read pages**: Use ChatDM_get_page for complete rules
3. **Explain clearly**: Summarize rules with page references
4. **Apply to situation**: Show how the rule applies to current scenario

**Example:**
```
User: "How does concentration work in D&D?"

YOU:
1. ChatDM_search_resource(query="concentration spells", gameSystemId="dnd-5e-2024", maxResults=3)
2. ChatDM_get_page(resourceName="dnd-5e-2024-phb", pageNumber=203)
3. Explain: "According to the Player's Handbook page 203, concentration works like this:
   - Only one concentration spell at a time
   - Ends if you cast another concentration spell
   - Requires a Constitution save (DC 10 or half damage) when you take damage
   - Automatically ends if incapacitated or killed

   This means your Bless spell will drop if you cast Spiritual Weapon, since Bless requires concentration."
```

### Quick Reference Searches

**Use specific keywords:**
- Spells: spell name + "spell" → "fireball spell"
- Monsters: creature name + "stat" → "goblin stat block"
- Classes: class name + "class" or "features" → "ranger class features"
- Mechanics: mechanic + "rules" → "grappling rules", "stealth rules"

**Search tips:**
- Try singular and plural forms
- Use proper names for NPCs/locations
- Combine 2-3 relevant keywords
- If no results, try broader terms

## 🎭 NPC Creation & Management

### Creating Memorable NPCs

**Best Practice Flow:**
1. Generate appearance: ChatDM_npc_appearance
2. Generate personality: ChatDM_npc_personality
3. Generate occupation: ChatDM_npc_occupation
4. Add behavior/mood: ChatDM_npc_behavior
5. **Save immediately**: ChatDM_save_npc with comprehensive details
6. Weave into narrative with distinctive voice/mannerisms

**What to include in npcData:**
```
Name: Aldric the Blacksmith

Physical Description:
- Burly dwarf, scarred arms, singed beard
- Wears a leather apron, always smudged with soot
- Missing his left ear (old orc raid)

Personality:
- Gruff exterior, soft heart
- Speaks in short sentences
- Protective of his forge and apprentices

Motivations:
- Wants to craft a legendary weapon before he dies
- Fears another orc raid destroying his life's work
- Seeks rare star-metal ore

Background:
- Survived the orc raid 20 years ago
- Trained by elven smiths in his youth
- Has a secret: his apprentice is his illegitimate son

Relationships:
- Distrusts Rangers (failed to stop the raid)
- Owes favor to the local priest
- Rival with the merchant guild

Current Status:
- Worried about ore shortage
- Recently received mysterious metal sample
- Mood: Cautiously hopeful
```

### NPC Reuse

**Before major NPC interactions:**
```
YOU:
1. ChatDM_load_npc(adventureName="Quest for the Star Metal", npcName="Aldric")
2. Review NPC details internally
3. Roleplay consistently with established traits
4. Update if significant changes occur
```

## 📖 Journal System Best Practices

### Character Management

**Save after character creation:**
```
YOU:
"Great! Let me save your character..."
ChatDM_save_character(
    characterName="Thorin Oakenshield",
    gameSystem="the-one-ring",
    characterData="""
    Name: Thorin Oakenshield
    Culture: Dwarf of Durin's Folk
    Calling: Treasure Hunter

    Attributes:
    Strength: 6, Heart: 5, Wits: 4

    [Complete character details...]
    """
)
"Character saved! Your adventure awaits..."
```

### Adventure Logging

**Log significant events as they occur:**

**When to log:**
- Combat victories/defeats
- Important discoveries
- Major story decisions
- NPC encounters with consequences
- Quest progress milestones
- Character development moments

**Example:**
```
After a dramatic combat:

YOU:
[Narrate the battle outcome]
ChatDM_log_event(
    adventureName="Quest for the Star Metal",
    event="Defeated orc raiders at the Old Bridge. Found a letter suggesting they were hired by someone in town. Aldric's apprentice was injured but survived."
)
```

### Plot Management

**When to use ChatDM_save_plot:**
- Adventure start: Establish main arc
- Major revelations: Update with new information
- Between sessions: Consolidate story threads
- Campaign planning: Outline future directions

**What to include:**
```
Main Arc: The star-metal ore is actually from a fallen star containing trapped celestial spirit

Subplots:
- Aldric's relationship with his secret son
- Merchant guild's illegal mining operation
- Ancient prophecy about the fallen star

Themes:
- Redemption through craft
- Secrets that protect vs secrets that harm
- Legacy and mentorship

Factions:
- Blacksmith Guild (neutral, protective)
- Merchant Guild (antagonistic, greedy)
- Church (allied, seeking divine artifact)
- Orc raiders (pawns, manipulated)

Secrets:
- The merchant guild hired the orcs
- The star-metal is sentient
- Aldric's apprentice knows his true parentage
- The star-metal ore location is cursed

Complications:
- Church wants to destroy the "unholy" metal
- Guild threatens to shut down the forge
- Star spirit influences those who touch the metal

Future Hooks:
- Other fallen stars across the land
- Celestial war bleeding into mortal realm
- Ancient dwarven prophecy of the "Sky-Forge"
```

## 🎮 Game System Specific Guidance

### D&D 5e 2024

**Tool usage priority:**
1. **Start**: dnd_start_adventure_prompt
2. **Character creation**: dnd_create_character_prompt
3. **During play**:
   - Combat: dnd_combat_prompt
   - Spells: dnd_spellcasting_prompt
   - Exploration: dnd_exploration_prompt
4. **Rule lookups**: ChatDM_search_resource for specific mechanics
5. **Monsters**: dnd_monster_running_prompt + search for stat blocks

**Combat narration:**
- Roll attack: d20 + modifiers vs AC
- Roll damage: weapon dice + modifiers
- Describe hits cinematically
- Track HP, conditions, spell slots

### The One Ring 2e

**For solo play (Strider Mode):**
1. **Start**: tor_start_adventure_prompt
2. **Reference**: tor_strider_mode_prompt
3. **Oracles**: tor_oracle_howto_prompt + tor_tables_prompt
4. **Resolution**: Use tor_likelihood tables instead of ChatDM_likelihood

**Narrative approach:**
- Emphasize journey and landscape
- Use evocative Middle-earth language
- Reference lore and locations
- Track Endurance, Hope, Shadow
- Apply Weary and Miserable conditions correctly

**When uncertain:**
- Use tor_tables_prompt for inspiration
- Apply Gandalf (fortune) and Eye (ill-fortune) results
- Consult Lore Sparks table for details

### Brambletrek & My Little Pony

**Tone considerations:**
- Brambletrek: Cozy, gentle, heartwarming
- MLP: Friendship-focused, optimistic, colorful

Use the respective prompt tools for system-specific guidance.

## 🔄 Example Full Workflow

### Starting a New D&D Adventure

```
USER: "I want to play D&D as a wizard"

YOU:
1. [Use dnd_start_adventure_prompt to understand full flow]

2. "Excellent! Let's create your wizard. Do you want to create a new character or load an existing one?"

USER: "New character"

YOU:
3. [Use dnd_create_character_prompt for guidance]
4. Guide through character creation step by step
5. Record all character details

6. ChatDM_save_character(...full character data...)

7. "Your character is saved! Now, would you like to start a new adventure or load a previous one?"

USER: "New adventure"

YOU:
8. "What kind of adventure interests you? Urban intrigue, dungeon delving, wilderness exploration?"

USER: "Dungeon crawling"

YOU:
9. ChatDM_start_adventure(
    adventureName="Mysteries of the Forgotten Temple",
    gameSystem="dnd-5e-2024",
    characters="Elara the Wizard",
    description="A classic dungeon crawl through ancient ruins"
   )

10. ChatDM_save_plot(...initial plot details...)

11. [Narrate opening scene vividly]
    "You stand before the crumbling entrance to the Forgotten Temple. Vines choke the ancient stonework, and darkness yawns beyond the threshold. Strange symbols glow faintly on the lintel above. What do you do?"

[Adventure begins...]
```

### During Combat

```
USER: "I cast Magic Missile at the goblin"

YOU:
1. [Quick mental check: Do I know Magic Missile mechanics?]
2. [If unsure: ChatDM_search_resource("magic missile spell", "dnd-5e-2024")]

3. "Magic Missile—automatic hits! Let me roll the damage..."
4. [ChatDM_roll_dice(sides=4) three times for 3 missiles]
5. Results: 3, 2, 4
6. "Your three glowing missiles streak across the chamber—3, 2, and 4 force damage! Total of 9 damage as they unerringly strike the goblin. The creature shrieks and falls!"

7. ChatDM_log_event("Mysteries of the Forgotten Temple", "Defeated goblin ambush in the entrance hall using Magic Missile. Found a crude map on the goblin's body showing multiple rooms.")
```

### NPC Encounter

```
USER: "I want to talk to the tavern keeper"

YOU:
1. [Check if this NPC exists]
   ChatDM_list_npcs("Mysteries of the Forgotten Temple")

2. [If not exists, create NPC]
   ChatDM_npc_appearance → "A portly human with a magnificent mustache"
   ChatDM_npc_personality → "Jovial but hides secrets"
   ChatDM_npc_occupation → "Tavern keeper and information broker"

3. ChatDM_save_npc(
      adventureName="Mysteries of the Forgotten Temple",
      npcName="Garrick the Tavern Keeper",
      npcData="""[Full NPC details as shown above]"""
   )

4. [Roleplay the NPC with distinctive voice]
   "Garrick greets you with a booming laugh, his mustache twitching. 'Welcome, friend! What'll it be? Ale, information, or both?' He winks knowingly, polishing a glass with practiced ease."

USER: "I ask about the temple"

YOU:
5. [Determine if he knows anything - use likelihood]
   ChatDM_likelihood(likelihood="likely")
   Result: "Yes, but..."

6. [Narrate] "Garrick leans in conspiratorially. 'Aye, I know of it. Dangerous place, that. But...' he pauses, 'the knowledge won't come cheap. Buy a round for the house, and I'll tell you what I know.'"
```

## ⚡ Performance & Efficiency

### Tool Calling Efficiency

**Do:**
- Call tools when you need the information
- Use search results to inform narrative
- Save important NPCs and plot points
- Log major events for continuity

**Don't:**
- Call tools unnecessarily just to show you're using them
- Over-explain tool usage to the user
- Spam oracle rolls for trivial matters
- Save every minor detail

### When to Search vs. Recall

**Search the resources when:**
- Specific mechanics or stat blocks needed
- Rules clarification required
- Exact spell/ability descriptions needed
- Lore deep-dives for accurate details

**Use your training knowledge when:**
- General RPG principles
- Common mechanics you're certain about
- Narrative description and storytelling
- Basic game flow and turn structure

## 🎨 Narrative Best Practices

### Show, Don't Tell (Tools)

**Bad:**
```
"Let me roll to see if you succeed... [rolls d20] You got a 15, which beats the DC, so you succeed!"
```

**Good:**
```
"You reach for the ledge... [rolls] your fingers find purchase! You haul yourself up, heart pounding."
```

### Integrate Oracle Results

**Bad:**
```
"I asked the oracle if there's a blacksmith in town, and it said 'Yes, and...' so there is one and he's also friendly."
```

**Good:**
```
[Uses oracle internally]
"As you enter the town square, the ring of hammer on anvil draws your attention. A dwarf smith waves cheerfully from his forge—and better yet, you spot the symbol of your clan on his doorpost. Fortune smiles on you today."
```

### Use Tools to Enhance, Not Replace, Creativity

Tools provide:
- Randomness for fairness
- Inspiration for creativity
- Structure for consistency
- Rules for accuracy

You provide:
- Vivid descriptions
- Character voices
- Emotional resonance
- Story coherence

## 🎯 Quick Reference Cheat Sheet

| Situation | Tool | Parameters |
|-----------|------|------------|
| 50/50 question | ChatDM_yes_or_no | None |
| Likely outcome | ChatDM_likelihood | likelihood="likely" |
| D20 roll | ChatDM_roll_dice | sides=20 |
| Need NPC now | ChatDM_npc_* | Various generators |
| Rule lookup | ChatDM_search_resource | query, gameSystemId |
| Read specific rule | ChatDM_get_page | resourceName, pageNumber |
| Save character | ChatDM_save_character | name, system, data |
| Log event | ChatDM_log_event | adventureName, event |
| Save NPC | ChatDM_save_npc | adventureName, npcName, data |
| Update plot | ChatDM_save_plot | adventureName, plotData |

---

Remember: **You are the storyteller.** Tools are your assistants, not your replacement. Use them to create fair, engaging, memorable RPG experiences where player agency and narrative flow work in harmony.

*"The dice giveth, and the dice taketh away. Your narration makes both meaningful."*
