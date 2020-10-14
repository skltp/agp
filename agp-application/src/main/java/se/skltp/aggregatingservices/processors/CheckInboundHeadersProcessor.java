package se.skltp.aggregatingservices.processors;

import lombok.extern.log4j.Log4j2;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import se.skltp.aggregatingservices.constants.AgpHeaders;

@Service
@Log4j2
public class CheckInboundHeadersProcessor implements Processor {

  private static final String HEADER_MISSING_MSG = "Mandatory HTTP header %s is missing\n";

  @Override
  public void process(Exchange exchange) {

    String senderId         = exchange.getIn().getHeader(AgpHeaders.X_VP_SENDER_ID, String.class);
    String originalConsumer = exchange.getIn().getHeader(AgpHeaders.X_RIVTA_ORIGINAL_SERVICE_CONSUMER_HSA_ID, String.class);
    String correlationId    = exchange.getIn().getHeader(AgpHeaders.X_SKLTP_CORRELATION_ID, String.class);

    if (log.isDebugEnabled()) {
      log.debug(AgpHeaders.X_VP_SENDER_ID + " = " + senderId + ", " +
          AgpHeaders.X_RIVTA_ORIGINAL_SERVICE_CONSUMER_HSA_ID + " = " + originalConsumer + ", " +
          AgpHeaders.X_SKLTP_CORRELATION_ID + " = " + correlationId);
    }

    String errMsg = "";
    if (senderId == null) {
      errMsg = String.format(HEADER_MISSING_MSG, AgpHeaders.X_VP_SENDER_ID);
    }
    if (originalConsumer == null) {
      errMsg = errMsg + String.format(HEADER_MISSING_MSG, AgpHeaders.X_RIVTA_ORIGINAL_SERVICE_CONSUMER_HSA_ID);
    }
    if (correlationId == null) {
      errMsg = errMsg + String.format(HEADER_MISSING_MSG, AgpHeaders.X_SKLTP_CORRELATION_ID);
    }
    if (StringUtils.isNotEmpty(errMsg)) {
      throw new IllegalArgumentException(errMsg);
    }

  }
}
