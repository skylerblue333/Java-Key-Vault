package com.skycoin4444.vault;

import java.util.Base64;

public final class Main {
    private Main() {}

    public static void main(String[] args) {
        String encoded = System.getenv("SKY_VAULT_MASTER_KEY_B64");
        if (encoded == null || encoded.isBlank()) {
            System.err.println("SKY_VAULT_MASTER_KEY_B64 is required");
            System.exit(2);
        }
        final byte[] key;
        try {
            key = Base64.getDecoder().decode(encoded);
        } catch (IllegalArgumentException e) {
            System.err.println("SKY_VAULT_MASTER_KEY_B64 must be valid base64");
            System.exit(2);
            return;
        }
        try {
            KeyVault vault = new KeyVault(key);
            System.out.println("{\"service\":\"sky-key-vault\",\"status\":\"ready\",\"entries\":" + vault.size() + "}");
        } catch (IllegalArgumentException e) {
            System.err.println(e.getMessage());
            System.exit(2);
        }
    }
}
