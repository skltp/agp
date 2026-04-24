/*
 * Copyright © 2014-2026 Inera.
 * Copyright owner URL: https://www.inera.se/
 * SKLTP overview page: https://inera.atlassian.net/wiki/spaces/SKLTP/overview
 * This library is free software under the GNU Lesser General Public License v3.0.
 * Please refer to the full license files at the project root.
 */
package se.skltp.aggregatingservices.consumer;

import java.util.Map;
import se.skltp.aggregatingservices.utils.ServiceResponse;

public interface ConsumerService {


  public ServiceResponse callService(String logicalAddress, String patientId, Map<String, Object> additionalHeaders);

  public ServiceResponse callService(String patientId);

  public ServiceResponse callService(String logicalAddress, String patientId);

  public ServiceResponse callService(String senderId, String originalId,  String logicalAddress, String patientId  );

  public ServiceResponse callServiceWithWrongContract();


}
