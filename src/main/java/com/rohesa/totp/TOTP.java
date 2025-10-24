package com.rohesa.totp;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;

/**
 * Implementation of the Time-based One-Time Password algorithm.
 * Based on RFC 6238: https://tools.ietf.org/html/rfc6238
 */
public class TOTP {
    private static final int DEFAULT_DIGITS = 6;
    private static final String DEFAULT_ALGORITHM = "HmacSHA1";
    private static final long DEFAULT_PERIOD = 30;

    private final byte[] secret;
    private final int digits;
    private final String algorithm;
    private final long period;

    /**
     * Creates a new TOTP instance with default settings.
     *
     * @param secret The shared secret
     */
    public TOTP(byte[] secret) {
        this(secret, DEFAULT_DIGITS, DEFAULT_ALGORITHM, DEFAULT_PERIOD);
    }

    /**
     * Creates a new TOTP instance with custom settings.
     *
     * @param secret The shared secret
     * @param digits The number of digits in the OTP (usually 6 or 8)
     * @param algorithm The HMAC algorithm to use (HmacSHA1, HmacSHA256, HmacSHA512)
     * @param period The time period in seconds (usually 30)
     */
    public TOTP(byte[] secret, int digits, String algorithm, long period) {
        this.secret = secret.clone();
        this.digits = digits;
        this.algorithm = algorithm;
        this.period = period;
    }

    /**
     * Generates a TOTP code for the current time.
     *
     * @return The generated TOTP code
     * @throws GeneralSecurityException If the HMAC algorithm is not available
     */
    public String now() throws GeneralSecurityException {
        return generateCode(Instant.now().getEpochSecond());
    }

    /**
     * Generates a TOTP code for a specific timestamp.
     *
     * @param timestamp The Unix timestamp (in seconds)
     * @return The generated TOTP code
     * @throws GeneralSecurityException If the HMAC algorithm is not available
     */
    public String generateCode(long timestamp) throws GeneralSecurityException {
        long counter = timestamp / period;
        return generateCodeFromCounter(counter);
    }

    /**
     * Verifies if a TOTP code is valid for the current time.
     *
     * @param code The TOTP code to verify
     * @return true if the code is valid, false otherwise
     */
    public boolean verify(String code) {
        return verify(code, 1);
    }

    /**
     * Verifies if a TOTP code is valid for the current time within a window.
     *
     * @param code The TOTP code to verify
     * @param windowSize The number of periods before and after the current time to check
     * @return true if the code is valid, false otherwise
     */
    public boolean verify(String code, int windowSize) {
        if (code == null || code.length() != digits) {
            return false;
        }

        long currentTimeSeconds = Instant.now().getEpochSecond();
        long currentTimeCounter = currentTimeSeconds / period;

        for (int i = -windowSize; i <= windowSize; i++) {
            try {
                String calculatedCode = generateCodeFromCounter(currentTimeCounter + i);

                boolean areEqual = MessageDigest.isEqual(
                        calculatedCode.getBytes(StandardCharsets.UTF_8),
                        code.getBytes(StandardCharsets.UTF_8)
                );

                if (areEqual) {
                    return true;
                }
            } catch (GeneralSecurityException e) {
                return false;
            }
        }

        return false;
    }

    private String generateCodeFromCounter(long counter) throws GeneralSecurityException {
        byte[] counterBytes = new byte[8];
        for (int i = 7; i >= 0; i--) {
            counterBytes[i] = (byte) (counter & 0xff);
            counter >>= 8;
        }

        byte[] hash = generateHMAC(algorithm, secret, counterBytes);

        // Dynamic truncation
        int offset = hash[hash.length - 1] & 0xf;
        int binary = ((hash[offset] & 0x7f) << 24) |
                ((hash[offset + 1] & 0xff) << 16) |
                ((hash[offset + 2] & 0xff) << 8) |
                (hash[offset + 3] & 0xff);

        int otp = binary % (int) Math.pow(10, digits);

        // Ensure the code has the correct number of digits by padding with zeros
        return String.format("%0" + digits + "d", otp);
    }

    private byte[] generateHMAC(String algorithm, byte[] key, byte[] data) throws NoSuchAlgorithmException, InvalidKeyException {
        Mac mac = Mac.getInstance(algorithm);
        mac.init(new SecretKeySpec(key, algorithm));
        return mac.doFinal(data);
    }
}