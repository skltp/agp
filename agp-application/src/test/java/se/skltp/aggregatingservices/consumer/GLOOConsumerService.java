package se.skltp.aggregatingservices.consumer;

import static se.skltp.aggregatingservices.data.TestDataDefines.SAMPLE_SENDER_ID;
import static se.skltp.aggregatingservices.data.TestDataDefines.TEST_LOGICAL_ADDRESS_1;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.Message;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.support.DefaultExchange;
import org.apache.cxf.binding.soap.SoapFault;
import org.apache.cxf.binding.soap.SoapHeader;
import org.apache.cxf.headers.Header;
import org.apache.cxf.message.MessageContentsList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.skltp.aggregatingservices.constants.AgpHeaders;
import se.skltp.aggregatingservices.riv.clinicalprocess.healthcond.actoutcome.getaggregatedlaboratoryorderoutcome.GLOOAgpServiceConfiguration;
import se.skltp.aggregatingservices.utils.FindContentUtil;
import se.skltp.aggregatingservices.utils.JaxbUtil;
import se.skltp.aggregatingservices.utils.RequestUtil;
import se.skltp.aggregatingservices.utils.ServiceResponse;
import se.skltp.agp.riv.interoperability.headers.v1.ProcessingStatusType;

@Service
public class GLOOConsumerService implements ConsumerService {

  public static final String GLOO_SERVICE_ADDRESS = "cxf:%s"
      + "?wsdlURL=%s"
      + "&serviceClass=%s";

  @Produce
  protected ProducerTemplate template;

  @Autowired
  GLOOAgpServiceConfiguration serviceConfiguration;

  private static final JaxbUtil jaxbUtil = new JaxbUtil(ProcessingStatusType.class);

  @Override
  public ServiceResponse callService(String logicalAddress, String patientId, Map<String, Object> additionalHeaders) {
    final Map<String, Object> headers = prepareHeaders(SAMPLE_SENDER_ID, SAMPLE_SENDER_ID, additionalHeaders);
    return callServiceWithHeaders(logicalAddress, patientId, prepareHeaders(logicalAddress, patientId, headers));
  }

  @Override
  public ServiceResponse callService(String patientId) {
    return callService(SAMPLE_SENDER_ID, SAMPLE_SENDER_ID, TEST_LOGICAL_ADDRESS_1, patientId);
  }

  public ServiceResponse callService(String logicalAddress, String patientId) {
    return callService(SAMPLE_SENDER_ID, SAMPLE_SENDER_ID, logicalAddress, patientId);
  }

  public ServiceResponse callService(String senderId, String originalId, String logicalAddress, String patientId) {
    return callServiceWithHeaders(logicalAddress, patientId, prepareHeaders(senderId, originalId, null));
  }


  private ServiceResponse callServiceWithHeaders(String logicalAddress, String patientId, Map<String, Object> headers) {

    final MessageContentsList testRequest = RequestUtil.createTestMessageContentsList(logicalAddress, patientId);

    final Exchange exchange = new DefaultExchange(template.getCamelContext());
    exchange.getIn().setBody(testRequest);
    exchange.getIn().setHeaders(headers);
    exchange.setPattern(ExchangePattern.InOut);
    final Message response = template.send(getAddress(), exchange).getOut();
    return createServiceResponse(response);
  }

  public ServiceResponse callServiceWithWrongContract() {
    Map<String, Object> headers = prepareHeaders("senderId", "originalId", null);

    final MessageContentsList faultyTestRequest = FindContentUtil.createRequestMessageContentsList("123456", "patientId");

    final Exchange exchange = new DefaultExchange(template.getCamelContext());
    exchange.getIn().setBody(faultyTestRequest);
    exchange.getIn().setHeaders(headers);
    exchange.setPattern(ExchangePattern.InOut);
    final Message response = template.send(getFaultyProducerAddress(), exchange).getOut();
    return createServiceResponse(response);
  }

  private Map<String, Object> prepareHeaders(String senderId, String originalId, Map<String, Object> additionalHeaders) {

    Map<String, Object> headers = new HashMap();
    headers.put(AgpHeaders.X_RIVTA_ORIGINAL_SERVICE_CONSUMER_HSA_ID, originalId);
    headers.put(AgpHeaders.X_VP_SENDER_ID, senderId);
    headers.put(AgpHeaders.X_SKLTP_CORRELATION_ID, "test-corr-id");
    if (additionalHeaders != null) {
      headers.putAll(additionalHeaders);
    }
    return headers;
  }

  private ServiceResponse createServiceResponse(Message response) {
    ServiceResponse serviceResponse = new ServiceResponse();

    ProcessingStatusType processingStatusType = getProcessingStatus(response);
    serviceResponse.setProcessingStatus(processingStatusType);
    serviceResponse.setHeaders(response.getHeaders());
    serviceResponse.setResponseCode(response.getHeader(Exchange.HTTP_RESPONSE_CODE, Integer.class));
    MessageContentsList contentsList = response.getBody(MessageContentsList.class);
    if (contentsList != null) {
      serviceResponse.setObject(contentsList.get(0));
    } else {
      serviceResponse.setSoapFault(response.getExchange().getException(SoapFault.class));
    }
    return serviceResponse;
  }

  private ProcessingStatusType getProcessingStatus(Message response) {
    List<SoapHeader> soapHeaders = (List<SoapHeader>) response.getHeader(Header.HEADER_LIST);
    if (soapHeaders != null && !soapHeaders.isEmpty()) {
      return (ProcessingStatusType) jaxbUtil.unmarshal(soapHeaders.get(0).getObject());
    }
    return null;
  }

  protected String getAddress() {
    return String.format(GLOO_SERVICE_ADDRESS
        , serviceConfiguration.getInboundServiceURL()
        , serviceConfiguration.getInboundServiceWsdl()
        , serviceConfiguration.getInboundServiceClass());
  }

  protected String getFaultyProducerAddress() {
    return String.format(GLOO_SERVICE_ADDRESS
        , serviceConfiguration.getInboundServiceURL()
        , "/schemas/TD_ENGAGEMENTINDEX_1_0_R/interactions/FindContentInteraction/FindContentInteraction_1.0_RIVTABP21.wsdl"
        , "se.skltp.aggregatingservices.riv.itintegration.engagementindex.findcontent.v1.rivtabp21.FindContentResponderInterface"
    );
  }
}
