/*
 * Copyright © 2014-2026 Inera.
 * Copyright owner URL: https://www.inera.se/
 * SKLTP overview page: https://inera.atlassian.net/wiki/spaces/SKLTP/overview
 * This library is free software under the GNU Lesser General Public License v3.0.
 * Please refer to the full license files at the project root.
 */
package se.skltp.aggregatingservices.logging;

import org.apache.cxf.message.Message;

import java.util.List;

public interface ValidationLogger {
  void addErrors(List<CollectingErrorHandler.ErrorRecord> errors, String service, Message message);

  void flush();

  void register(String serviceName);
}
