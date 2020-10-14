package se.skltp.aggregatingservices.logging;

import lombok.Data;
import org.apache.cxf.ext.logging.event.LogEvent;

@Data
public class LogEntry {
  LogEvent logEvent;
  String correlationId;
  String componentId;
  String receiverId;
  String senderId;
  String originalSenderId;
  String engagementProcessingResult;
  String processingStatus;
  String processingStatusCountTot;
  String processingStatusCountFail;

}
