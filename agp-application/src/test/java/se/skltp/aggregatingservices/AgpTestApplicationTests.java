package se.skltp.aggregatingservices;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest(classes = {AgpApplication.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class AgpTestApplicationTests {

  @Test
  public void contextLoads() {
  }

}
