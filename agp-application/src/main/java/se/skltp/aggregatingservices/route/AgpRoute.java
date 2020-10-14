package se.skltp.aggregatingservices.route;

import static se.skltp.aggregatingservices.constants.AgpHeaders.X_VP_SENDER_ID;
import static se.skltp.aggregatingservices.constants.AgpProperties.AGP_ORIGINAL_QUERY;
import static se.skltp.aggregatingservices.constants.AgpProperties.INCOMMING_VP_SENDER_ID;
import static se.skltp.aggregatingservices.constants.AgpProperties.LOGICAL_ADDRESS;

import org.apache.camel.builder.RouteBuilder;
import org.apache.cxf.message.MessageContentsList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.stereotype.Component;
import se.skltp.aggregatingservices.AgpCxfEndpointConfigurer;
import se.skltp.aggregatingservices.aggregate.AgpAggregationStrategy;
import se.skltp.aggregatingservices.config.EiConfig;
import se.skltp.aggregatingservices.processors.CheckInboundHeadersProcessor;
import se.skltp.aggregatingservices.processors.CreateFindContentProcessor;
import se.skltp.aggregatingservices.processors.CreateRequestListProcessor;
import se.skltp.aggregatingservices.processors.CreateResponseProcessor;
import se.skltp.aggregatingservices.processors.FilterFindContentResponseProcessor;

@Component
public class AgpRoute extends RouteBuilder {

  public static final String EI_FINDCONTENT_URI = "cxf://{{ei.findContentUrl}}"
      + "?wsdlURL=/schemas/TD_ENGAGEMENTINDEX_1_0_R/interactions/FindContentInteraction/FindContentInteraction_1.0_RIVTABP21.wsdl"
      + "&serviceClass=se.skltp.aggregatingservices.riv.itintegration.engagementindex.findcontent.v1.rivtabp21.FindContentResponderInterface"
      + "&portName={urn:riv:itintegration:engagementindex:FindContent:1:rivtabp21}FindContentResponderPort"
      + "&dataFormat=POJO"
      + "&cxfConfigurer=#eiEndpointConfBean"
      + "&properties.use.async.http.conduit=%s"
      + "&properties.org.apache.cxf.transport.http.async.MAX_CONNECTIONS=10000"
      + "&properties.org.apache.cxf.transport.http.async.MAX_PER_HOST_CONNECTIONS=2000";

  @Value("${aggregate.timeout:29000}")
  Long aggregationTimeout;

  @Autowired
  GenericApplicationContext applicationContext;

  @Autowired
  EiConfig eiConfig;

  @Autowired
  CreateRequestListProcessor createRequestListProcessor;

  @Autowired
  CreateFindContentProcessor createFindContentProcessor;

  @Autowired
  FilterFindContentResponseProcessor filterFindContentResponseProcessor;

  @Autowired
  CreateResponseProcessor createResponseProcessor;

  @Autowired
  AgpAggregationStrategy agpAggregationStrategy;

  @Autowired
  CheckInboundHeadersProcessor checkInboundHeadersProcessor;

  @Override
  public void configure() throws Exception {


    applicationContext.registerBean("eiEndpointConfBean", AgpCxfEndpointConfigurer.class,
        ()->new AgpCxfEndpointConfigurer(eiConfig.getReceiveTimeout(), eiConfig.getConnectTimeout(), false, true));

    String findContentServiceAddress = String.format(EI_FINDCONTENT_URI,
        eiConfig.getUseAyncHttpConduit() );

     from("direct:agproute").id("agp-service-route").streamCaching()
        .process(checkInboundHeadersProcessor)
        .setProperty(AGP_ORIGINAL_QUERY, body())
        .setProperty(INCOMMING_VP_SENDER_ID, header(X_VP_SENDER_ID))
        .process(createFindContentProcessor)
        .to(findContentServiceAddress).id("to.findcontent")
        .setHeader(X_VP_SENDER_ID, exchangeProperty(INCOMMING_VP_SENDER_ID))
        .process(filterFindContentResponseProcessor)
        .process(createRequestListProcessor)
        .removeHeaders("{{headers.request.filter}}")
        .split(body()).executorServiceRef("splitterThreadPool")
                    .timeout(aggregationTimeout)
                    .parallelProcessing(true)
                    .aggregationStrategy(agpAggregationStrategy)
            .setProperty(LOGICAL_ADDRESS).exchange(ex -> ex.getIn().getBody(MessageContentsList.class).get(0))
            .removeHeader("breadcrumbId")
            .toD("direct:${exchangeProperty.AgpServiceComponentId}")
        .end()
        .process(createResponseProcessor)
        .removeHeaders("{{headers.response.filter}}")
        .removeProperty(LOGICAL_ADDRESS);

  }


}