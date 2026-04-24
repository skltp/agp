/*
 * Copyright © 2014-2026 Inera.
 * Copyright owner URL: https://www.inera.se/
 * SKLTP overview page: https://inera.atlassian.net/wiki/spaces/SKLTP/overview
 * This library is free software under the GNU Lesser General Public License v3.0.
 * Please refer to the full license files at the project root.
 */
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