package se.skltp.aggregatingservices;

import java.util.ArrayList;
import java.util.List;

import org.apache.camel.CamelContext;
import org.apache.camel.spring.boot.CamelContextConfiguration;
import org.apache.cxf.feature.Feature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import se.skltp.aggregatingservices.logging.MessageLoggingFeature;

@Configuration
public class BeansConfiguration {

  @Bean
  public List<Feature> loggingFeatures(MessageLoggingFeature messageLoggingFeature) {
    final List<Feature> features = new ArrayList<>();
    features.add(messageLoggingFeature);
    return features;
  }

  @Autowired
  StartupEventNotifier startupEventNotifier;

  @Bean
  CamelContextConfiguration contextConfiguration() {
    return new CamelContextConfiguration() {
      @Override
      public void beforeApplicationStart(CamelContext camelContext) {
        camelContext.getManagementStrategy().addEventNotifier(startupEventNotifier);
      }
      @Override
      public void afterApplicationStart(CamelContext camelContext) {
        // Do nothing here
      }
    };
  }
}
