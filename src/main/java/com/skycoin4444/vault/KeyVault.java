package com.skycoin4444.vault;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public final class KeyVault {
    private static final Pattern KEY_ID = Pattern.compile("[A-Za-z0-9_.-]{1,64}");
    private static final int MAX_SECRET_BYTES = 4096;
    private static final int IV_BYTES = 12;
    private static final int TAG_BITS = 128;

    private final SecretKey wrappingKey;
    private final SecureRandom random;
    private final Map<String, Envelope> entries = new ConcurrentHashMap<>();

    public KeyVault(byte[] wrappingKey) {
        this(wrappingKey, new SecureRandom());
    }

    KeyVault(byte[] wrappingKey, SecureRandom random) {
        if (wrappingKey == null || wrappingKey.length != 32) {
            throw new IllegalArgumentException("wrapping key must be exactly 32 bytes");
        }
        this.wrappingKey = new SecretKeySpec(Arrays.copyOf(wrappingKey, wrappingKey.length), "AES");
        this.random = random;
    }

    public void put(String id, byte[] secret) {
        validateId(id);
        if (secret == null || secret.length == 0 || secret.length > MAX_SECRET_BYTES) {
            throw new IllegalArgumentException("secret must contain 1-4096 bytes");
        }
        byte[] iv = new byte[IV_BYTES];
        random.nextBytes(iv);
        entries.put(id, encrypt(id, iv, secret));
    }

    public byte[] get(String id) {
        validateId(id);
        Envelope envelope = entries.get(id);
        if (envelope == null) {
            throw new IllegalArgumentException("key not found");
        }
        return decrypt(id, envelope);
    }

    public boolean delete(String id) {
        validateId(id);
        return entries.remove(id) != null;
    }

    public int size() {
        return entries.size();
    }

    public String exportEnvelope(String id) {
        validateId(id);
        Envelope envelope = entries.get(id);
        if (envelope == null) throw new IllegalArgumentException("key not found");
        return Base64.getUrlEncoder().withoutPadding().encodeToString(envelope.iv()) + "."
            + Base64.getUrlEncoder().withoutPadding().encodeToString(envelope.ciphertext());
    }

    private Envelope encrypt(String id, byte[] iv, byte[] secret) {
        try {
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, wrappingKey, new GCMParameterSpec(TAG_BITS, iv));
            cipher.updateAAD(id.getBytes(StandardCharsets.UTF_8));
            return new Envelope(Arrays.copyOf(iv, iv.length), cipher.doFinal(secret));
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("encryption failed", e);
        }
    }

    private byte[] decrypt(String id, Envelope envelope) {
        try {
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, wrappingKey, new GCMParameterSpec(TAG_BITS, envelope.iv()));
            cipher.updateAAD(id.getBytes(StandardCharsets.UTF_8));
            return cipher.doFinal(envelope.ciphertext());
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("decryption failed", e);
        }
    }

    private static void validateId(String id) {
        if (id == null || !KEY_ID.matcher(id).matches()) {
            throw new IllegalArgumentException("key id must match [A-Za-z0-9_.-]{1,64}");
        }
    }

    private record Envelope(byte[] iv, byte[] ciphertext) {
        Envelope {
            iv = Arrays.copyOf(iv, iv.length);
            ciphertext = Arrays.copyOf(ciphertext, ciphertext.length);
        }
    }
}
