package se.skltp.aggregatingservices.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Autowired;
import se.skltp.aggregatingservices.config.VpConfig;

@Configuration
public class HashStrategyConfig {

  private static final Logger log = LoggerFactory.getLogger(HashStrategyConfig.class);

  private final VpConfig vpConfig;

  @Autowired
  public HashStrategyConfig(VpConfig vpConfig) {
    this.vpConfig = vpConfig;
  }

  @Bean
  public HashStrategy hashStrategy() {
    String strategy = vpConfig.getValidationLog().getHashStrategy();
    if (strategy == null || strategy.isBlank()) {
      log.debug("No vp.validationLog.hashStrategy configured; defaulting to 'sha256'");
      return new Sha256HashStrategy();
    }

    log.debug("Configured vp.validationLog.hashStrategy='{}'", strategy);
    if (strategy.equalsIgnoreCase("noop")) {
      log.warn("Using NoopHashStrategy: validation messages will not be hashed");
      return new NoopHashStrategy();
    }

    if (strategy.equalsIgnoreCase("sha256")) {
      return new Sha256HashStrategy();
    }

    // Unknown strategy — warn and fall back to sha256 to preserve secure default
    log.warn("Unknown vp.validationLog.hashStrategy='{}'. Supported values: 'sha256', 'noop'. Falling back to 'sha256'", strategy);
    return new Sha256HashStrategy();
  }
}
