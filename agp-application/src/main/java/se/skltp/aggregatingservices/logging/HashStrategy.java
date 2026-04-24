/*
 * Copyright © 2014-2026 Inera.
 * Copyright owner URL: https://www.inera.se/
 * SKLTP overview page: https://inera.atlassian.net/wiki/spaces/SKLTP/overview
 * This library is free software under the GNU Lesser General Public License v3.0.
 * Please refer to the full license files at the project root.
 */
package se.skltp.aggregatingservices.logging;

/** Pluggable hashing strategy used to obfuscate validation messages. */
public interface HashStrategy {
  /**
   * Return a non-reversible representation of input suitable for uniqueness checks.
   */
  String hash(String input);
}

