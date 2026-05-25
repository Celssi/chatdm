package fi.celssi.chatdm.service.journal;

import fi.celssi.chatdm.storage.InMemoryJournalStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JournalToolsTest {

    private InMemoryJournalStorage storage;
    private CharacterJournalTools characters;
    private AdventureJournalTools adventures;
    private CampaignJournalTools campaigns;

    @BeforeEach
    void setUp() {
        storage = new InMemoryJournalStorage();
        characters = new CharacterJournalTools(storage);
        adventures = new AdventureJournalTools(storage);
        campaigns = new CampaignJournalTools(storage);
    }

    @Test
    void characterCrud_roundTrip() {
        assertTrue(characters.saveCharacter("Aragorn", "the-one-ring", "Ranger stats", "Fellowship").contains("saved"));
        assertTrue(characters.loadCharacter("Aragorn").contains("Ranger stats"));
        assertTrue(characters.updateCharacter("Aragorn", "the-one-ring", "Updated stats", "Fellowship").contains("updated"));
        assertTrue(characters.listCharacters("Fellowship").contains("Aragorn"));
        assertTrue(characters.deleteCharacter("Aragorn").contains("deleted"));
        assertTrue(characters.loadCharacter("Aragorn").contains("Error:"));
    }

    @Test
    void adventureLinksToCampaign() {
        String started = adventures.startAdventure("Moria", "the-one-ring", "Frodo", "Desc", "Fellowship");
        assertTrue(started.contains("started"));

        String listed = adventures.listAdventuresByCampaign("Fellowship");
        assertTrue(listed.contains("Moria"));
        assertFalse(listed.contains("No adventures found"));
    }

    @Test
    void campaignDelete_removesFile() {
        assertTrue(campaigns.saveCampaign("Fellowship", "Main arc").contains("saved"));
        assertTrue(campaigns.deleteCampaign("Fellowship").contains("deleted"));
        assertTrue(campaigns.loadCampaign("Fellowship").contains("Error:"));
    }
}
