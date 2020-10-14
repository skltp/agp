package se.skltp.aggregatingservices.processors;

import static org.junit.Assert.assertEquals;
import static se.skltp.aggregatingservices.constants.AgpProperties.AGP_ORIGINAL_QUERY;
import static se.skltp.aggregatingservices.constants.AgpProperties.AGP_SERVICE_HANDLER;
import static se.skltp.aggregatingservices.constants.AgpProperties.AGP_TAK_CONTRACT_NAME;
import static se.skltp.aggregatingservices.data.TestDataDefines.TEST_RR_ID_MANY_HITS_NO_ERRORS;

import java.util.List;
import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.support.DefaultExchange;
import org.apache.camel.test.spring.junit5.MockEndpoints;
import org.apache.cxf.message.MessageContentsList;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import se.skltp.aggregatingservices.constants.AgpHeaders;
import se.skltp.aggregatingservices.riv.itintegration.engagementindex.findcontentresponder.v1.FindContentResponseType;
import se.skltp.aggregatingservices.utils.AgpServiceFactoryImpl;
import se.skltp.aggregatingservices.utils.EngagementProcessingStatusUtil;
import se.skltp.aggregatingservices.utils.FindContentUtil;
import se.skltp.aggregatingservices.utils.RequestUtil;

@ExtendWith({SpringExtension.class})
@ContextConfiguration(classes = {CreateRequestListProcessor.class})
@TestPropertySource("classpath:application.properties")
@MockEndpoints("direct:end")
public class CreateRequestListProcessorTest {


  @Autowired
  CreateRequestListProcessor createRequestListProcessor;


  @Test
  public void processWithAllEngagementsAuthorized() throws Exception {
    Exchange ex = createExchange();

    createRequestListProcessor.process(ex);

    List<MessageContentsList> list = (List<MessageContentsList>) ex.getIn().getBody();
    assertEquals(3, list.size());

  }

  private Exchange createExchange() {
    final Exchange ex = new DefaultExchange(new DefaultCamelContext());

    ex.setProperty(AGP_ORIGINAL_QUERY, RequestUtil.createTestMessageContentsList());
    ex.setProperty(AGP_SERVICE_HANDLER, AgpServiceFactoryImpl.createInstance("domain1", "cat1"));
    ex.getIn().setHeader(AgpHeaders.X_VP_SENDER_ID, "sender1");
    ex.getIn().setHeader(AgpHeaders.X_RIVTA_ORIGINAL_SERVICE_CONSUMER_HSA_ID, "org_sender1");
    ex.setProperty(AGP_TAK_CONTRACT_NAME, "ns:1");

    final MessageContentsList messageContentsList = FindContentUtil.createMessageContentsList(TEST_RR_ID_MANY_HITS_NO_ERRORS);
    EngagementProcessingStatusUtil.initAllAsFiltered((FindContentResponseType) messageContentsList.get(0), ex );

    ex.getIn().setBody(messageContentsList);
    return ex;
  }


}
