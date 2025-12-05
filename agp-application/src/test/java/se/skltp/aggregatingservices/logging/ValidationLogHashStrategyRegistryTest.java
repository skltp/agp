package se.skltp.aggregatingservices.logging;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import se.skltp.aggregatingservices.config.VpConfig;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class ValidationLogHashStrategyRegistryTest {

  public static Stream<Arguments> strategyProvider() {
    return Stream.of(
        Arguments.of("sha256", Sha256HashStrategy.class),
        Arguments.of("noop", NoopHashStrategy.class),
        Arguments.of("SHA256", Sha256HashStrategy.class),
        Arguments.of("NoOp", NoopHashStrategy.class),
        Arguments.of("something-else", Sha256HashStrategy.class),
        Arguments.of(null, Sha256HashStrategy.class)
    );
  }

  @Test
  @DisplayName("Default strategy (or unspecified) returns Sha256HashStrategy")
  void defaultStrategyIsSha256() {
    VpConfig cfg = new VpConfig();
    // default in VpConfig.ValidationLog is "sha256"
    HashStrategyConfig registry = new HashStrategyConfig(cfg);
    HashStrategy strategy = registry.hashStrategy();
    assertNotNull(strategy);
    assertInstanceOf(Sha256HashStrategy.class, strategy, "Expected default strategy to be Sha256HashStrategy");
  }

  @ParameterizedTest
  @MethodSource("strategyProvider")
  @DisplayName("Strategy selection based on configuration")
  void strategyIsSelected(String strategyName, Class<? extends HashStrategy> expectedClass) {
    VpConfig cfg = new VpConfig();
    cfg.getValidationLog().setHashStrategy(strategyName);
    HashStrategyConfig registry = new HashStrategyConfig(cfg);
    HashStrategy strategy = registry.hashStrategy();
    assertNotNull(strategy);
    assertInstanceOf(expectedClass, strategy, "Expected strategy for '" + strategyName + "' to be " + expectedClass.getSimpleName());
  }
}

