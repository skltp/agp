package se.skltp.aggregatingservices.integrationtests;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.boot.test.context.SpringBootTest;
import se.skltp.aggregatingservices.AgpApplication;

@CamelSpringBootTest
@SpringBootTest(classes = {AgpApplication.class})
public class GetStatusIT {

  @Produce
  protected ProducerTemplate producerTemplate;

  @Autowired
  BuildProperties buildProperties;

  @Test
  public void getStatusResponseTest() {

    String name = buildProperties.getName();
    String version = buildProperties.getVersion();

    String statusResponse = producerTemplate.requestBody("{{agp.status.url}}", "body", String.class);
    assertTrue (statusResponse .startsWith("{") && statusResponse .endsWith("}"));
    assertTrue (statusResponse.contains("Name\" : \"" + name));
    assertTrue (statusResponse.contains("Version\" : \"" + version));
    assertTrue (statusResponse.contains("ServiceStatus\" : \"Started"));
  }

}

