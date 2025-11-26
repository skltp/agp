package se.skltp.aggregatingservices.logging;

import org.apache.cxf.Bus;
import org.apache.cxf.BusFactory;
import org.apache.cxf.endpoint.Endpoint;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.Message;
import org.apache.cxf.service.Service;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.service.model.ServiceInfo;
import org.apache.cxf.wsdl11.WSDLServiceFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXParseException;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ValidationLogInterceptorTest {

  public static final String MESSAGE_WSDL = "classpath:soap/hello_world.wsdl";
  public static final String CORRECT_MESSAGE = "soap/hello_world.xml";
  public static final String INCORRECT_MESSAGE = "soap/hello_world_error.xml";
  public static final String SERVICE_NAME = "serviceName";
  public static final String MESSAGE_INBOUND = "org.apache.cxf.message.inbound";

  @DisplayName("It can validate SOAP messages against the schema")
  @Test
  void testSuccessfulValidation() {
    ValidationLogger validationLogger = mock(ValidationLogger.class);
    ValidationLogInterceptor validationLogInterceptor = new ValidationLogInterceptor(SERVICE_NAME, validationLogger);
    for (int ctr = 0; ctr < 3; ctr++) {
      Message message = mockMessage(true, CORRECT_MESSAGE);

      validationLogInterceptor.handleMessage(message);

      verify(message).getContent(any());
      assertNotNull(validationLogInterceptor.validator);
    }
    verify(validationLogger, times(0)).addErrors(anyList(), anyString(), any());
  }

  @DisplayName("Errors in the schema are reported to the ValidationLogger")
  @Test
  void testValidationError() {
    ValidationLogger validationLogger = mock(ValidationLogger.class);
    ValidationLogInterceptor validationLogInterceptor = new ValidationLogInterceptor(SERVICE_NAME, validationLogger);
    for (int ctr = 0; ctr < 3; ctr++) {
      Message message = mockMessage(true, INCORRECT_MESSAGE);

      validationLogInterceptor.handleMessage(message);

      verify(validationLogger, times(ctr + 1)).addErrors(anyList(), anyString(), any());
    }
  }

  @DisplayName("Only inbound messages are considered")
  @Test
  void testMessageNotInbound() {
    ValidationLogger validationLogger = mock(ValidationLogger.class);
    ValidationLogInterceptor validationLogInterceptor = new ValidationLogInterceptor(SERVICE_NAME, validationLogger);
    Message message = mockMessage(false, CORRECT_MESSAGE);

    validationLogInterceptor.handleMessage(message);

    verify(validationLogger, times(0)).addErrors(anyList(), anyString(), any());
    verify(message, times(0)).getContent(any());
  }

  @DisplayName("Messages without content are handled gracefully")
  @Test
  void testMessageWithNoContent() {
    ValidationLogger validationLogger = mock(ValidationLogger.class);
    ValidationLogInterceptor validationLogInterceptor = new ValidationLogInterceptor(SERVICE_NAME, validationLogger);
    Message message = mockMessage(true, CORRECT_MESSAGE);
    when(message.getContent(any())).thenReturn(null);

    validationLogInterceptor.handleMessage(message);

    verify(validationLogger, times(0)).addErrors(anyList(), anyString(), any());
    verify(message).getContent(any());
  }

  @DisplayName("IO errors when reading messages are handled gracefully")
  @Test
  void testMessageIOError() throws IOException {
    ValidationLogger validationLogger = mock(ValidationLogger.class);
    ValidationLogInterceptor validationLogInterceptor = new ValidationLogInterceptor(SERVICE_NAME, validationLogger);
    Message message = mockMessage(true, CORRECT_MESSAGE);
    InputStream inputStream = mock(InputStream.class);
    when(inputStream.readAllBytes()).thenThrow(IOException.class);
    when(message.getContent(InputStream.class)).thenReturn(inputStream);

    validationLogInterceptor.handleMessage(message);

    verify(validationLogger, times(0)).addErrors(anyList(), anyString(), any());
    verify(message).getContent(any());
  }

  @DisplayName("Endpoint service info missing is handled gracefully")
  @Test
  void testEndpointServiceInfoMissing() {
    ValidationLogger validationLogger = mock(ValidationLogger.class);
    ValidationLogInterceptor validationLogInterceptor = new ValidationLogInterceptor(SERVICE_NAME, validationLogger);
    Message message = mockMessage(true, CORRECT_MESSAGE);
    when(message.getExchange().getEndpoint().getEndpointInfo().getService()).thenReturn(null);

    validationLogInterceptor.handleMessage(message);

    verify(message).getContent(any());
    assertNull(validationLogInterceptor.validator);
    verify(validationLogger, times(0)).addErrors(anyList(), anyString(), any());
  }

  @Test
  void testErrorHandler() {
    var errorHandler = new ValidationLogInterceptor.CollectingErrorHandler();
    assertFalse(errorHandler.hasErrors());
    errorHandler.warning(new SAXParseException("warning!", "", "", 42, 42));
    assertTrue(errorHandler.hasErrors());
    assertEquals(1, errorHandler.getErrors().size());
    errorHandler.error(new SAXParseException("error!", "", "", 42, 42));
    assertTrue(errorHandler.hasErrors());
    assertEquals(2, errorHandler.getErrors().size());
    errorHandler.fatalError(new SAXParseException("fatalError!", "", "", 42, 42));
    assertTrue(errorHandler.hasErrors());
    assertEquals(3, errorHandler.getErrors().size());
  }

  private Message mockMessage(boolean inbound, String messageResource) {
    Message message = mock(Message.class);
    when(message.get(MESSAGE_INBOUND)).thenReturn(inbound);
    InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(messageResource);
    when(message.getContent(InputStream.class)).thenReturn(is);

    Exchange exchange = mockExchange();
    when(message.getExchange()).thenReturn(exchange);
    return message;
  }

  Exchange mockExchange() {
    Bus bus = BusFactory.newInstance().createBus();

    WSDLServiceFactory factory = new WSDLServiceFactory(bus, MESSAGE_WSDL, null);
    Service cxfService = factory.create();
    ServiceInfo serviceInfo = cxfService.getServiceInfos().get(0);
    Exchange exchange = mock(Exchange.class);
    Endpoint endpoint = mock(Endpoint.class);
    EndpointInfo endpointInfo = mock(EndpointInfo.class);
    when(endpointInfo.getService()).thenReturn(serviceInfo);
    when(endpoint.getEndpointInfo()).thenReturn(endpointInfo);
    when(exchange.getEndpoint()).thenReturn(endpoint);
    return exchange;
  }

}
