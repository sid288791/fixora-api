package org.simulynx.fixora.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class GoAlertFingerprintTagTest {

    @Test
    void appendToAddsTagOnItsOwnLineAfterTheDetails() {
        String result = GoAlertFingerprintTag.appendTo("Something broke", "fixora-test-1");

        assertEquals("Something broke\n\nfixora-fingerprint: fixora-test-1", result);
    }

    @Test
    void extractReadsBackAFingerprintAppendedByAppendTo() {
        String details = GoAlertFingerprintTag.appendTo("Something broke", "fixora-test-1");

        assertEquals("fixora-test-1", GoAlertFingerprintTag.extract(details));
    }

    @Test
    void extractReturnsNullWhenNoTagIsPresent() {
        assertNull(GoAlertFingerprintTag.extract("An alert GoAlert received from somewhere else entirely"));
    }

    @Test
    void extractReturnsNullForNullInput() {
        assertNull(GoAlertFingerprintTag.extract(null));
    }

    @Test
    void extractStopsAtWhitespaceSoTrailingTextIsNotCapturedIntoTheFingerprint() {
        String details = "Something broke\n\nfixora-fingerprint: fixora-test-1\n\nSome other trailing note";

        assertEquals("fixora-test-1", GoAlertFingerprintTag.extract(details));
    }
}
