/*
 * Copyright © 2014-2026 Inera.
 * Copyright owner URL: https://www.inera.se/
 * SKLTP overview page: https://inera.atlassian.net/wiki/spaces/SKLTP/overview
 * This library is free software under the GNU Lesser General Public License v3.0.
 * Please refer to the full license files at the project root.
 */
package se.skltp.aggregatingservices.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "vp")
public class VpConfig {
  String instanceId;
  String defaultServiceURL;
  int defaultConnectTimeout;
  int defaultReceiveTimeout;
  Boolean useAyncHttpConduit;
  ValidationLog validationLog = new ValidationLog();

  @Data
  public static class ValidationLog {
    Set<String> services = new HashSet<>();
    int interval = 60000;
    // choose hashing strategy for validation messages: "sha256" (default) or "noop"
    String hashStrategy = "sha256";
  }
}
