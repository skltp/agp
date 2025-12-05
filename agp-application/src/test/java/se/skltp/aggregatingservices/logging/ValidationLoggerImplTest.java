package se.skltp.aggregatingservices.logging;

import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.Message;
import org.apache.logging.log4j.message.StringMapMessage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ValidationLoggerImplTest {
  public static final String LOG_MESSAGE = "LogMessage";
  public static final String SERVICE = "service";
  public static final String MESSAGE = "message";
  public static final String RECEIVER_ID = "receiverid";
  public static final String BUSINESS_CORRELATION_ID = "BusinessCorrelationId";
  public static final String VALIDATION_ERR = "validation-err";
  public static final String THE_LOGICAL_ADDRESS = "theLogicalAddress";
  public static final String THE_CORRELATION_ID = "theCorrelationId";

  @DisplayName("addErrors adds received error messages to the list")
  @Test
  void testAddErrors() {
    ValidationLoggerImpl validationLogger = new ValidationLoggerImpl();
    Message message = mockMessage();
    // create ErrorRecords matching the new structure (message string used directly)
    var err1 = new CollectingErrorHandler.ErrorRecord(null, "Validation error: aaa");
    var err2 = new CollectingErrorHandler.ErrorRecord(null, "Validation error: bbb");
    List<CollectingErrorHandler.ErrorRecord> errorMessages = List.of(err1, err2);

    validationLogger.addErrors(errorMessages, "service", message);

    var errorList = validationLogger.getUniqueMessages();
    assertEquals(2, errorList.get().size());
    for (var errorMessage: errorMessages) {
      checkMessageInErrorList("service", errorMessage, errorList);
    }
  }

  @DisplayName("flush clears the error list")
  @Test
  void testFlush() {
    ValidationLoggerImpl validationLogger = new ValidationLoggerImpl();
    Message message = mockMessage();
    var err1 = new CollectingErrorHandler.ErrorRecord(null, "Validation error: aaa");
    var err2 = new CollectingErrorHandler.ErrorRecord(null, "Validation error: bbb");
    List<CollectingErrorHandler.ErrorRecord> errorMessages = List.of(err1, err2);

    validationLogger.addErrors(errorMessages, "service", message);

    var errorList = validationLogger.getUniqueMessages();
    assertEquals(2, errorList.get().size());

    validationLogger.flush();

    assertEquals(0, errorList.get().size());
  }

  @DisplayName("Only unique error messages are retained")
  @Test
  void testUniqueErrors() {
    ValidationLoggerImpl validationLogger = new ValidationLoggerImpl();
    Message message = mockMessage();
    List<CollectingErrorHandler.ErrorRecord> errorMessages1 = List.of(
      new CollectingErrorHandler.ErrorRecord(null, "Validation error: aaa"),
      new CollectingErrorHandler.ErrorRecord(null, "Validation error: bbb")
    );
    List<CollectingErrorHandler.ErrorRecord> errorMessages2 = List.of(
      new CollectingErrorHandler.ErrorRecord(null, "Validation error: bbb"),
      new CollectingErrorHandler.ErrorRecord(null, "Validation error: ccc")
    );
    List<String> services = List.of("service1", "service2");

    for (var service: services) {
      validationLogger.addErrors(errorMessages1, service, message);
      validationLogger.addErrors(errorMessages2, service, message);
    }

    List<CollectingErrorHandler.ErrorRecord> allErrorMessages = Stream.concat(errorMessages1.stream(), errorMessages2.stream()).toList();
    var errorList = validationLogger.getUniqueMessages();
    assertEquals(6, errorList.get().size());
    for (var service: services) {
      for (var errorRecord : allErrorMessages) {
        checkMessageInErrorList(service, errorRecord, errorList);
      }
    }
  }

  @DisplayName("Locator fields are included in logged messages")
  @Test
  void testLocatorFieldsIncluded() {
    ValidationLoggerImpl validationLogger = new ValidationLoggerImpl();
    Message message = mockMessage();

    var locator = new CollectingErrorHandler.LocatorInfo("pubId", "sysId", 11, 22);
    var err = new CollectingErrorHandler.ErrorRecord(locator, "Validation error: with-locator");
    validationLogger.addErrors(List.of(err), "serviceX", message);

    var errorList = validationLogger.getUniqueMessages();
    var key = new ValidationLoggerImpl.LogEntryKey("serviceX", THE_LOGICAL_ADDRESS, err.message());
    assertTrue(errorList.get().containsKey(key));
    var msg = errorList.get().get(key);
    assertEquals("pubId", msg.get("location.publicId"));
    assertEquals("sysId", msg.get("location.systemId"));
    assertEquals("11", msg.get("location.lineNumber"));
    assertEquals("22", msg.get("location.columnNumber"));
  }

  @DisplayName("register adds service to internal list")
  @Test
  void testRegisterAddsService() throws Exception {
    ValidationLoggerImpl validationLogger = new ValidationLoggerImpl();
    // access private field 'services' via reflection
    var servicesField = ValidationLoggerImpl.class.getDeclaredField("services");
    servicesField.setAccessible(true);
    @SuppressWarnings("unchecked")
    List<String> services = (List<String>) servicesField.get(validationLogger);
    assertTrue(services.isEmpty());

    validationLogger.register("myService");
    assertTrue(services.contains("myService"));
  }

  private Message mockMessage() {
    Message message = mock(Message.class);
    Exchange exchange = mock(Exchange.class);
    when(message.getExchange()).thenReturn(exchange);
    when(message.get("LogicalAddress")).thenReturn(THE_LOGICAL_ADDRESS);
    when(exchange.get("CorrelationId")).thenReturn(THE_CORRELATION_ID);
    return message;
  }

  private void checkMessageInErrorList(String service, CollectingErrorHandler.ErrorRecord errorRecord, AtomicReference<ConcurrentMap<ValidationLoggerImpl.LogEntryKey, StringMapMessage>> errorList) {
    var key = new ValidationLoggerImpl.LogEntryKey(service, THE_LOGICAL_ADDRESS, errorRecord.message());
    assertTrue(errorList.get().containsKey(key));
    var message0 = errorList.get().get(key);
    assertEquals(VALIDATION_ERR, message0.get(LOG_MESSAGE));
    assertEquals(service, message0.get(SERVICE));
    assertEquals(THE_LOGICAL_ADDRESS, message0.get(RECEIVER_ID));
    assertEquals(THE_CORRELATION_ID, message0.get(BUSINESS_CORRELATION_ID));
    assertEquals(errorRecord.message(), message0.get(MESSAGE));
  }
}
