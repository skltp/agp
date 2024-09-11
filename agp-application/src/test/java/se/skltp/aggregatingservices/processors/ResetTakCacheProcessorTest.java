package se.skltp.aggregatingservices.processors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.List;
import org.apache.camel.Exchange;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.test.junit5.CamelTestSupport;
import org.apache.camel.test.spring.junit5.MockEndpoints;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import se.skltp.aggregatingservices.service.TakCacheServiceImpl;
import se.skltp.takcache.TakCache;
import se.skltp.takcache.TakCacheLog;

@ExtendWith({SpringExtension.class})
@ContextConfiguration(classes = {ResetTakCacheProcessor.class, TakCacheServiceImpl.class})
@TestPropertySource("classpath:application.properties")
@MockEndpoints("direct:end")
public class ResetTakCacheProcessorTest extends CamelTestSupport {

  @MockBean(name = "takCache")
  private TakCache takCache;

  @Autowired
  ResetTakCacheProcessor resetTakCacheProcessor;

  @Produce("direct:start")
  protected ProducerTemplate template;

  @BeforeEach
  public void beforeTest() {
    List<String> testLog = new ArrayList<>();
    testLog.add("Test log1");
    testLog.add("Test log2");

    TakCacheLog takCacheLog = mock(TakCacheLog.class);
    Mockito.when(takCacheLog.getLog()).thenReturn(testLog);
    Mockito.when(takCache.refresh(any())).thenReturn(takCacheLog);
  }
  @Test
  public void process() {
    Exchange exchange = template.send(resetTakCacheProcessor);
    assertEquals("<br>Test log1<br>Test log2", exchange.getMessage().getBody(String.class) );
    assertEquals("text/html;", exchange.getMessage().getHeader("Content-Type", String.class) );
    assertEquals(200, exchange.getMessage().getHeader(Exchange.HTTP_RESPONSE_CODE, Integer.class).intValue() );
  }


}