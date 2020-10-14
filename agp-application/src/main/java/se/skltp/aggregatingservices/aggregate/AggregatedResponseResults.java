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
