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
