# TOTP (Time-based One-Time Password)

[](https://opensource.org/licenses/MIT)
[](https://www.oracle.com/java/technologies/javase/jdk11-archive-downloads.html)

A modern, **RFC 6238** compliant implementation of the Time-based One-Time Password (TOTP) algorithm. Written in pure Java 11, this library is lightweight and has zero Spring dependencies, making it easy to integrate into any Java project, including Spring Boot.

This library also includes utilities for:

  * Secure Base32 secret key generation.
  * `otpauth://` URI generation for enrollment.
  * QR Code generation to display the secret for easy scanning by authenticator apps (like Google Authenticator, Authy, etc.).

## Key Features

  * **TOTP Code Generation:** Generate TOTP codes based on a secret key and the current time.
  * **TOTP Code Verification:** Verify user-submitted codes, with support for a time window (discrepancy) to account for clock skew.
  * **Secret Key Generation:** Securely generate random secret keys (20 bytes, 160 bits) and encode them in Base32.
  * **QR Code Generator:** Create the `otpauth://` URI and render it as a QR Code image (`BufferedImage` or `byte[]`).
  * **Flexible Configuration:** Uses a Builder Pattern to customize:
      * Code digits (default: 6)
      * HMAC algorithm (default: `HmacSHA1`, supports `HmacSHA256`, `HmacSHA512`)
      * Time period (default: 30 seconds)

-----

## Installation

### 1\. (Recommended) Local Testing Usage

This is the method you requested to test this library in your other local projects.

**Step 1: Clone This Repository**
Clone the `totp` repository to your local machine.

```bash
git clone https://github.com/Rohesa123/totp.git
cd totp
```

**Step 2: Install to Your Local Maven Repository**
Run the following Maven command inside the `totp` project directory. This will compile the code and install it into your local `.m2` repository.

```bash
mvn clean install
```

**Step 3: Add as a Dependency in Your Other Project**
Now, in your *other* project (e.g., your Spring Boot application), add the following dependency to your `pom.xml` file. Maven will automatically find it in your local `.m2` repository.

```xml
<dependency>
    <groupId>com.rohesa</groupId>
    <artifactId>totp</artifactId>
    <version>1.0.0</version>
</dependency>
```

### 2\. (Future) Via Maven Central

Once this project is published to Maven Central, you can simply add it like this:

```xml
<dependency>
    <groupId>com.rohesa</groupId>
    <artifactId>totp</artifactId>
    <version>1.0.0</version>
</dependency>
```

-----

## How to Use

Here are common usage examples for the library.

### Example 1: User Enrollment (Creating a Secret & QR Code)

When a user wants to enable 2FA, you need to generate a new secret, save it to your database, and display it as a QR code for them to scan.

```java
import com.rohesa.totp.SecretGenerator;
import com.rohesa.totp.QRCodeGenerator;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

// 1. Generate a new secret key
SecretGenerator secretGen = new SecretGenerator();
String base32Secret = secretGen.generateSecretKey();
// -> "JBSWY3DPEHPK3PXP..." (Example)
// IMPORTANT: Save this base32Secret in your database for this user.

// 2. Prepare details for the QR Code
String issuer = "YourAppName"; // Your application/service name
String accountName = "user@example.com"; // The user's email or username

// 3. Create the otpauth:// URI
QRCodeGenerator qrGen = new QRCodeGenerator();
String uri = qrGen.generateTotpUri(issuer, accountName, base32Secret, 6, "HmacSHA1", 30);
// -> "otpauth://totp/YourAppName:user@example.com?secret=JBSWY3DPEHPK3PXP&issuer=YourAppName&algorithm=HmacSHA1&digits=6&period=30"

// 4. Generate the QR Code image
// You can send this as a byte array in an HTTP response
// or save it temporarily.
try {
    BufferedImage qrImage = qrGen.generateQRCode(uri, 250, 250);
    
    // (Optional) Save to a file for testing
    ImageIO.write(qrImage, "PNG", new File("qrcode.png"));

    // (Optional) Convert to byte array to send via API
    byte[] pngData = qrGen.toByteArray(qrImage);
    
    System.out.println("QR Code generated successfully!");
    System.out.println("Secret Key: " + base32Secret);

} catch (Exception e) {
    e.printStackTrace();
}
```

### Example 2: Verifying a TOTP Code

When the user logs in, after entering their password, they will be prompted for the 6-digit code from their authenticator app. You need to verify this code.

```java
import com.rohesa.totp.TOTP;
import com.rohesa.totp.TOTPBuilder;

// 1. Get the user's saved secret from your database (from Example 1)
String userSecretFromDB = "JBSWY3DPEHPK3PXP"; // (Example)

// 2. Get the code submitted by the user from the login form
String userProvidedCode = "123456"; 

try {
    // 3. Create a TOTP instance using the Builder
    TOTP totp = new TOTPBuilder()
                    .withSecretKey(userSecretFromDB)
                    .build(); // Uses defaults (6 digits, HmacSHA1, 30 sec)

    // 4. Verify the code
    // totp.verify() automatically checks the time-window discrepancy
    boolean isValid = totp.verify(userProvidedCode);

    if (isValid) {
        System.out.println("2FA Verification Successful!");
        // Proceed with login
    } else {
        System.out.println("Invalid 2FA Code.");
        // Fail login
    }

} catch (Exception e) {
    e.printStackTrace();
}
```

### Example 3: Advanced Configuration (Builder)

If you wish to use non-default settings (e.g., 8 digits, SHA256, and a 60-second period).

```java
import com.rohesa.totp.TOTP;
import com.rohesa.totp.TOTPBuilder;

String userSecretFromDB = "JBSWY3DPEHPK3PXP...";

// Custom configuration
TOTP totpCustom = new TOTPBuilder()
                    .withSecretKey(userSecretFromDB)
                    .withDigits(8)
                    .withAlgorithm("HmacSHA256")
                    .withPeriod(60)
                    .build();

// Verify (ensure the enrollment QR code used the same parameters!)
boolean isValid = totpCustom.verify("87654321");

// Generate the current code (useful for debugging)
String currentCode = totpCustom.now();
System.out.println("Current 8-digit (SHA256) code: " + currentCode);
```

-----

## Dependencies

This library uses the following external dependencies:

  * **[commons-codec](https://commons.apache.org/proper/commons-codec/)**: Used for Base32 encoding and decoding.
  * **[ZXing (core & javase)](https://github.com/zxing/zxing)**: Used for QR Code generation.

## License

This project is licensed under the [MIT License](https://www.google.com/search?q=LICENSE).
