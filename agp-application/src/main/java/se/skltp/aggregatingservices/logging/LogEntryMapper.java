package se.skltp.aggregatingservices.logging;

import static se.skltp.aggregatingservices.constants.AgpProperties.CORRELATION_ID;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.cxf.ext.logging.event.DefaultLogEventMapper;
import org.apache.cxf.message.Message;
import org.apache.logging.log4j.ThreadContext;
import se.skltp.aggregatingservices.constants.AgpHeaders;
import se.skltp.aggregatingservices.constants.AgpProperties;

public class LogEntryMapper {

  public static final String MSG_TYPE_LOG_REQ_IN = "req-in";
  public static final String MSG_TYPE_LOG_REQ_OUT = "req-out";
  public static final String MSG_TYPE_LOG_RESP_IN = "resp-in";
  public static final String MSG_TYPE_LOG_RESP_OUT = "resp-out";
  public static final String MSG_TYPE_LOG_RESP_OUT_ERROR = "error-out";
  public static final String MSG_TYPE_LOG_RESP_IN_ERROR = "error-in";

  protected static final DefaultLogEventMapper eventMapper = new DefaultLogEventMapper();

  protected static Set<String> sensitiveProtocolHeaderNames = new HashSet<>();

  // Static utility class
  private LogEntryMapper() {
  }

  public static LogEntry map(Message message) {
    LogEntry logEntry = new LogEntry();
    logEntry.setLogEvent(eventMapper.map(message, sensitiveProtocolHeaderNames));
    logEntry.setComponentId(getComponentId(message));
    logEntry.setSenderId(getHeader(AgpHeaders.X_VP_SENDER_ID, message));
    logEntry.setOriginalSenderId(getHeader(AgpHeaders.X_RIVTA_ORIGINAL_SERVICE_CONSUMER_HSA_ID, message));
    logEntry.setEngagementProcessingResult(getProperty(AgpProperties.LOG_ENGAGEMENT_PROCESSING_RESULT, message));
    logEntry.setProcessingStatus(getProperty(AgpProperties.LOG_PROCESSING_STATUS, message));
    logEntry.setProcessingStatusCountFail(getProperty(AgpProperties.LOG_PROCESSING_COUNT_FAIL, message));
    logEntry.setProcessingStatusCountTot(getProperty(AgpProperties.LOG_PROCESSING_COUNT_TOT, message));
    logEntry.setReceiverId(getLogicalAddress(message));

    String correlationId = getCorrelationId(message);
    if (correlationId != null) {
      message.getExchange().put(CORRELATION_ID, correlationId);
      ThreadContext.put("corr.id", String.format("[%s]", correlationId));
      logEntry.setCorrelationId(correlationId);
    }

    return logEntry;
  }

  public static String getCorrelationId(Message message) {
    String corrId = (String) message.getExchange().get(CORRELATION_ID);
    if (corrId != null) {
      return corrId;
    }
    return getHeader(AgpHeaders.X_SKLTP_CORRELATION_ID, message);
  }

  public static String getLogicalAddress(Message message) {
    final String logicalAddress = getProperty(AgpProperties.LOGICAL_ADDRESS, message);
    if (logicalAddress == null) {
      return (String) message.getExchange().get(AgpProperties.LOGICAL_ADDRESS);
    }
    message.getExchange().put(AgpProperties.LOGICAL_ADDRESS, logicalAddress);
    return logicalAddress;
  }

  public static String getComponentId(Message message) {
    String componentId = (String) message.getExchange().get(AgpProperties.AGP_SERVICE_COMPONENT_ID);
    if (componentId != null) {
      return componentId;
    }

    return (String) message.getExchange().getEndpoint().get("ComponentId");
  }

  @SuppressWarnings("unchecked")
  public static String getHeader(String headerName, Message message) {
    final Map<String, List<String>> headers = (Map<String, List<String>>) message.get(Message.PROTOCOL_HEADERS);
    if (headers != null) {
      var headerList = headers.get(headerName);
      if (headerList != null && !headerList.isEmpty()) {
        return headerList.get(0);
      }
    }
    return null;
  }

  public static String getProperty(String propertyName, Message message) {
    Object prop = message.get(propertyName);
    if (prop != null) {
      return message.get(propertyName).toString();
    }
    return null;
  }
}
