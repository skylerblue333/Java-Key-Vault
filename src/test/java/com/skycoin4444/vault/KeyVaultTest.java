package com.skycoin4444.vault;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class KeyVaultTest {
    private static byte[] key() {
        byte[] key = new byte[32];
        for (int i = 0; i < key.length; i++) key[i] = (byte) (i + 1);
        return key;
    }

    @Test
    void storesEncryptedEnvelopeAndRoundTripsSecret() {
        KeyVault vault = new KeyVault(key());
        byte[] secret = "super-secret-value".getBytes(StandardCharsets.UTF_8);
        vault.put("api.primary", secret);
        assertArrayEquals(secret, vault.get("api.primary"));
        assertFalse(vault.exportEnvelope("api.primary").contains("super-secret-value"));
        assertEquals(1, vault.size());
    }

    @Test
    void rejectsInvalidIdsAndOversizedSecrets() {
        KeyVault vault = new KeyVault(key());
        assertThrows(IllegalArgumentException.class, () -> vault.put("", new byte[] {1}));
        assertThrows(IllegalArgumentException.class, () -> vault.put("ok", new byte[4097]));
    }

    @Test
    void deleteRemovesEntry() {
        KeyVault vault = new KeyVault(key());
        vault.put("token", new byte[] {1, 2, 3});
        assertTrue(vault.delete("token"));
        assertFalse(vault.delete("token"));
        assertThrows(IllegalArgumentException.class, () -> vault.get("token"));
    }

    @Test
    void wrongWrappingKeyCannotDecryptEnvelopeByConstruction() {
        assertThrows(IllegalArgumentException.class, () -> new KeyVault(new byte[16]));
    }
}
