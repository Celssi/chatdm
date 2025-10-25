package fi.celssi.chatdm.ChatDM;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

@Service
public class DnDPromptsOracle {

    @Tool(name = "dnd_start_adventure_prompt", description = """
            Get the structured prompt for starting a new D&D 5e 2024 adventure.
            This provides the complete flow for adventure selection, character setup, and journal initialization.
            Use this when a player wants to begin playing D&D 5th Edition (2024).
            """)
    public String getStartAdventurePrompt() {
        return """
                STARTING A D&D 5E 2024 ADVENTURE
                
                STEP 1: CHOOSE OR LOAD CHARACTER
                Ask the player if they want to:
                • Load an existing character (call journal_load_character with their character name)
                • Create a new character (proceed to character creation using dnd_create_character_prompt)
                
                STEP 2: CHOOSE OR LOAD ADVENTURE
                Ask the player if they want to:
                • Load an existing adventure (call journal_load_adventure with adventure name)
                • Start a new adventure
                
                STEP 3: INITIALIZE JOURNAL
                Once character and adventure are ready:
                • Call journal_create to initialize the adventure journal
                • Set up the initial scene
                
                STEP 4: BEGIN PLAY
                • Describe the opening scene vividly
                • Introduce the setting, NPCs, and atmosphere
                • Present the initial situation or hook
                • Ask the player what they do
                
                DM ROLE:
                • Narrate the world, control all NPCs, and resolve actions
                • Interpret dice rolls, apply the 2024 rules, and describe narrative consequences
                • Stay immersed in the world at all times
                
                STYLE:
                • Be descriptive and engaging
                • Balance challenge with fairness
                • Create memorable NPCs and meaningful choices
                • Let player decisions shape the story
                
                RULES ENGINE:
                • System: D&D 5th Edition (2024 rules)
                • Core Mechanic: d20 + modifier vs DC or AC
                • Use Advantage/Disadvantage when appropriate
                • Track resources: HP, spell slots, features, inspiration
                • Reference the 2024 Player's Handbook, Monster Manual, and DM's Guide
                """;
    }

    @Tool(name = "dnd_create_character_prompt", description = """
            Get the structured prompt for D&D 5e 2024 character creation.
            This provides the complete flow with species, class, background, and ability scores.
            Use this when creating a new D&D character.
            """)
    public String getCreateCharacterPrompt() {
        return """
                D&D 5E 2024 CHARACTER CREATION
                
                Guide the player through creating their D&D character step by step:
                
                1. NAME AND CONCEPT
                Ask: What is your character's name and basic concept?
                
                2. SPECIES (formerly Race)
                Choose a species from the 2024 Player's Handbook:
                • Human, Dwarf, Elf, Halfling, Dragonborn, Gnome, Half-Elf, Half-Orc, Tiefling, Orc, Aasimar, Goliath
                Each species grants ability score increases and special traits
                
                3. CLASS
                Choose a class (levels 1-20):
                • Barbarian, Bard, Cleric, Druid, Fighter, Monk, Paladin, Ranger, Rogue, Sorcerer, Warlock, Wizard
                Each class has unique abilities, proficiencies, and progression
                
                4. BACKGROUND
                Choose a background that defines your character's history:
                • Acolyte, Charlatan, Criminal, Entertainer, Folk Hero, Guild Artisan, Hermit, Noble, Outlander, Sage, Sailor, Soldier, Urchin
                Backgrounds grant skill proficiencies and features
                
                5. ABILITY SCORES
                Use Standard Array (15, 14, 13, 12, 10, 8) or Point Buy:
                • Strength (STR) - Physical power
                • Dexterity (DEX) - Agility and reflexes
                • Constitution (CON) - Health and stamina
                • Intelligence (INT) - Reasoning and memory
                • Wisdom (WIS) - Awareness and insight
                • Charisma (CHA) - Force of personality
                Apply species bonuses after base scores
                
                6. SKILLS
                Choose skill proficiencies based on class and background
                Calculate skill bonuses (ability modifier + proficiency bonus if proficient)
                
                7. EQUIPMENT
                Select starting equipment from class options or roll for gold
                
                8. CHARACTER DETAILS
                Ask for:
                • Physical description and personality traits
                • Ideals, bonds, and flaws (from background)
                • Backstory
                
                9. CALCULATE STATS
                • HP: Class hit die maximum + CON modifier
                • AC: 10 + DEX modifier + armor bonus
                • Proficiency Bonus: +2 at level 1
                • Saving Throws: Based on class proficiencies
                • Attack Bonuses: Proficiency bonus + relevant ability modifier
                
                10. SAVE CHARACTER
                Once complete, call journal_save_character to save the character sheet
                Then summarize the character clearly and confirm the setup
                """;
    }

    @Tool(name = "dnd_play_encounter_prompt", description = """
            Get the structured prompt for resolving D&D 5e 2024 encounters.
            This covers combat, skill checks, saving throws, and narrative resolution.
            Use this during active D&D gameplay.
            """)
    public String getPlayEncounterPrompt() {
        return """
                D&D 5E 2024 ENCOUNTER RESOLUTION
                
                During play, act as the Dungeon Master and adjudicator.
                
                ABILITY CHECKS:
                1. Determine the relevant ability and skill
                2. Set a DC (Difficulty Class): Easy 10, Medium 15, Hard 20, Very Hard 25
                3. Check for Advantage/Disadvantage
                4. Player rolls: d20 + ability modifier + proficiency bonus (if proficient)
                5. Compare to DC and narrate the result
                
                SAVING THROWS:
                • Used to resist effects (spells, traps, etc.)
                • Roll d20 + ability modifier + proficiency bonus (if proficient in that save)
                • Compare to spell save DC or effect DC
                
                COMBAT:
                Initiative: Everyone rolls d20 + DEX modifier at start of combat
                
                On Your Turn:
                • Movement: Up to your speed (typically 30 feet)
                • Action: Attack, Cast a Spell, Dash, Disengage, Dodge, Help, Hide, Ready, Search, Use an Object
                • Bonus Action: If you have an ability that uses one
                • Reaction: Available once per round (Opportunity Attacks, certain spells/features)
                
                Attack Rolls:
                • Melee/Ranged: d20 + ability modifier + proficiency bonus vs AC
                • Spell Attack: d20 + spellcasting ability modifier + proficiency bonus vs AC
                • Critical Hit: Natural 20 (roll damage dice twice)
                • Critical Fail: Natural 1 (automatic miss)
                
                Damage:
                • Roll weapon/spell damage dice + ability modifier
                • Apply resistances, vulnerabilities, immunities
                • Reduce target's HP
                
                SPELLCASTING (2024 Rules):
                • Prepared casters can change prepared spells after a long rest
                • Spell slots are spent to cast spells
                • Concentration spells require concentration (one at a time)
                • Spell save DC = 8 + proficiency bonus + spellcasting ability modifier
                
                DEATH AND DYING:
                • At 0 HP: Unconscious and making death saving throws
                • Death Saves: d20, 10+ is success, 3 successes = stable, 3 failures = death
                • Natural 20: Regain 1 HP
                • Natural 1: Counts as 2 failures
                
                RESTS:
                • Short Rest: 1 hour, spend Hit Dice to recover HP, some features recharge
                • Long Rest: 8 hours, recover all HP, half of spent Hit Dice, spell slots, and features
                
                NARRATIVE STYLE:
                • Be vivid and descriptive
                • Make combat dynamic and exciting
                • Give NPCs personality and motivation
                • Let player choices matter
                
                JOURNAL MANAGEMENT:
                • Call journal_append regularly to record important events
                • Track XP, treasure, quest progress, and character development
                """;
    }

    @Tool(name = "dnd_combat_prompt", description = """
            Get structured guidance for running D&D 5e 2024 combat encounters.
            Use this when combat begins or for combat reference.
            """)
    public String getCombatPrompt() {
        return """
                D&D 5E 2024 COMBAT SYSTEM
                
                INITIATIVE:
                • All combatants roll d20 + DEX modifier
                • Order from highest to lowest (DM breaks ties)
                
                TURN STRUCTURE:
                Each creature gets, in order:
                1. Movement (up to speed, can be split)
                2. Action (one of the following):
                   - Attack (one weapon attack, or multiple if you have Extra Attack)
                   - Cast a Spell (with casting time of 1 action)
                   - Dash (double movement this turn)
                   - Disengage (move without provoking opportunity attacks)
                   - Dodge (attacks against you have disadvantage)
                   - Help (give an ally advantage on their next check/attack)
                   - Hide (make a Stealth check)
                   - Ready (prepare an action for a trigger)
                   - Search (make a Perception or Investigation check)
                   - Use an Object
                3. Bonus Action (if you have an ability that uses one)
                4. Reaction (once per round, on anyone's turn)
                
                ATTACKS:
                • Melee Attack: d20 + STR modifier + proficiency bonus vs target's AC
                • Ranged Attack: d20 + DEX modifier + proficiency bonus vs target's AC
                • Hit: Roll damage dice + ability modifier
                • Advantage: Roll 2d20, take higher
                • Disadvantage: Roll 2d20, take lower
                
                COVER:
                • Half Cover: +2 to AC and DEX saves
                • Three-Quarters Cover: +5 to AC and DEX saves
                • Total Cover: Can't be targeted directly
                
                OPPORTUNITY ATTACKS:
                • Reaction when enemy leaves your reach
                • One melee attack
                • Disengage action prevents opportunity attacks
                
                CONDITIONS (2024):
                • Blinded: Can't see, attacks against have advantage, your attacks have disadvantage
                • Charmed: Can't attack charmer, charmer has advantage on social checks
                • Deafened: Can't hear, auto-fail hearing-based checks
                • Frightened: Disadvantage on checks/attacks while source is in sight
                • Grappled: Speed 0, ends if grappler is incapacitated
                • Incapacitated: Can't take actions or reactions
                • Invisible: Can't be seen, attacks against have disadvantage, your attacks have advantage
                • Paralyzed: Incapacitated, auto-fail STR/DEX saves, attacks against have advantage, hits within 5 feet are critical
                • Petrified: Transformed to stone, incapacitated, weight increases 10x, resistant to all damage
                • Poisoned: Disadvantage on attack rolls and ability checks
                • Prone: Disadvantage on attacks, attacks against have advantage within 5 feet (disadvantage beyond), costs half movement to stand
                • Restrained: Speed 0, disadvantage on attacks and DEX saves, attacks against have advantage
                • Stunned: Incapacitated, auto-fail STR/DEX saves, attacks against have advantage
                • Unconscious: Incapacitated, can't move or speak, drops everything, auto-fail STR/DEX saves, attacks against have advantage, hits within 5 feet are critical
                
                COMBAT TIPS:
                • Describe attacks cinematically
                • Narrate the flow of battle
                • Use environment for tactics
                • Track HP, conditions, and spell slots
                • Keep combat moving - don't bog down in rules debates
                """;
    }

    @Tool(name = "dnd_spellcasting_prompt", description = """
            Get guidance for handling spellcasting in D&D 5e 2024.
            Use this for spell mechanics and spellcasting reference.
            """)
    public String getSpellcastingPrompt() {
        return """
                D&D 5E 2024 SPELLCASTING
                
                SPELLCASTING ABILITY:
                • Wizard: Intelligence
                • Cleric, Druid, Ranger: Wisdom
                • Bard, Paladin, Sorcerer, Warlock: Charisma
                
                SPELL SAVE DC: 8 + proficiency bonus + spellcasting ability modifier
                SPELL ATTACK BONUS: proficiency bonus + spellcasting ability modifier
                
                CASTING A SPELL:
                1. Choose a spell you have prepared (or know)
                2. Expend a spell slot of the spell's level or higher
                3. Follow the spell's description for targeting, range, duration, effects
                4. If the spell requires a roll:
                   - Attack Roll: d20 + spell attack bonus vs target's AC
                   - Saving Throw: Target rolls d20 + relevant save vs your spell save DC
                
                SPELL COMPONENTS:
                • V (Verbal): Must be able to speak
                • S (Somatic): Must have a free hand
                • M (Material): Need the material component or a spellcasting focus
                
                CONCENTRATION:
                • Some spells require concentration (marked in spell description)
                • Only one concentration spell at a time
                • Concentration ends if:
                  - You cast another concentration spell
                  - You are incapacitated or killed
                  - You fail a concentration check after taking damage (DC 10 or half damage, whichever is higher)
                
                SPELL LEVELS:
                • Cantrips (0 level): Can be cast at will, unlimited uses
                • 1st-9th Level: Require spell slots
                • Casting at Higher Levels: Some spells get stronger when cast with higher-level slots
                
                PREPARED SPELLS (2024 Changes):
                • Clerics, Druids, Paladins, Wizards prepare spells from their class spell list
                • Can change prepared spells after a long rest
                • Number prepared = spellcasting ability modifier + class level (minimum 1)
                
                KNOWN SPELLS:
                • Bards, Rangers, Sorcerers, Warlocks know a fixed number of spells
                • Can swap one known spell when you gain a level
                
                RITUAL CASTING:
                • Spells with "ritual" tag can be cast without expending a spell slot
                • Takes 10 minutes longer to cast
                • Some classes can ritual cast (Wizard, Cleric, Druid, Bard)
                
                SPELL SLOTS:
                Recovered after a long rest (Warlocks: short rest)
                Level 1: 2 slots at 1st level
                See class progression for full slot table
                
                COMMON SPELL TYPES:
                • Attack Roll Spells: Fire Bolt, Eldritch Blast, Guiding Bolt
                • Save Spells: Fireball (DEX), Hold Person (WIS), Thunderwave (CON)
                • Utility: Detect Magic, Identify, Comprehend Languages
                • Healing: Cure Wounds, Healing Word, Prayer of Healing
                • Buffs: Bless, Shield of Faith, Haste
                • Control: Sleep, Web, Hypnotic Pattern
                """;
    }

    @Tool(name = "dnd_exploration_prompt", description = """
            Get guidance for handling exploration and social interaction in D&D 5e 2024.
            Use this for non-combat gameplay.
            """)
    public String getExplorationPrompt() {
        return """
                D&D 5E 2024 EXPLORATION & SOCIAL INTERACTION
                
                EXPLORATION:
                • Encourage players to describe what they're doing
                • Call for ability checks when there's a chance of failure
                • Use passive Perception for noticing things without actively searching
                • Track time, resources (rations, torches), and exhaustion if relevant
                
                COMMON EXPLORATION CHECKS:
                • Perception (WIS): Noticing things, spotting hidden creatures
                • Investigation (INT): Searching for clues, understanding mechanisms
                • Survival (WIS): Tracking, foraging, navigating wilderness
                • Stealth (DEX): Moving quietly, hiding
                • Athletics (STR): Climbing, jumping, swimming
                • Acrobatics (DEX): Balancing, tumbling
                
                TRAPS:
                • Passive Perception or active Investigation to detect
                • Dexterity check to disarm (with thieves' tools if mechanical)
                • DEX save to avoid damage when triggered
                
                SOCIAL INTERACTION:
                • Roleplay conversations, don't just roll dice
                • Use ability checks when outcome is uncertain:
                  - Persuasion (CHA): Convincing someone in good faith
                  - Deception (CHA): Lying or misleading
                  - Intimidation (CHA): Threatening or coercing
                  - Insight (WIS): Reading intentions and sincerity
                
                NPC ATTITUDES:
                • Hostile: Will actively oppose the party
                • Unfriendly: Unhelpful, suspicious
                • Neutral: No strong feelings either way
                • Friendly: Helpful, willing to assist
                • Helpful: Will go out of their way to help
                
                Use DC 15 for typical social checks, adjust based on NPC attitude and situation
                
                RESTING:
                • Short Rest: 1 hour, spend Hit Dice to heal, some features recharge
                • Long Rest: 8 hours, recover all HP, half Hit Dice, spell slots, features
                
                TRAVEL:
                • Normal pace: 3 miles per hour, 24 miles per day
                • Fast pace: 4 miles per hour, disadvantage on Perception
                • Slow pace: 2 miles per hour, can use Stealth
                
                ENVIRONMENT:
                • Describe sights, sounds, smells
                • Use weather, lighting, terrain to create atmosphere
                • Telegraph dangers and opportunities
                • Make locations memorable
                """;
    }

    @Tool(name = "dnd_leveling_prompt", description = """
            Get guidance for leveling up characters in D&D 5e 2024.
            Use this when characters gain experience and level up.
            """)
    public String getLevelingPrompt() {
        return """
                D&D 5E 2024 LEVELING UP
                
                EXPERIENCE POINTS (XP):
                • Award XP for defeating monsters, completing quests, and achieving goals
                • See DMG for XP tables by monster CR
                • Alternative: Milestone leveling (level up at story milestones)
                
                XP THRESHOLDS:
                • Level 1 → 2: 300 XP
                • Level 2 → 3: 900 XP
                • Level 3 → 4: 2,700 XP
                • Level 4 → 5: 6,500 XP
                • See Player's Handbook for full progression
                
                WHEN YOU LEVEL UP:
                1. Increase HP:
                   - Roll your class Hit Die + CON modifier
                   - Or take the average (rounded up)
                
                2. Check Class Features:
                   - Gain new features as listed in class table
                   - Some levels grant subclass features
                   - Spellcasters may learn new spells or gain spell slots
                
                3. Ability Score Improvements:
                   - At certain levels (4, 8, 12, 16, 19 for most classes)
                   - Increase two ability scores by 1 each (max 20)
                   - Or take a feat instead
                
                4. Proficiency Bonus Increases:
                   - +2 (levels 1-4)
                   - +3 (levels 5-8)
                   - +4 (levels 9-12)
                   - +5 (levels 13-16)
                   - +6 (levels 17-20)
                
                5. Update Character Sheet:
                   - Recalculate attack bonuses, spell save DC, skill bonuses
                   - Update HP maximum
                   - Add new features to your sheet
                
                MULTICLASSING (Optional):
                • Can take levels in multiple classes
                • Must meet ability score prerequisites
                • Gain features of new class but not all proficiencies
                • Spell slots stack, but spells known/prepared don't
                • See Player's Handbook multiclassing rules
                
                FEATS (2024 Updates):
                • Gained at ASI levels or at character creation (variant rule)
                • Many feats have ability score prerequisites
                • Some popular feats: Great Weapon Master, Sharpshooter, War Caster, Alert
                • See Player's Handbook for full feat list
                """;
    }

    @Tool(name = "dnd_dm_guide_prompt", description = """
            Get general DM guidance for running D&D 5e 2024 games.
            Use this for overall DM tips and best practices.
            """)
    public String getDMGuidePrompt() {
        return """
                D&D 5E 2024 DUNGEON MASTER GUIDANCE
                
                CORE PRINCIPLES:
                • The rules are a framework, not chains - adjudicate fairly and consistently
                • Say "yes" when possible, or "yes, but..." or "no, but..."
                • Let player choices matter and shape the story
                • Be a fan of the characters - challenge them but want them to succeed
                • Make NPCs memorable with distinct voices, mannerisms, and motivations
                
                PACING:
                • Balance combat, exploration, and social interaction
                • Vary difficulty - not every encounter needs to be deadly
                • Use short rests to recover resources between encounters
                • End sessions on cliffhangers or natural breaks
                
                IMPROVISATION:
                • It's okay to not have all the answers
                • Use oracle tools (ChatDM oracles) for random inspiration
                • Let player ideas inspire you
                • Take notes on improvised details to maintain consistency
                
                ENCOUNTER BUILDING:
                • Use XP budgets based on party level and desired difficulty
                • Easy: Quick encounters, minimal resource drain
                • Medium: Standard challenge
                • Hard: Risky, significant resource use
                • Deadly: Could result in character deaths
                • See DMG for XP thresholds per character level
                
                REWARDS:
                • Give appropriate XP and treasure
                • Mix magical and mundane rewards
                • Use treasure to hint at lore and world
                • See DMG for treasure tables by CR
                
                RUNNING THE GAME:
                • Describe vividly using all senses
                • Ask "What do you do?" frequently
                • Let players describe their actions and successes
                • Keep combat moving - set a timer if needed
                • Handle rules disputes quickly, look up details after session
                
                SESSION ZERO:
                • Discuss expectations, tone, and boundaries
                • Explain house rules
                • Build characters together
                • Establish party bonds
                
                SAFETY TOOLS:
                • Use X-Card or Lines & Veils to respect boundaries
                • Check in with players about content
                • Be willing to adjust or skip sensitive content
                
                2024 EDITION CHANGES:
                • Species instead of Races
                • Updated spell lists and balance
                • Revised class features
                • New character creation options
                • Streamlined rules in many areas
                """;
    }

    @Tool(name = "dnd_monster_running_prompt", description = """
            Get guidance for running monsters and NPCs in combat.
            Use this for tactical monster guidance.
            """)
    public String getMonsterRunningPrompt() {
        return """
                D&D 5E 2024 RUNNING MONSTERS
                
                MONSTER STAT BLOCKS:
                • AC: Armor Class (how hard to hit)
                • HP: Hit Points (damage it can take)
                • Speed: Movement per turn
                • Ability Scores: STR, DEX, CON, INT, WIS, CHA
                • Saves: Proficient saving throws
                • Skills: Proficient skills
                • Damage Resistances/Immunities/Vulnerabilities
                • Condition Immunities
                • Senses: Darkvision, Blindsight, etc.
                • Languages
                • CR: Challenge Rating (difficulty indicator)
                
                MONSTER ACTIONS:
                • Monsters typically get one action per turn
                • Some have Multiattack (multiple attacks in one action)
                • Special abilities listed in stat block
                • Legendary creatures get Legendary Actions (between turns)
                • Some have Lair Actions (in their lair, on initiative count 20)
                
                TACTICS:
                • Play monsters intelligently based on their INT score
                • Low INT (2-3): Simple tactics, instinct-driven
                • Average INT (8-11): Basic tactics, self-preservation
                • High INT (12+): Complex tactics, retreat when needed
                
                • Consider motivations: defending territory, following orders, hungry, protecting young
                • Use terrain and numbers to advantage
                • Don't fight to the death unless it makes sense (mindless undead, fanatics)
                
                BOSS MONSTERS:
                • Use legendary actions to keep them active between turns
                • Give them minions for action economy
                • Use lair actions for environmental hazards
                • Consider giving them multiple phases or tactical retreats
                
                DESCRIBING COMBAT:
                • Narrate hits and misses cinematically
                • Describe how much damage seems to affect the creature
                • Telegraphing: hint at upcoming big attacks ("The dragon inhales deeply...")
                • Show personality through combat behavior
                
                ADJUDICATING DIFFICULTY:
                • If encounter is too easy: add reinforcements or have enemies retreat to regroup
                • If too hard: have enemies focus on subduing rather than killing, or flee when bloodied
                • Don't be afraid to adjust HP on the fly for pacing
                """;
    }
}
