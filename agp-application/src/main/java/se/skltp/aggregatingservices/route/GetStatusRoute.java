package se.skltp.aggregatingservices.route;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import se.skltp.aggregatingservices.processors.GetStatusProcessor;

@Component
public class GetStatusRoute extends RouteBuilder {

  public static final String JETTY_HTTP_FROM_GET_STATUS = "jetty://{{agp.status.url}}";
  public static final String GET_STATUS_ROUTE = "get-status";

  @Autowired
  GetStatusProcessor getStatusProcessor;

  @Override
  public void configure() {
    from(JETTY_HTTP_FROM_GET_STATUS).routeId(GET_STATUS_ROUTE)
        .process(getStatusProcessor);
  }
}