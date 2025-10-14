package com.rohesa.totp;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

/**
 * Utility for generating QR codes for TOTP secrets.
 */
public class QRCodeGenerator {

    /**
     * Generates a TOTP URI according to the KeyURI format.
     *
     * @param issuer The service provider issuing the TOTP
     * @param accountName The user account name
     * @param secretKey The secret key in Base32 encoding
     * @param digits The number of digits in the code
     * @param algorithm The HMAC algorithm
     * @param period The time period in seconds
     * @return The URI string
     */
    public String generateTotpUri(String issuer, String accountName, String secretKey,
                                  int digits, String algorithm, int period) {
        try {
            // URL encode the parameters
            String encodedIssuer = URLEncoder.encode(issuer, StandardCharsets.UTF_8.toString());
            String encodedAccountName = URLEncoder.encode(accountName, StandardCharsets.UTF_8.toString());

            // Build the URI parameters
            Map<String, String> params = new HashMap<>();
            params.put("secret", secretKey);
            params.put("issuer", encodedIssuer);
            params.put("algorithm", algorithm);
            params.put("digits", String.valueOf(digits));
            params.put("period", String.valueOf(period));

            // Build the URI string
            StringBuilder paramsString = new StringBuilder();
            for (Map.Entry<String, String> entry : params.entrySet()) {
                if (paramsString.length() > 0) {
                    paramsString.append("&");
                }
                paramsString.append(entry.getKey()).append("=").append(entry.getValue());
            }

            return String.format("otpauth://totp/%s:%s?%s",
                    encodedIssuer, encodedAccountName, paramsString);
        } catch (Exception e) {
            throw new IllegalArgumentException("Error generating TOTP URI", e);
        }
    }

    /**
     * Generates a QR code image containing the TOTP URI.
     *
     * @param uri The TOTP URI
     * @param width The width of the QR code image
     * @param height The height of the QR code image
     * @return A BufferedImage containing the QR code
     * @throws WriterException If the QR code cannot be generated
     */
    public BufferedImage generateQRCode(String uri, int width, int height) throws WriterException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(uri, BarcodeFormat.QR_CODE, width, height);
        return MatrixToImageWriter.toBufferedImage(bitMatrix);
    }

    /**
     * Converts a QR code to PNG bytes.
     *
     * @param qrCode The QR code image
     * @return The PNG data as a byte array
     * @throws IOException If the image cannot be written
     */
    public byte[] toByteArray(BufferedImage qrCode) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(qrCode, "PNG", baos);
        return baos.toByteArray();
    }
}