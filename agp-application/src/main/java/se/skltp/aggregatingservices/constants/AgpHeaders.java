/*
 * Copyright © 2014-2026 Inera.
 * Copyright owner URL: https://www.inera.se/
 * SKLTP overview page: https://inera.atlassian.net/wiki/spaces/SKLTP/overview
 * This library is free software under the GNU Lesser General Public License v3.0.
 * Please refer to the full license files at the project root.
 */
package se.skltp.aggregatingservices.constants;

public class AgpHeaders {

  // Header defines
  private AgpHeaders() {
  }

  public static final String X_VP_SENDER_ID = "x-vp-sender-id";
  public static final String X_VP_INSTANCE_ID = "x-vp-instance-id";
  public static final String X_RIVTA_ORIGINAL_SERVICE_CONSUMER_HSA_ID = "x-rivta-original-serviceconsumer-hsaid";
  public static final String X_SKLTP_CORRELATION_ID = "x-skltp-correlation-id";
  public static final String HEADER_CONTENT_TYPE = "Content-type";
}
