/*
 * Copyright © 2014-2026 Inera.
 * Copyright owner URL: https://www.inera.se/
 * SKLTP overview page: https://inera.atlassian.net/wiki/spaces/SKLTP/overview
 * This library is free software under the GNU Lesser General Public License v3.0.
 * Please refer to the full license files at the project root.
 */
package se.skltp.aggregatingservices.aggregate;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import se.skltp.agp.riv.interoperability.headers.v1.ProcessingStatusType;

@Data
public class AggregatedResponseResults {
  private List<Object> responseObjects = new ArrayList<>();
  private ProcessingStatusType processingStatus = new ProcessingStatusType();
}
