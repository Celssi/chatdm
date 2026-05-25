package fi.celssi.chatdm.service.shared;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class WryterioContentGuardTest {

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "  ", "null", "NULL", "undefined", "a"})
    void isMeaningfulContent_rejectsPlaceholders(String value) {
        assertFalse(WryterioContentGuard.isMeaningfulContent(value));
    }

    @Test
    void isMeaningfulContent_acceptsRealContent() {
        assertTrue(WryterioContentGuard.isMeaningfulContent("Real chapter text here"));
        assertTrue(WryterioContentGuard.isMeaningfulContent("  ok  "));
    }

    @ParameterizedTest
    @ValueSource(strings = {"true", "TRUE", "yes", "1"})
    void isConfirmOverwrite_acceptsTruthyValues(String value) {
        assertTrue(WryterioContentGuard.isConfirmOverwrite(value));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"false", "no", "0", "maybe"})
    void isConfirmOverwrite_rejectsNonTruthyValues(String value) {
        assertFalse(WryterioContentGuard.isConfirmOverwrite(value));
    }
}
