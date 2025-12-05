package se.skltp.aggregatingservices.logging;

/** Pluggable hashing strategy used to obfuscate validation messages. */
public interface HashStrategy {
  /**
   * Return a non-reversible representation of input suitable for uniqueness checks.
   */
  String hash(String input);
}

