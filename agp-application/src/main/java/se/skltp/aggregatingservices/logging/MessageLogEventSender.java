package se.skltp.aggregatingservices.logging;

import static se.skltp.aggregatingservices.logging.LogEntryMapper.MSG_TYPE_LOG_REQ_IN;
import static se.skltp.aggregatingservices.logging.LogEntryMapper.MSG_TYPE_LOG_REQ_OUT;
import static se.skltp.aggregatingservices.logging.LogEntryMapper.MSG_TYPE_LOG_RESP_IN;
import static se.skltp.aggregatingservices.logging.LogEntryMapper.MSG_TYPE_LOG_RESP_IN_ERROR;
import static se.skltp.aggregatingservices.logging.LogEntryMapper.MSG_TYPE_LOG_RESP_OUT;
import static se.skltp.aggregatingservices.logging.LogEntryMapper.MSG_TYPE_LOG_RESP_OUT_ERROR;

import java.net.InetAddress;
import lombok.extern.log4j.Log4j2;
import org.apache.cxf.common.util.StringUtils;
import org.apache.cxf.ext.logging.event.EventType;
import org.apache.cxf.ext.logging.event.LogEvent;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

@Log4j2
public class MessageLogEventSender {

  private static final Marker MESSAGE_LOG_MARKER = MarkerManager.getMarker("AGP_MESSAGE_LOG");

  protected static String hostName = "UNKNOWN (UNKNOWN)";

  static {
    try {
      // Let's give it a try, fail silently...
      InetAddress host = InetAddress.getLocalHost();
      hostName = String.format("%s (%s)",
          host.getCanonicalHostName(),
          host.getHostAddress());
    } catch (Exception ex) {
      log.warn("Failed get runtime values for logging", ex);
    }
  }

  public void send(LogEntry event, Logger logger) {
    logger.info(MESSAGE_LOG_MARKER, getLogMessage(event));
  }


  public static String getLogMessage(LogEntry msgEvent) {
    LogEvent logEvent = msgEvent.getLogEvent();
    StringBuilder b = new StringBuilder();
    b.append("skltp-messages\n");
    b.append("** logEvent-info.start ***********************************************************\n");
    write(b, "LogMessage", type2LogMessage(logEvent.getType()));
    write(b, "ComponentId", "aggregating-services");
    write(b, "ServiceImpl", msgEvent.getComponentId());
    write(b, "Host", hostName);
    write(b, "Endpoint", logEvent.getAddress());
    write(b, "MessageId", logEvent.getExchangeId());
    write(b, "BusinessCorrelationId", msgEvent.getCorrelationId());

    b.append("ExtraInfo=\n");
    write(b,"-responseCode", logEvent.getResponseCode());
    if (logEvent.getServiceName() != null) {
      write(b,"-wsdl_namespace", logEvent.getServiceName().getNamespaceURI());
    }
    write(b,"-senderid", msgEvent.getSenderId());
    write(b,"-originalServiceconsumerHsaid", msgEvent.getOriginalSenderId());
    write(b,"-receiverid", msgEvent.getReceiverId());
    write(b,"-engagementProcessingStatus", msgEvent.getEngagementProcessingResult());
    write(b,"-processingStatus", msgEvent.getProcessingStatus());
    write(b,"-processingStatusCountFail", msgEvent.getProcessingStatusCountFail());
    write(b,"-processingStatusCountTot", msgEvent.getProcessingStatusCountTot());
    write(b,"-headers", logEvent.getHeaders().toString());
    write(b,"-encoding", logEvent.getEncoding());

    if (!StringUtils.isEmpty(logEvent.getPayload())) {
      write(b, "Payload", logEvent.getPayload());
    }
    b.append("** logEvent-info.end *************************************************************\n");

    return b.toString();
  }

  private static String type2LogMessage(EventType type) {
    switch(type){
      case REQ_IN:
        return MSG_TYPE_LOG_REQ_IN;

      case REQ_OUT:
        return MSG_TYPE_LOG_REQ_OUT;

      case RESP_IN:
        return MSG_TYPE_LOG_RESP_IN;

      case RESP_OUT:
        return MSG_TYPE_LOG_RESP_OUT;

      case FAULT_IN:
        return MSG_TYPE_LOG_RESP_IN_ERROR;

      case FAULT_OUT:
        return MSG_TYPE_LOG_RESP_OUT_ERROR;

      default:
        return type.name();
    }
  }


  protected static void write(StringBuilder b, String key, String value) {
    if (value != null) {
      b.append(key).append("=").append(value).append("\n");
    }
  }

}
