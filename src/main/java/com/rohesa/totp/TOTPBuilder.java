package com.rohesa.totp;

/**
 * Builder for creating TOTP instances with custom configurations.
 */
public class TOTPBuilder {
    private byte[] secret;
    private int digits = 6;
    private String algorithm = "HmacSHA1";
    private long period = 30;

    /**
     * Sets the secret key.
     *
     * @param secret The secret key as bytes
     * @return This builder
     */
    public TOTPBuilder withSecret(byte[] secret) {
        this.secret = secret.clone();
        return this;
    }

    /**
     * Sets the secret key from a Base32-encoded string.
     *
     * @param base32Secret The Base32-encoded secret key
     * @return This builder
     */
    public TOTPBuilder withSecretKey(String base32Secret) {
        SecretGenerator secretGenerator = new SecretGenerator();
        this.secret = secretGenerator.decodeSecretKey(base32Secret);
        return this;
    }

    /**
     * Sets the number of digits in the generated codes.
     *
     * @param digits The number of digits (usually 6 or 8)
     * @return This builder
     */
    public TOTPBuilder withDigits(int digits) {
        this.digits = digits;
        return this;
    }

    /**
     * Sets the HMAC algorithm.
     *
     * @param algorithm The algorithm (HmacSHA1, HmacSHA256, or HmacSHA512)
     * @return This builder
     */
    public TOTPBuilder withAlgorithm(String algorithm) {
        this.algorithm = algorithm;
        return this;
    }

    /**
     * Sets the time period.
     *
     * @param period The period in seconds (usually 30)
     * @return This builder
     */
    public TOTPBuilder withPeriod(long period) {
        this.period = period;
        return this;
    }

    /**
     * Builds the TOTP instance.
     *
     * @return A new TOTP instance with the configured settings
     * @throws IllegalStateException If the secret key has not been set
     */
    public TOTP build() {
        if (secret == null) {
            throw new IllegalStateException("Secret key must be set");
        }

        return new TOTP(secret, digits, algorithm, period);
    }
}