/*
 * Copyright © 2014-2026 Inera.
 * Copyright owner URL: https://www.inera.se/
 * SKLTP overview page: https://inera.atlassian.net/wiki/spaces/SKLTP/overview
 * This library is free software under the GNU Lesser General Public License v3.0.
 * Please refer to the full license files at the project root.
 */
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
