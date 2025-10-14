package com.rohesa.totp;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.security.GeneralSecurityException;

public class TOTPTest {

    @Test
    public void testGenerateCode() throws GeneralSecurityException {
        SecretGenerator secretGenerator = new SecretGenerator();
        String secretKey = "JBSWY3DPEHPK3PXP"; // Test vector
        byte[] secret = secretGenerator.decodeSecretKey(secretKey);

        TOTP totp = new TOTP(secret, 6, "HmacSHA1", 30);

        // Test with a known timestamp
        long timestamp = 1634484000; // 2021-10-17 12:00:00 UTC
        String code = totp.generateCode(timestamp);

        // The actual code would depend on the timestamp and secret
        // This is just to show how to test the functionality
        assertNotNull(code);
        assertEquals(6, code.length());
        assertTrue(code.matches("\\d{6}"));
    }

    @Test
    public void testVerify() throws GeneralSecurityException {
        SecretGenerator secretGenerator = new SecretGenerator();
        byte[] secret = secretGenerator.decodeSecretKey(secretGenerator.generateSecretKey());

        TOTP totp = new TOTP(secret);
        String code = totp.now();

        assertTrue(totp.verify(code));
    }

    @Test
    public void testBuilder() {
        SecretGenerator secretGenerator = new SecretGenerator();
        String secretKey = secretGenerator.generateSecretKey();

        TOTP totp = new TOTPBuilder()
                .withSecretKey(secretKey)
                .withDigits(8)
                .withAlgorithm("HmacSHA256")
                .withPeriod(60)
                .build();

        // Just verify the build works
        assertNotNull(totp);
    }
}