package fi.celssi.chatdm.service;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

@Service
public class TheOneRingPromptsOracle {

    @Tool(name = "tor_start_adventure_prompt", description = """
            Get the structured prompt for starting a new The One Ring 2e adventure.
            Includes a fast setup path for solo play using Strider Mode.
            Use this when a player wants to begin playing The One Ring Second Edition.
            """)
    public String getStartAdventurePrompt() {
        return """
                STARTING A THE ONE RING 2E ADVENTURE
                
                STEP 1: CHOOSE OR LOAD HERO
                Ask the player if they want to:
                • Load an existing hero (call journal_load_character with the hero name)
                • Create a new hero (proceed to character creation using tor_create_hero_prompt)
                
                STEP 2: CHOOSE OR LOAD ADVENTURE
                Ask the player if they want to:
                • Load an existing adventure (call journal_load_adventure with adventure name)
                • Start a new adventure
                
                STEP 3: SELECT PLAY MODE
                • Solo Strider Mode: No Loremaster. Use tor_strider_mode_prompt and tor_oracle_howto_prompt
                • Traditional LM Mode: Use standard rules. You still can use tor_tables_prompt for inspiration
                
                STEP 4: PICK A PATRON AND SAFE HAVEN
                • Patron: Gandalf, Gilraen, Bilbo, Balin, Círdan, or Tom and Goldberry
                • Safe Haven: Choose a sanctuary tied to culture or patron. Note why it feels like home
                • If Solo Strider Mode, roll or choose a Patron mission with tor_patron_prompt
                
                STEP 5: INITIALIZE JOURNAL
                • Call journal_create to start the adventure log
                • Record: hero, culture, calling, Patron, safe haven, starting year and season
                
                STEP 6: OPENING SCENE
                • Frame a grounded scene: a message, a rumor, a meeting, a sign of Shadow
                • Set place, time of day, weather, and stakes
                • Ask the player what they do
                
                GM OR SOLO FACILITATOR ROLE
                • Evoke Eriador with concrete details. Keep travel and peril present
                • When uncertain, consult tor_tables_prompt or tor_oracle_howto_prompt
                • Resolve actions with tor_play_scene_prompt and tor_combat_prompt
                • Use tor_journey_prompt for longer travel, and tor_fellowship_phase_prompt at rests
                
                RULES ENGINE
                • System: The One Ring Second Edition
                • Core mechanic: Feat die plus Success dice against a Target Number
                • Attribute TN = 20 − Attribute. In Strider Mode you may use 18 − Attribute to heighten self reliance
                • Track Endurance, Hope, Shadow. Apply Weary, Miserable, Wounded correctly
                • Use Favoured and Ill-favoured as sources allow
                """;
    }

    @Tool(name = "tor_create_hero_prompt", description = """
            Get the structured prompt for The One Ring 2e hero creation.
            Includes solo-friendly notes for Strider Mode.
            """)
    public String getCreateHeroPrompt() {
        return """
                THE ONE RING 2E HERO CREATION
                
                1. NAME AND CONCEPT
                Ask for a concise concept: culture, outlook, and what draws the hero into the wilds
                
                2. CHOOSE CULTURE
                Options include Bardings, Dwarves of Durin's Folk, Elves of Lindon, Hobbits of the Shire, Men of Bree, Rangers of the North
                • Note: record Attributes, Skills, Proficiencies, Blessing, and Standard of Living
                
                3. CHOOSE CALLING
                Captain, Champion, Messenger, Scholar, Treasure Hunter, or Warden
                • Write the Shadow Path events for this calling
                
                4. ATTRIBUTES AND DERIVED STATS
                • Set Strength, Heart, Wits
                • Compute Attribute TNs: 20 − Attribute (or 18 − Attribute in Strider Mode variant)
                • Record Endurance, Hope, Parry, and Load
                
                5. SKILLS AND COMBAT PROFICIENCIES
                • Mark cultural Skill ranks and any chosen increases
                • Choose weapon proficiencies and list damage, injury, and qualities
                
                6. DISTINCTIVE FEATURES
                • Choose according to culture. In Strider Mode also add the special Feature: Strider. While journeying you are Inspired on skill rolls
                
                7. VIRTUE AND REWARD
                • Choose one starting Virtue or Reward (culture dependent). Avoid company-only benefits for solo play
                
                8. STARTING GEAR
                • War gear, travelling gear, useful items, mounts if any. Track Load
                
                9. SAFE HAVEN
                • Describe why it is home, who dwells there, and what comfort it offers
                
                10. PATRON
                • Pick a Patron aligned with the hero’s goals. Note Agenda and Favoured Callings
                
                11. FELLOWSHIP RATING
                • Solo baseline: 3, plus bonuses from Virtues, Blessings, or Patron if applicable
                
                12. PREVIOUS EXPERIENCE
                • Allocate as per core rules. In Strider Mode you may start with 15 points instead of 10 to reflect hard-earned grit
                
                13. FINISHING TOUCHES
                • Appearance, manner, notable memories, and a personal goal
                • Save with journal_save_character and append a short background to the adventure log
                """;
    }

    @Tool(name = "tor_play_scene_prompt", description = """
            Get the structured prompt for resolving non-combat scenes in The One Ring 2e.
            Covers action resolution, tests, degrees of success, and consequences.
            """)
    public String getPlayScenePrompt() {
        return """
                THE ONE RING 2E SCENE RESOLUTION
                
                SCENE FRAMING
                1. State place, time, weather, and who is present
                2. Name the immediate challenge or uncertainty
                3. Ask the player how they approach it
                
                WHEN TO ROLL
                • Roll when there is danger, hidden knowledge, or an uncooperative NPC to influence
                • Otherwise say yes and move forward
                
                MAKING A TEST
                1. Pick the relevant Skill, Combat Proficiency, or Attribute test
                2. Build the pool: 1 Feat die + Success dice equal to Skill rating
                3. Compare to the appropriate Attribute TN
                4. Interpret degree of success: none, great, extraordinary
                5. Apply Favoured or Ill-favoured when sources call for it
                6. Spend Hope for +1d. If Inspired, Hope grants +2d
                
                RESULT GUIDANCE
                • Success: achieve intent and add a useful detail
                • Great: achieve intent with speed, subtlety, or collateral benefit
                • Extraordinary: achieve intent and set up advantage for the next action or widen influence
                • Failure: change the situation. Introduce a cost, a complication, time pressure, or Shadow exposure
                
                SOLO AIDS
                • If uncertain, ask a yes or no question and consult tor_oracle_howto_prompt
                • For open questions, pull a short prompt from tor_tables_prompt and build from there
                • Record turning points with journal_append
                """;
    }

    @Tool(name = "tor_combat_prompt", description = """
            Get structured guidance for running The One Ring 2e combat, including a Strider Mode skirmish note.
            """)
    public String getCombatPrompt() {
        return """
                THE ONE RING 2E COMBAT
                
                ONSET OF BATTLE
                • Establish sides, distance, lighting, footing, and morale
                • Choose stances and set target numbers accordingly
                • Name who acts first based on fiction or initiative method your table prefers
                
                ROUNDS AT CLOSE QUARTERS
                1. Choose a stance each round: Forward, Open, Defensive, or Rearward
                2. Make attack rolls with weapon proficiency. On a hit, inflict damage. On a Piercing blow, force Protection test or cause a Wound
                3. Track Endurance loss, Weary when Endurance ≤ Load, and Miserable when Hope is exhausted
                4. Protection tests prevent Wounds. Two Wounds generally remove a foe from the fight
                5. Apply Complications and Advantages from the fiction
                
                SPECIAL STATES
                • Weary: count only solid 4–6 on Success dice for totals
                • Miserable: a roll with the Eye on the Feat die and no success icons fails regardless of total
                • Wounded: serious risk. Many foes withdraw or fall after one Wound
                
                HOPE AND INSPIRATION
                • Spend Hope for +1d. If Inspired, +2d
                • Distinctive Features can grant Inspiration when you lean into them
                
                STRIDER MODE SKIRMISH NOTE
                • As a lone hero, favour distance, terrain, and ambush
                • Use a mobile, evasive style: strike, withdraw, and deny melee when outnumbered
                • When fictionally justified, run small hit-and-fade exchanges before full Close Quarters is joined
                
                AFTERMATH
                • Describe the field. Offer a moment of quiet or a new threat. Journal key details and injuries
                """;
    }

    @Tool(name = "tor_journey_prompt", description = """
            Get structured guidance for running journeys in The One Ring 2e.
            Includes solo adjustments for Strider Mode.
            """)
    public String getJourneyPrompt() {
        return """
                THE ONE RING 2E JOURNEYS
                
                BEFORE YOU SET OUT
                • Mark origin, destination, route, and season. Note Peril rating by region
                • Estimate legs and rests. Set reasons for haste or caution
                
                ROLES OR SOLO APPROACH
                • Group play uses Guide, Hunter, Look-out, and Scout
                • In Strider Mode there is one hero. React to hazards as they arise with appropriate Skills
                
                TRAVEL SEQUENCE
                1. Describe daily progress: terrain, weather, and signs
                2. Trigger journey events by region or interval. Resolve with tests and fiction
                3. Apply Fatigue and Endurance loss as the route and events dictate. Watch Load and Weary
                4. Offer short vignettes of hope, wonder, or memory to balance hardship
                
                JOURNEY EVENTS, SOLO-FRIENDLY IDEAS
                • Ominous signs: tracks, smoke, carrion birds
                • Waylaid by weather: sudden storm, fog, bitter cold or heat
                • Crossroads choice: safe detour versus risky shortcut
                • Hidden thing: a barrow, a campsite, a stash, an old boundary stone
                • Company on the road: wary travellers, Dwarves with news, Rangers, or Ruffians
                • Shadow in the wild: wargs on the scent, spiders, or unseen watchers
                
                ARRIVAL
                • Set first impressions and potential trouble. If time passes, proceed to tor_fellowship_phase_prompt for rest
                """;
    }

    @Tool(name = "tor_council_prompt", description = """
            Get guidance for Councils and social encounters in The One Ring 2e.
            """)
    public String getCouncilPrompt() {
        return """
                THE ONE RING 2E COUNCILS
                
                SET THE SCENE
                • Who is the host and what do they want
                • Formality, time pressure, gifts, and courtesy
                
                RESOLUTION FLOW
                1. Opening approach: Awe, Courtesy, or Riddle as suited
                2. Establish Tolerance based on culture and approach
                3. Exchange arguments. Roll appropriate Skills and track successes against Tolerance
                4. Conclude with an ask. On success, receive aid, news, or safe passage. On failure, suffer delay, suspicion, or a new condition
                
                SOLO NOTE
                • If uncertain about the host’s mood or demands, ask yes or no questions with tor_oracle_howto_prompt
                • Draw a prompt from tor_tables_prompt to reveal an unexpected factor like a rival or a debt
                """;
    }

    @Tool(name = "tor_fellowship_phase_prompt", description = """
            Get guidance for Fellowship Phases in The One Ring 2e.
            Includes journaling and undertakings suited to solo play.
            """)
    public String getFellowshipPhasePrompt() {
        return """
                THE ONE RING 2E FELLOWSHIP PHASE
                
                WHEN IT BEGINS
                • After an Adventuring Phase or upon reaching a safe haven
                
                WHAT TO DO
                • Heal and recover. Remove Fatigue with rest and comfort
                • Choose Undertakings: Gather Rumours, Heal Scars, Train, Research Lore, Visit Patron, Craft, Strengthen Ties
                • If solo, report to Patron in fiction and roll or choose a new lead for the next journey
                
                EXPERIENCE
                • Spend Skill points and Adventure points per core rules
                • At Yule add Skill points equal to Wits
                • In Strider Mode you may also award points by Milestone. See tor_xp_milestones_prompt
                
                JOURNAL
                • Summarize events, lessons, new bonds, and worries for the road ahead
                """;
    }

    @Tool(name = "tor_strider_mode_prompt", description = """
            Get the Strider Mode quick reference for solo play in The One Ring 2e.
            Includes oracle use, solo adjustments, and practical tips.
            """)
    public String getStriderModePrompt() {
        return """
                STRIDER MODE QUICK REFERENCE
                
                CORE IDEA
                • You are both player and facilitator. Ask questions, act boldly, and let the dice surprise you
                
                SOLO ADJUSTMENTS
                • Attribute TN option: 18 − Attribute to keep pace with lone challenges
                • Extra Distinctive Feature: Strider. While journeying you are Inspired on skill rolls
                • Fellowship rating: 3 plus bonuses from Blessings, Virtues, or Patron
                
                ORACLE BASICS
                1. Phrase a clear question. Set odds
                2. Roll or decide. Yes, Maybe, or No
                3. If Maybe, add a twist or a cost
                • Use tor_oracle_howto_prompt for odds bands and examples
                • Use tor_tables_prompt for Telling prompts, Lore sparks, and Fortune or Ill-fortune when the Feat die shows Gandalf or the Eye
                
                PATRONS
                • Select a Patron fitting your aim. Pull or choose a mission seed with tor_patron_prompt
                
                JOURNEYS
                • React to hazards as they arise. Keep travel scenes short and vivid. Let a few details imply the wider wild
                
                COMBAT
                • Prefer ambush, elevation, and distance. Withdraw when outnumbered
                
                ADVANCEMENT
                • After notable beats, check tor_xp_milestones_prompt and award Skill and Adventure points
                """;
    }

    @Tool(name = "tor_patron_prompt", description = """
            Get Patron selection guidance and a d6 list of mission seeds for each Patron.
            """)
    public String getPatronPrompt() {
        return """
                PATRONS AND MISSION SEEDS
                
                HOW TO USE
                • Choose a Patron that matches your agenda and calling
                • Roll 1d6 or pick a mission seed and personalize it with people, places, and a looming Shadow
                
                GANDALF THE GREY
                1. Carry heartening news to a remote stead and lift their spirits
                2. Deliver word of enemy movements to a trusted ally who can act
                3. Win the trust of a wary group who might aid the Free Peoples
                4. Capture a servant of the Enemy and learn what they know
                5. Confirm a rumor of a weapon the Enemy plans to wield
                6. Rescue a missing friend now held in a dark place
                
                GILRAEN, DAUGHTER OF DIRHAEL
                1. Find a Ranger gone missing and learn their fate
                2. Uncover a hunter of Rangers and their motives
                3. Break up raiding patrols along the road and learn who leads them
                4. Shield a settlement that has drawn the Enemy's eye and discover why
                5. Face monstrous servants prowling the borders and the strange gear they bear
                6. Seek a silent refuge and rekindle its watch
                
                BILBO BAGGINS
                1. Fetch a rare delicacy from an out of the way place
                2. Survey the land and note changes for a careful map
                3. Bear a missive along perilous roads to a specific friend
                4. Find someone who can decipher a puzzling old text
                5. Retrieve a map with secrets that must not fall to the Enemy
                6. Track an old trinket of import before darker hands claim it
                
                BALIN, SON OF FUNDIN
                1. Trace a nameless Shadow stirring near a Dwarven holding
                2. Reforge a broken Dwarf-worked thing of war and find the right craftsperson
                3. Explore lost halls for lore that can arm the West
                4. Carry home what a fallen comrade bore
                5. Drive the Enemy from a defiled stronghold and learn who commands them
                6. Hunt a rising lieutenant gathering followers in the dark
                
                CÍRDAN THE SHIPWRIGHT
                1. Deliver timber or tar vital to shipbuilding under threat
                2. Confront an old foe of the Elves who has shown their face again
                3. Travel to a beacon and rekindle its flame against creeping Shadow
                4. Learn why messengers vanish or are waylaid
                5. Recover an Elven heirloom from a creature that covets it
                6. Heal a sacred grove whose trees are ailing
                
                TOM BOMBADIL AND LADY GOLDBERRY
                1. Cure beasts struck by a strange affliction with a rare herb
                2. Cleanse a spring gone black and deal with what lurks there
                3. Escort travellers on a vital errand through the wilds
                4. Purge a sacred place now stained by the Shadow
                5. Find a flower blooming after long years and protect it
                6. Banish a foul presence nesting in an ancient tree
                """;
    }

    @Tool(name = "tor_tables_prompt", description = """
            Get compact solo tables: Telling yes or no, Lore sparks, and Fortune or Ill-fortune prompts.
            Suitable for pasting into a scratchpad while you play.
            """)
    public String getTablesPrompt() {
        return """
                SOLO TABLES
                
                TELLING: SET ODDS THEN ROLL 1D100
                • Impossible: Yes 1–2, Maybe 3–4, otherwise No
                • Very Unlikely: Yes 1–10, Maybe 11–15, otherwise No
                • Unlikely: Yes 1–30, Maybe 31–40, otherwise No
                • Fifty Fifty: Yes 1–50, Maybe 51–60, otherwise No
                • Likely: Yes 1–70, Maybe 71–85, otherwise No
                • Very Likely: Yes 1–90, Maybe 91–95, otherwise No
                • Near Certain: Yes 1–96, Maybe 97–98, otherwise No
                If Maybe, add a twist or a cost
                
                LORE SPARKS: ROLL 1D20
                1 Stranger on the road  2 Hidden boundary stone  3 Old watch tower  4 Fresh tracks
                5 Abandoned camp  6 Ruined bridge  7 Whispered rumor  8 Found token or clasp
                9 Strange lights  10 Songs of long ago  11 Ranger sign  12 Dwarven mark
                13 Footprints that stop  14 Black feather  15 Cold well  16 Barrow door ajar
                17 Broken blade  18 Faded map fragment  19 Echoed footsteps  20 Distant horn
                Use as answers to Who, What, Where, or Why by association
                
                FORTUNE WHEN THE FEAT DIE SHOWS GANDALF
                • Timely aid. An ally, tool, or shortcut appears
                • Clear insight. See through a ruse or grasp a hidden link
                • Safe passage. Weather clears or foes move aside
                • Hidden strength. You do better than expected
                
                ILL-FORTUNE WHEN THE FEAT DIE SHOWS THE EYE
                • Complication now. A second threat acts or a plan is revealed
                • Weariness. Extra Fatigue, poor footing, or gear snags
                • Unwelcome attention. Someone marks you for later trouble
                • Costly choice. Succeed but accept a mark or a debt
                """;
    }

    @Tool(name = "tor_oracle_howto_prompt", description = """
            Get a short how-to for asking oracle questions in solo play, with odds guidance and examples.
            """)
    public String getOracleHowToPrompt() {
        return """
                ORACLE HOW TO
                
                1. STATE THE QUESTION
                Make it specific. Yes or no works best when the answer changes the situation
                
                2. SET THE ODDS
                Base on fiction, not desire. Choose from: Impossible, Very Unlikely, Unlikely, Fifty Fifty, Likely, Very Likely, Near Certain
                
                3. ROLL 1D100 AND READ THE BAND
                Yes, Maybe, or No. If Maybe, add a twist or a cost and move forward
                
                4. FOLLOW UP WITH A LORE SPARK
                Pull a prompt from tor_tables_prompt to add a concrete detail
                
                EXAMPLES
                • Is there an herb-master in this village? Odds: Unlikely. Roll. Maybe. There is someone who knows herbs, but they are away until dusk
                • Do the tracks belong to goblins? Odds: Likely. Roll. Yes. Fresh, and moving north
                • Does the ferryman trust me? Odds: Fifty Fifty. Roll. No. You will need to offer proof or pay dearly
                • Is the ruin warded? Odds: Very Likely. Roll. Maybe. The ward sleeps but will answer loud noise
                """;
    }

    @Tool(name = "tor_xp_milestones_prompt", description = """
            Get a milestone checklist for awarding Skill and Adventure points in Strider Mode.
            """)
    public String getXPMilestonesPrompt() {
        return """
                STRIDER MODE MILESTONES
                
                AWARD TYPICAL REWARDS AS YOU GO
                • Accept a Patron mission: +1 Adventure point
                • Complete a meaningful journey: +2 Skill points
                • Face a noteworthy encounter during travel: +1 Skill point
                • Reveal a significant location or discovery: +1 Adventure point
                • Overcome a tricky obstacle: +1 Skill point
                • Participate in a Council: +1 Skill point
                • Survive a dangerous combat: +1 Adventure point
                • Achieve a personal goal: +1 Adventure and +1 Skill point
                • Complete a Patron mission: +1 Adventure and +1 Skill point
                
                AT YULE
                • Add Skill points equal to Wits
                
                JOURNAL
                • Note what changed in the world or in the hero
                """;
    }
}
