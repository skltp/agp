package se.skltp.aggregatingservices.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "ei")
public class EiConfig {
  String logicalAddress;
  String senderId;
  String findContentUrl;
  int connectTimeout;
  int receiveTimeout;
  Boolean useAyncHttpConduit;
}
