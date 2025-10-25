# Brambletrek Interactive Prompts Design

## Prompt 1: Start New Adventure

### Flow

1. **Welcome & Adventure Selection**
   ```
   🌲 Welcome to Brambletrek! Let's start your adventure.
   
   What type of adventure would you like to play?
   
   1. Standard Adventure (Core Rules - Journey through Hyhill)
   2. Secrets of the World Tree (Expansion)
   3. Dungeons of Dragonkeep (Expansion)
   4. The Pumpkin Party (Module)
   5. A Birthday of Wonders (Module)
   6. The Warmth of the First Frost (Module)
   
   Please choose 1-6:
   ```

2. **Character Selection**
   ```
   Great choice! Now, do you want to:
   
   1. Use an existing character
   2. Create a new character
   
   Please choose 1-2:
   ```

3. **If Existing Character**
    - List available saved characters
    - Load selected character

4. **If New Character**
    - Run character creation flow (see Prompt 2)

5. **Start Adventure**
    - Create adventure journal
    - Log starting scenario with character details
    - Display adventure introduction
    - Show starting resources
    - Begin Day 1

---

## Prompt 2: Create New Character

### Flow

1. **Name Input**
   ```
   🐭 Let's create your Gnawborn character!
   
   What is your character's name?
   ```

2. **Legacy Selection**
   ```
   Choose your Legacy (or type 'random' to draw a card):
   
   1. The Seer - Mystical and wise (+5 Morale, -3 Health)
   2. The Scrapper - Tough and fearless (+5 Health, -3 Morale)
   3. The Storyteller - Inspiring and charismatic (+5 Morale, -3 Supplies)
   4. The Seeker - Curious and perceptive (+5 Supplies, -3 Morale)
   5. The Sneaker - Stealthy and quick (+5 Health, -3 Supplies)
   6. The Soother - Healing and supportive (+5 Health, -3 Morale)
   
   Choose 1-6 or type 'random':
   ```

3. **Reason for Adventure**
   ```
   Drawing a card to determine your Reason for Adventure...
   
   🎴 Drew: [Card]
   [Display full reason from table]
   ```

4. **Background**
   ```
   Drawing a card to determine your Background...
   
   🎴 Drew: [Card]
   [Display full background from table]
   ```

5. **Trinket**
   ```
   Drawing a card to determine your Trinket...
   
   🎴 Drew: [Card]
   [Display full trinket description]
   ```

6. **Resources Calculation**
   ```
   Now let's calculate your Resources (Health, Morale, Supplies).
   Drawing 2 cards per stat...
   
   ❤️ Drawing for Health...
   🎴 Drew: [Card 1] + [Card 2] = [Total]
   [If ≤6: Automatically draw bonus card]
   
   🎯 Drawing for Morale...
   🎴 Drew: [Card 1] + [Card 2] = [Total]
   
   🎒 Drawing for Supplies...
   🎴 Drew: [Card 1] + [Card 2] = [Total]
   ```

7. **Apply Legacy Modifiers & Display Final Character**
   ```
   ⚡ Applying [Legacy Name] modifiers...
   
   📋 CHARACTER CREATED: [Name]
   
   Legacy: [Legacy Name]
   Reason: [Brief summary]
   Background: [Brief summary]
   Trinket: [Item name]
   
   Resources:
   ❤️ Health: X/20
   🎯 Morale: X/20
   🎒 Supplies: X/20
   
   [Legacy Name] Abilities:
   • [Ability 1]
   • [Ability 2]
   • [Ability 3]
   • [Ability 4]
   
   ✅ Character saved!
   ```

---

## Gameplay Flow (Automatic with Smart Prompts)

### Normal Encounters - Auto Resolve

```
🎴 Drew: 10 of Clubs

You push through the underbrush and disturb a large beehive!
Angry bees swarm around you, stinging repeatedly.

Damage: -10 Health
Health: 20 → 10/20
```

### When Resources Drop Below 30% - Ask About Abilities

**Critical Health:**

```
⚠️ Your Health is critically low! (6/20)

Available abilities:
• Quick Berry - Restore 4 Health (once per day)

Would you like to use Quick Berry? (yes/no)
```

**Critical Morale:**

```
⚠️ Your Morale is wavering! (4/14)

Available abilities:
• [List relevant Morale abilities if character has any]

Would you like to use an ability? (yes/no)
```

**Low Supplies:**

```
⚠️ Your Supplies are running low! (5/20)

You may struggle to rest tonight (requires 4 Supplies).
Consider searching for supplies or managing encounters carefully.

Continue? (yes)
```

### Overcome the Odds - Offered for Negative Encounters (Black Cards)

**Before damage/loss is applied:**

```
🎴 Drew: 3 of Clubs (Clubs/Spades = Black = Unfortunate Event)

You face thick thorny brambles blocking your path.
Potential damage: -3 Health

Would you like to attempt to Overcome the Odds?
• Uses one daily Legacy Ability slot (replaces one ability use)
• Draw Ability Card and Outcome Card
• If Ability > Outcome: SUCCESS (overcome the event)
• If Ability = Ace: CRITICAL SUCCESS (gain the stat instead!)
• If Ability = 2: CRITICAL FAILURE (lose DOUBLE the stats!)

Attempt to Overcome the Odds? (yes/no)
```

**If yes - Success:**

```
Drawing to Overcome the Odds...

Ability Card: Jack of Hearts (10)
Outcome Card: 7 of Diamonds (7)

10 > 7 - SUCCESS!

You rummage through your bag and pull out a makeshift tool. With 
precision and determination, you cut through the thorns piece by piece!

Event Overcome: No damage taken!
Health: 10/20 (unchanged)

[Note: One Legacy Ability use consumed for today]
```

**If yes - Critical Success (Ace):**

```
Drawing to Overcome the Odds...

Ability Card: Ace of Spades (11) ⭐
Outcome Card: 8 of Hearts (8)

ACE - CRITICAL SUCCESS!

Not only do you navigate the thorns perfectly, but you find medicinal 
herbs growing among them! You harvest them carefully.

Event Overcome: GAINED +3 Health instead!
Health: 10 → 13/20

[Note: One Legacy Ability use consumed for today]
```

**If yes - Critical Failure (2):**

```
Drawing to Overcome the Odds...

Ability Card: 2 of Clubs (2) ⚠️
Outcome Card: 5 of Diamonds (5)

2 - CRITICAL FAILURE!

Your attempt to carefully navigate only makes things worse! You get 
tangled in the thorns and struggle, taking even more damage.

DOUBLE damage: -3 → -6 Health!
Health: 10 → 4/20

[Note: One Legacy Ability use consumed for today]
```

**If yes - Regular Failure:**

```
Drawing to Overcome the Odds...

Ability Card: 4 of Hearts (4)
Outcome Card: 7 of Spades (7)

4 < 7 - FAILED!

Despite your efforts, the thorns are too thick to avoid.

Event occurs as normal: -3 Health
Health: 10 → 7/20

[Note: One Legacy Ability use consumed for today]
```

### Combat - Turn by Turn with Ability Prompts

**Combat Encounter & Overcome the Odds Option:**

```
⚔️ COMBAT: Territorial Crow
Type: Bird/Winged Beast (Clubs)
Health: 12/12

This is an Unfortunate Event (black card - Clubs/Spades).

Would you like to attempt to Overcome the Odds?
• Uses one daily Legacy Ability slot
• Draw Ability Card and Outcome Card
• Success: Overcome combat (no battle needed)
• Critical Success (Ace): Opponent flees, drops ITEM
• Critical Failure (2): Opponent gets Surprise Turn before Initiative

Attempt to Overcome the Odds? (yes/no)
```

**If player chooses no or fails, combat begins:**

**Initiative & First Turn:**

```
Drawing initiative...
You: Queen (10) | Crow: 2
You go first!

--- YOUR TURN 1 ---
Your hand:
1. Ace of Spades - Heroic Strike (Deal 5 damage - half your current health)
2. 2 of Hearts - Dodge (Evade next attack, +2 damage on your next attack)
3. 3 of Hearts - Haymaker (Deal 4 damage)
4. Ace of Clubs - Heroic Strike (Deal 5 damage)

Which card do you play? (1-4)
```

**After player chooses:**

```
You play: Heroic Strike! (Ace of Spades)

[Narrative description of the attack]

Damage: 5
Crow Health: 12 → 7/12

--- CROW'S TURN 1 ---
[Opponent card drawn automatically]
[Narrative description]
[Damage applied]

Your Health: 10 → 6/20
```

**Critical moment in combat:**

```
⚠️ You're badly wounded in combat! (2/20 Health)

Available abilities:
• Quick Berry - Restore 4 Health (once per day) ✓
• Resilience - Halve next damage (once per day) ✓
• Power Strike - Double next attack damage (once per day) ✓

Use an ability before your next turn?
Type ability name or 'none': _____
```

### Between Encounters

```
--- Encounter Complete ---

Current Status:
❤️ Health: 12/20
🎯 Morale: 14/14
🎒 Supplies: 18/20

What would you like to do?
• 'continue' - Draw next encounter
• 'rest' - Spend 4 Supplies to restore 2 Health
• 'check' - View full character sheet and abilities

Your choice: _____
```

### End of Day

```
🌙 NIGHT FALLS - End of Day 1

Day Summary:
• 4 encounters completed
• 1 combat victory
• Entered the Depths of Aldwund

Current Resources:
❤️ Health: 5/20
🎯 Morale: 19/19
🎒 Supplies: 20/20

RESTING:
Spending 4 Supplies...
Restoring 2 Health...

After Rest:
❤️ Health: 7/20
🎯 Morale: 19/19
🎒 Supplies: 16/20

Abilities Used Today (Reset tomorrow):
• Quick Berry ✓ (used)
• Power Strike (available)
• Resilience (available)
• Taunt (available)

Sleep well. Tomorrow brings new adventures...

Ready for Day 2? (yes)
```

---

## Key Features

### Auto-Resolve by Default

- Draw cards automatically
- Resolve encounters with narrative
- Apply damage/benefits immediately
- Keep gameplay flowing

### Smart Intervention Points

1. **Critical Resources** - Ask about healing/restoration abilities
2. **Before Negative Encounters** - Offer "Overcome the Odds"
3. **Combat Turns** - Player chooses tactic cards
4. **Critical Combat Moments** - Suggest available abilities
5. **Between Encounters** - Option to rest or continue
6. **End of Day** - Mandatory rest and ability reset

### No Choices for Positive Encounters

```
🎴 Drew: 6 of Hearts

You encounter a friendly herbalist who teaches you about 
medicinal plants!

Gained: +6 Health
Health: 10 → 16/20

[Continue automatically to next encounter]
```

### Always Offer Overcome the Odds for Negatives

**Important Rules:**

- Only works on **Unfortunate Events (black cards: Clubs ♣ and Spades ♠)**
- Does NOT work on positive events (Hearts ♥ and Diamonds ♦)
- Uses **one daily Legacy Ability slot** (not separate from abilities)
- Can only be used **once per day** (replaces one of the 4 Legacy Abilities)
- Player must choose: Use Overcome the Odds OR save ability slots for combat/events

**Tracking Usage:**

```
After using Overcome the Odds, remind player:

⚠️ Overcome the Odds used! 
This counts as one of your daily Legacy Ability uses.

Remaining ability uses today:
✓ Ability 1 (available)
✓ Ability 2 (available)  
✓ Ability 3 (available)
✗ Ability 4 (used for Overcome the Odds)
```

```
Any card that causes:
- Health loss (Clubs/Spades)
- Morale loss (Clubs/Spades)
- Supply loss (Clubs/Spades)
- Combat (Clubs/Spades face cards)
- Negative effects (Clubs/Spades)

→ Offer Overcome the Odds before resolving
→ Only if player hasn't used it today
→ Remind it uses an ability slot
```