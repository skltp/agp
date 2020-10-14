package se.skltp.aggregatingservices.route;

import org.apache.camel.EndpointInject;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import se.skltp.aggregatingservices.config.TestStubConfiguration;
import se.skltp.aggregatingservices.processors.FindContentResponseProcessor;
import se.skltp.aggregatingservices.riv.itintegration.engagementindex.findcontent.v1.rivtabp21.FindContentResponderInterface;

@Component
public class FindContentStubRoute extends RouteBuilder {

  public static final String FINDCONTENT_WSDL_PATH = "/schemas/TD_ENGAGEMENTINDEX_1_0_R/interactions/FindContentInteraction/FindContentInteraction_1.0_RIVTABP21.wsdl";
  public static final String FINDCONTENT_SERVICECLASS = FindContentResponderInterface.class.getName();

  private static final String SERVICE_CONFIGURATION = "cxf:%s"
      + "?wsdlURL=%s"
      + "&serviceClass=%s"
      + "&portName={urn:riv:itintegration:engagementindex:FindContent:1:rivtabp21}FindContentResponderPort";

  protected String serviceAddress;

  @EndpointInject(uri="mock:findcontent:input")
  MockEndpoint mock;

  @Autowired
  FindContentResponseProcessor findContentResponseProcessor;

  @Autowired
  public FindContentStubRoute(TestStubConfiguration testStubConfiguration) {
    serviceAddress = String
        .format(SERVICE_CONFIGURATION, testStubConfiguration.getFindContentAddress(), FINDCONTENT_WSDL_PATH, FINDCONTENT_SERVICECLASS);
  }

  @Override
  public void configure() throws Exception {
    from(serviceAddress).id("FindContent.route")
        .to("mock:findcontent:input")
        .process(findContentResponseProcessor);
  }

  public MockEndpoint getMock() {
    return mock;
  }
}
