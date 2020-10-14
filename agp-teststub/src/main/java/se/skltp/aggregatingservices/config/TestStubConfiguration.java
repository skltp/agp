package se.skltp.aggregatingservices.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "teststub")
public class TestStubConfiguration {
  String findContentAddress;
  String sokVagValInfoAddress;
  String faultyServiceAddress;

}
