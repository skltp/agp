package se.skltp.aggregatingservices.processors;

import static se.skltp.aggregatingservices.constants.AgpProperties.AGP_ORIGINAL_QUERY;
import static se.skltp.aggregatingservices.constants.AgpProperties.AGP_SERVICE_HANDLER;
import static se.skltp.aggregatingservices.constants.AgpProperties.ENGAGEMENT_PROCESSING_RESULT;
import static se.skltp.aggregatingservices.constants.AgpProperties.EXPECTED_IN_PROCESSING_STATUS;
import static se.skltp.aggregatingservices.constants.AgpProperties.LOG_ENGAGEMENT_PROCESSING_RESULT;
import static se.skltp.aggregatingservices.constants.AgpProperties.LOG_PROCESSING_COUNT_FAIL;
import static se.skltp.aggregatingservices.constants.AgpProperties.LOG_PROCESSING_COUNT_TOT;
import static se.skltp.aggregatingservices.constants.AgpProperties.LOG_PROCESSING_STATUS;
import static se.skltp.agp.riv.interoperability.headers.v1.StatusCodeEnum.NO_DATA_SYNCH_FAILED;

import java.io.StringReader;
import java.util.List;
import java.util.stream.Collectors;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import lombok.extern.log4j.Log4j2;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.cxf.binding.soap.SoapHeader;
import org.apache.cxf.headers.Header;
import org.apache.cxf.headers.Header.Direction;
import org.apache.cxf.message.MessageContentsList;
import org.apache.cxf.staxutils.StaxUtils;
import org.springframework.stereotype.Service;
import se.skltp.aggregatingservices.aggregate.AggregatedResponseResults;
import se.skltp.aggregatingservices.api.AgpServiceFactory;
import se.skltp.aggregatingservices.logging.ProcessingStatusLogFormat;
import se.skltp.aggregatingservices.utils.JaxbUtil;
import se.skltp.aggregatingservices.utils.ProcessingStatusUtil;
import se.skltp.agp.riv.interoperability.headers.v1.ObjectFactory;
import se.skltp.agp.riv.interoperability.headers.v1.ProcessingStatusRecordType;
import se.skltp.agp.riv.interoperability.headers.v1.ProcessingStatusType;

@Service
@Log4j2
public class CreateResponseProcessor implements Processor {

  private static final ObjectFactory OF_HEADERS = new ObjectFactory();
  private static final JaxbUtil jaxbUtil = new JaxbUtil(ProcessingStatusType.class);

  @Override
  public void process(Exchange exchange) throws Exception {
    AggregatedResponseResults aggregatedResponseResults = getAggregatedResponseResults(exchange);
    AgpServiceFactory agpServiceProcessor = exchange.getProperty(AGP_SERVICE_HANDLER, AgpServiceFactory.class);
    MessageContentsList originalRequest = exchange.getProperty(AGP_ORIGINAL_QUERY, MessageContentsList.class);

    validateProcessingStatus(exchange, aggregatedResponseResults);

    Object responseObject = agpServiceProcessor
        .createAggregatedResponseObject(originalRequest, aggregatedResponseResults.getResponseObjects());
    exchange.getIn().setBody(responseObject);

    insertProcessingStatusHeader(exchange, aggregatedResponseResults.getProcessingStatus());

    ProcessingStatusLogFormat processingStatusLogFormat = new ProcessingStatusLogFormat(
        aggregatedResponseResults.getProcessingStatus());
    exchange.setProperty(LOG_PROCESSING_STATUS, processingStatusLogFormat.getProcStatus());
    exchange.setProperty(LOG_PROCESSING_COUNT_TOT, processingStatusLogFormat.getProcStatusCountTot());
    exchange.setProperty(LOG_PROCESSING_COUNT_FAIL, processingStatusLogFormat.getProcStatusCountFail());
    exchange.setProperty(LOG_ENGAGEMENT_PROCESSING_RESULT, exchange.getProperty(ENGAGEMENT_PROCESSING_RESULT));

    exchange.getIn().setHeader(Exchange.HTTP_RESPONSE_CODE, 200);
  }

  private void validateProcessingStatus(Exchange exchange, AggregatedResponseResults aggregatedResponseResults) {
    List<String> expectedLogicalAddresses = exchange.getProperty(EXPECTED_IN_PROCESSING_STATUS, List.class);
    if(expectedLogicalAddresses != null){
      final List<ProcessingStatusRecordType> processingStatusList = aggregatedResponseResults.getProcessingStatus().getProcessingStatusList();
      final List<String> actualLogicalAddresses = processingStatusList.stream().map(ProcessingStatusRecordType::getLogicalAddress)
          .collect(Collectors.toList());
      for(String logicalAddress : expectedLogicalAddresses){
        if(!actualLogicalAddresses.contains(logicalAddress)) {
          final ProcessingStatusRecordType statusRecord = ProcessingStatusUtil
              .createStatusRecord(logicalAddress, NO_DATA_SYNCH_FAILED, new Exception("Unknown error"));
          processingStatusList.add(statusRecord);
        }
      }
    }
  }

  private AggregatedResponseResults getAggregatedResponseResults(Exchange exchange) {
    AggregatedResponseResults aggregatedResponseResults = exchange.getIn().getBody(AggregatedResponseResults.class);
    if (aggregatedResponseResults == null) {
      return new AggregatedResponseResults();
    }
    return aggregatedResponseResults;
  }

  private void insertProcessingStatusHeader(Exchange exchange, ProcessingStatusType processingStatus) throws XMLStreamException {
    String xmlStatus = jaxbUtil.marshal(OF_HEADERS.createProcessingStatus(processingStatus));
    log.info("processingStatus:\n{}", xmlStatus);

    SoapHeader newHeader = new SoapHeader(new QName("urn:riv:interoperability:headers:1", "ProcessingStatus")
        , StaxUtils.read(new StringReader(xmlStatus)).getDocumentElement());
    newHeader.setDirection(Direction.DIRECTION_OUT);
    List<SoapHeader> soapHeaders = (List) exchange.getIn().getHeader(Header.HEADER_LIST);
    soapHeaders.add(newHeader);
  }
}
