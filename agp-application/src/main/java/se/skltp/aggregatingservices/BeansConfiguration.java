package se.skltp.aggregatingservices;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.cxf.feature.Feature;
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

  @Bean
  public AtomicBoolean preStopStatus() {
    return new AtomicBoolean(false);
  }
}
