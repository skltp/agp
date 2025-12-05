package se.skltp.aggregatingservices.logging;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * SHA-256-based hash strategy. Uses a ThreadLocal MessageDigest for performance and avoids repeated
 * allocations. This class intentionally retains a ThreadLocal<MessageDigest> because the hash
 * strategies are used by request-processing routes that are created at application initialization
 * and not changed at runtime. Threads running those routes are long-lived, so reuse is safe and
 * reduces allocation churn.
 */
@SuppressWarnings("squid:S5164")
public class Sha256HashStrategy implements HashStrategy {
  private static final String ALGORITHM = "SHA-256";

  // Use a ThreadLocal MessageDigest: MessageDigest is not thread-safe but expensive to create.
  private static final ThreadLocal<MessageDigest> DIGEST = ThreadLocal.withInitial(() -> {
    try {
      return MessageDigest.getInstance(ALGORITHM);
    } catch (NoSuchAlgorithmException e) {
      // SHA-256 should always be available on modern JDKs; fail fast if not.
      throw new ExceptionInInitializerError(e);
    }
  });

  // Static hex lookup to avoid Formatter allocations per call
  private static final char[] HEX_ARRAY = "0123456789abcdef".toCharArray();

  @Override
  public String hash(String input) {
    if (input == null) return "";

    MessageDigest md = DIGEST.get();
    md.reset();
    byte[] digest = md.digest(input.getBytes(java.nio.charset.StandardCharsets.UTF_8));

    // convert bytes to hex using static lookup
    char[] hexChars = new char[digest.length * 2];
    for (int byteIndex = 0; byteIndex < digest.length; byteIndex++) {
      int v = digest[byteIndex] & 0xFF;
      hexChars[byteIndex * 2] = HEX_ARRAY[v >>> 4];
      hexChars[byteIndex * 2 + 1] = HEX_ARRAY[v & 0x0F];
    }
    return new String(hexChars);
  }
}
