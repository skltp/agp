package se.skltp.aggregatingservices.processors;

import static se.skltp.aggregatingservices.constants.AgpProperties.LOGICAL_ADDRESS;
import static se.skltp.aggregatingservices.constants.AgpProperties.AGP_SERVICE_HANDLER;

import lombok.extern.log4j.Log4j2;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.component.cxf.common.message.CxfConstants;
import org.apache.cxf.message.MessageContentsList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.skltp.aggregatingservices.api.AgpServiceFactory;
import se.skltp.aggregatingservices.config.EiConfig;
import se.skltp.aggregatingservices.config.VpConfig;
import se.skltp.aggregatingservices.constants.AgpHeaders;
import se.skltp.aggregatingservices.riv.itintegration.engagementindex.findcontentresponder.v1.FindContentType;

@Service
@Log4j2
public class CreateFindContentProcessor implements Processor {

  @Autowired
  VpConfig vpConfig;

  @Autowired
  EiConfig eiConfig;

  @Override
  public void process(Exchange exchange) throws Exception {
    final MessageContentsList originalQueryMessageList = exchange.getIn().getBody(MessageContentsList.class);

    prepareHeaders(exchange);
    MessageContentsList findContent = createFindContentMessageList(exchange, originalQueryMessageList);
    exchange.getIn().setBody(findContent);
  }

  private void prepareHeaders(Exchange exchange) {
    Message in = exchange.getIn();

    in.removeHeader(CxfConstants.OPERATION_NAME);
    in.removeHeader(CxfConstants.OPERATION_NAMESPACE);
    in.removeHeader("SoapAction");
    in.setHeader(AgpHeaders.X_VP_SENDER_ID, eiConfig.getSenderId());
    in.setHeader(AgpHeaders.X_VP_INSTANCE_ID, vpConfig.getInstanceId());

  }

  private MessageContentsList createFindContentMessageList(Exchange exchange, MessageContentsList originalQueryMessageList) {
    AgpServiceFactory agpServiceProcessor =  exchange.getProperty(AGP_SERVICE_HANDLER, AgpServiceFactory.class);
    FindContentType findContent = agpServiceProcessor.createFindContent(originalQueryMessageList);
    MessageContentsList findContentMessageList = new MessageContentsList();
    exchange.setProperty(LOGICAL_ADDRESS, eiConfig.getLogicalAddress());

    findContentMessageList.add(eiConfig.getLogicalAddress());
    findContentMessageList.add(findContent);
    return findContentMessageList;
  }

}
