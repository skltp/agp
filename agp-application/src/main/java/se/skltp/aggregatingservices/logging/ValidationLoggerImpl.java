package se.skltp.aggregatingservices.logging;

import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.message.StringMapMessage;
import org.springframework.stereotype.Service;
import org.apache.cxf.message.Message;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

@Service
@Log4j2
public class ValidationLoggerImpl implements ValidationLogger {

  public static final String LOG_MESSAGE = "LogMessage";
  public static final String SERVICE = "service";
  public static final String MESSAGE = "message";
  public static final String RECEIVER_ID = "receiverid";
  public static final String BUSINESS_CORRELATION_ID = "BusinessCorrelationId";
  public static final String VALIDATION_ERR = "validation-err";

  private final List<String> services = new CopyOnWriteArrayList<>();
  private final AtomicBoolean first = new AtomicBoolean(true);

  record LogEntryKey(String service, String receiverId, String message) {
  }

  private final AtomicReference<ConcurrentMap<LogEntryKey, StringMapMessage>> uniqueMessages = new AtomicReference<>(new ConcurrentHashMap<>());

  @Override
  public void addErrors(List<CollectingErrorHandler.ErrorRecord> errors, String service, Message message) {
    errors.forEach(errRecord -> {
      var locator = errRecord.locator();
      // include locator fields in the message as location.publicId etc.
      String receiverId = LogEntryMapper.getLogicalAddress(message);
      LogEntryKey logEntryKey = new LogEntryKey(service, receiverId, errRecord.message());
      uniqueMessages.get().computeIfAbsent(logEntryKey, key -> {
        StringMapMessage msg = new StringMapMessage();
        msg.put(LOG_MESSAGE, VALIDATION_ERR);
        msg.put(SERVICE, key.service);
        msg.put(MESSAGE, key.message);
        msg.put(RECEIVER_ID, key.receiverId);
        msg.put(BUSINESS_CORRELATION_ID, LogEntryMapper.getCorrelationId(message));
        // locator fields
        if (locator != null) {
          if (locator.publicId() != null) msg.put("location.publicId", locator.publicId());
          if (locator.systemId() != null) msg.put("location.systemId", locator.systemId());
          msg.put("location.lineNumber", Integer.toString(locator.lineNumber()));
          msg.put("location.columnNumber", Integer.toString(locator.columnNumber()));
        }
        return msg;
      });
    });
  }

  @Override
  public void flush() {
    if (first.compareAndSet(true, false) && !services.isEmpty()) {
      log.info("Logging validation errors for services {}", services);
    }
    uniqueMessages.getAndSet(new ConcurrentHashMap<>())
      .values()
      .forEach(log::warn);
  }

  @Override
  public void register(String serviceName) {
    services.add(serviceName);
  }

  AtomicReference<ConcurrentMap<LogEntryKey, StringMapMessage>> getUniqueMessages() {
    return uniqueMessages;
  }
}
