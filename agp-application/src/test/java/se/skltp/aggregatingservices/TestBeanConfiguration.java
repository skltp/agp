package se.skltp.aggregatingservices;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import se.skltp.aggregatingservices.utils.TestLogAppender;

@Configuration
public class TestBeanConfiguration {

  @Bean
  public TestLogAppender testLogAppender() {
    return TestLogAppender.getInstance();
  }

}
