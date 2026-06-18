# ChatDM - AI Assistant Guide

ChatDM is an MCP (Model Context Protocol) server that exposes tools for solo TTRPG play. This guide helps AI assistants understand how to use the tool ecosystem effectively.

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
| **Search** | PDF resources, rules lookup | `ChatDM_list_resources`, `ChatDM_search_resource` |
| **Prompts** | Game-specific session prompts | `tor_play_scene_prompt`, `dnd_start_adventure_prompt`, etc. |
