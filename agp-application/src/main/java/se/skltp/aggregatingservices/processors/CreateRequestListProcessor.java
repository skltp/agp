package se.skltp.aggregatingservices.processors;

import static se.skltp.aggregatingservices.constants.AgpProperties.AGP_ORIGINAL_QUERY;
import static se.skltp.aggregatingservices.constants.AgpProperties.AGP_SERVICE_HANDLER;
import static se.skltp.aggregatingservices.constants.AgpProperties.EXPECTED_IN_PROCESSING_STATUS;

import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.log4j.Log4j2;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.cxf.message.MessageContentsList;
import org.springframework.stereotype.Service;
import se.skltp.aggregatingservices.api.AgpServiceFactory;
import se.skltp.aggregatingservices.riv.itintegration.engagementindex.findcontentresponder.v1.FindContentResponseType;
import se.skltp.aggregatingservices.utils.EngagementProcessingStatusUtil;

@Service
@Log4j2
public class CreateRequestListProcessor implements Processor {


  @Override
  public void process(Exchange exchange) {

    MessageContentsList originalQuery = exchange.getProperty(AGP_ORIGINAL_QUERY, MessageContentsList.class);
    AgpServiceFactory agpServiceProcessor = exchange.getProperty(AGP_SERVICE_HANDLER, AgpServiceFactory.class);

    MessageContentsList findContentMessageList = exchange.getIn().getBody(MessageContentsList.class);
    final FindContentResponseType findContentResponse = (FindContentResponseType) findContentMessageList.get(0);

    List<MessageContentsList> queryObjects = agpServiceProcessor.createRequestList(originalQuery, findContentResponse);

    EngagementProcessingStatusUtil.updateWithNotFilteredByService(queryObjects, exchange);

    List<String> logicalAddresses = queryObjects.stream().map(mc->(String)mc.get(0)).collect(Collectors.toList());
    exchange.setProperty(EXPECTED_IN_PROCESSING_STATUS, logicalAddresses);
    exchange.getIn().setBody(queryObjects);

  }

}
