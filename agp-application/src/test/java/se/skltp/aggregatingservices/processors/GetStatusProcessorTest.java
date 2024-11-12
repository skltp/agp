package se.skltp.aggregatingservices.processors;

import org.apache.camel.Exchange;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.mock.MockEndpoint;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.info.BuildProperties;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import se.skltp.aggregatingservices.service.TakCacheServiceImpl;
import org.apache.camel.test.junit5.CamelTestSupport;
import se.skltp.takcache.TakCache;

@ExtendWith({SpringExtension.class})
@EnableAutoConfiguration
@ContextConfiguration(classes = {GetStatusProcessor.class, TakCacheServiceImpl.class})
@TestPropertySource("classpath:application.properties")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class GetStatusProcessorTest extends CamelTestSupport {

  @MockBean(name = "takCache")
  private TakCache takCache;

  @Autowired GetStatusProcessor getStatusProcessor;

  @Autowired BuildProperties buildProperties;

  @Produce("direct:getStatus")
  protected ProducerTemplate template;

  @Test
  public void getStatusTest() {
    String name = buildProperties.getName();
    String version = buildProperties.getVersion();

    MockEndpoint resultEndpoint = resolveMandatoryEndpoint("mock:result", MockEndpoint.class);
    template.send(resultEndpoint, getStatusProcessor);
    Exchange exchange;
    exchange = resultEndpoint.getExchanges().get(0);
    assert (exchange.getIn().getBody().toString().contains("Name\" : \"" + name));
    assert (exchange.getIn().getBody().toString().contains("Version\" : \"" + version));
    assert (exchange.getIn().getBody().toString().contains("ServiceStatus\" : \"Started"));
  }
}
