package se.skltp.aggregatingservices.aggregate;

import static se.skltp.aggregatingservices.constants.AgpProperties.LOGICAL_ADDRESS;
import static se.skltp.agp.riv.interoperability.headers.v1.StatusCodeEnum.DATA_FROM_SOURCE;
import static se.skltp.agp.riv.interoperability.headers.v1.StatusCodeEnum.NO_DATA_SYNCH_FAILED;

import lombok.extern.log4j.Log4j2;
import org.apache.camel.AggregationStrategy;
import org.apache.camel.Exchange;
import org.springframework.stereotype.Service;
import se.skltp.aggregatingservices.utils.EngagementProcessingStatusUtil;
import se.skltp.aggregatingservices.utils.ProcessingStatusUtil;
import se.skltp.agp.riv.interoperability.headers.v1.ProcessingStatusRecordType;

@Service
@Log4j2
public class AgpAggregationStrategy implements AggregationStrategy {


  @Override
  public Exchange aggregate(Exchange oldExchange, Exchange newExchange) {

    if(oldExchange==null){
      AggregatedResponseResults aggregatedResponseResults = new AggregatedResponseResults();
      updateAggregatedResponse(newExchange, aggregatedResponseResults);
      newExchange.getIn().setBody(aggregatedResponseResults);
      return newExchange;
    } else {
      AggregatedResponseResults aggregatedResponseResults = oldExchange.getIn().getBody(AggregatedResponseResults.class);
      updateAggregatedResponse(newExchange, aggregatedResponseResults);
      return oldExchange;
    }
  }

  private void updateAggregatedResponse(Exchange newExchange, AggregatedResponseResults aggregatedResponseResults) {
    final ProcessingStatusRecordType statusRecord;
    final String logicalAddress = newExchange.getProperty(LOGICAL_ADDRESS, String.class);
    if(newExchange.isFailed() || newExchange.getException() != null){
      final Exception exception = newExchange.getException();
      log.info("Failed get result from {} with exception:", logicalAddress, exception);
      statusRecord = ProcessingStatusUtil.createStatusRecord(logicalAddress, NO_DATA_SYNCH_FAILED, exception);
      aggregatedResponseResults.getProcessingStatus().getProcessingStatusList().add(statusRecord);

      // reset the exception to prevent in to be thrown after split
      newExchange.setException(null);

    } else {
      EngagementProcessingStatusUtil.updateOK(logicalAddress, newExchange);
      statusRecord = ProcessingStatusUtil.createStatusRecord(logicalAddress, DATA_FROM_SOURCE);
      aggregatedResponseResults.getProcessingStatus().getProcessingStatusList().add(statusRecord);
      final Object inBody = newExchange.getIn().getBody();
      if(inBody!=null) {
        aggregatedResponseResults.getResponseObjects().add(inBody);
      }
    }
  }

  @Override
  public void timeout(Exchange oldExchange, int index, int total, long timeout) {
    log.warn("Aggregation timeout occured for index {}. Timeout value: {}", index, timeout);
  }

}
