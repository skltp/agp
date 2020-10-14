package se.skltp.aggregatingservices;

import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@CamelSpringBootTest
@SpringBootTest(classes = GLOOApplication.class)
public class GLOOApplicationTest {
  @Test
  public void contextLoads() {
  }

}
