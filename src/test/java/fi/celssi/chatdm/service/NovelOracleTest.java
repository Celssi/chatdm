package fi.celssi.chatdm.service;

import fi.celssi.chatdm.storage.InMemoryJournalStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NovelOracleTest {

    private InMemoryJournalStorage storage;
    private NovelOracle novelOracle;

    @BeforeEach
    void setUp() {
        storage = new InMemoryJournalStorage();
        novelOracle = new NovelOracle(storage);
    }

    @Test
    void saveAndLoadSingleChapter() {
        novelOracle.syncBook("# My Book\n\n## Chapter One\n\nHello.", "My Book");
        assertTrue(novelOracle.saveBookChapter("My Book", 1, "Updated chapter body", "Chapter One").contains("saved"));
        assertTrue(novelOracle.loadBookChapter("My Book", 1).contains("Updated chapter body"));
    }

    @Test
    void appendChapter_incrementsIndex() {
        novelOracle.syncBook("# My Book\n\n## One\n\nA.", "My Book");
        String result = novelOracle.appendBookChapter("My Book", "Second chapter text", "Two");
        assertTrue(result.contains("Chapter 2"));
        assertTrue(novelOracle.loadBookChapter("My Book", 2).contains("Second chapter"));
    }
}
