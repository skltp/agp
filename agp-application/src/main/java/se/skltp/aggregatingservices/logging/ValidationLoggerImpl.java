package se.skltp.aggregatingservices.logging;

import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.message.StringMapMessage;
import org.springframework.stereotype.Service;
import org.apache.cxf.message.Message;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
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

  private final List<String> services = new ArrayList<>();
  private boolean first = true;

  record LogEntryKey(String service, String receiverId, String message) {
  }

  private final AtomicReference<ConcurrentMap<LogEntryKey, StringMapMessage>> uniqueMessages = new AtomicReference<>(new ConcurrentHashMap<>());

  @Override
  public void addErrors(List<String> errors, String service, Message message) {
    errors.forEach(err -> {
      LogEntryKey logEntryKey = new LogEntryKey(service, LogEntryMapper.getLogicalAddress(message), err);
      uniqueMessages.get().computeIfAbsent(logEntryKey, key -> {
        StringMapMessage msg = new StringMapMessage();
        msg.put(LOG_MESSAGE, VALIDATION_ERR);
        msg.put(SERVICE, key.service);
        msg.put(MESSAGE, key.message);
        msg.put(RECEIVER_ID, key.receiverId);
        msg.put(BUSINESS_CORRELATION_ID, LogEntryMapper.getCorrelationId(message));
        return msg;
      });
    });
  }

  @Override
  public void flush() {
    if (first) {
      if (!services.isEmpty()) {
        log.info("Logging validation errors for services {}", services);
      }
      first = false;
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
