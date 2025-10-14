package com.rohesa.totp;

import org.apache.commons.codec.binary.Base32;
import java.security.SecureRandom;

/**
 * Utility for generating secure secret keys for TOTP.
 */
public class SecretGenerator {
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final Base32 BASE_32_ENCODER = new Base32();

    private final int defaultSecretSize;

    public SecretGenerator() {
        this(20); // Default to 160 bits (20 bytes)
    }

    public SecretGenerator(int secretSizeBytes) {
        this.defaultSecretSize = secretSizeBytes;
    }

    /**
     * Generates a random secret key with the default size.
     *
     * @return The generated secret in Base32 encoding
     */
    public String generateSecretKey() {
        return generateSecretKey(defaultSecretSize);
    }

    /**
     * Generates a random secret key with a custom size.
     *
     * @param size The size of the secret in bytes
     * @return The generated secret in Base32 encoding
     */
    public String generateSecretKey(int size) {
        byte[] buffer = new byte[size];
        RANDOM.nextBytes(buffer);
        return BASE_32_ENCODER.encodeToString(buffer);
    }

    /**
     * Decodes a Base32-encoded secret key to bytes.
     *
     * @param base32Secret The Base32-encoded secret
     * @return The decoded secret
     */
    public byte[] decodeSecretKey(String base32Secret) {
        return BASE_32_ENCODER.decode(base32Secret);
    }
}