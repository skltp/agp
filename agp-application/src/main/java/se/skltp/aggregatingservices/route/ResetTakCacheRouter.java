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
import se.skltp.aggregatingservices.processors.ResetTakCacheProcessor;

@Component
public class ResetTakCacheRouter extends RouteBuilder {

  public static final String RESET_TAK_CACHE_ROUTE = "reset-tak-cache-route";
  public static final String NETTY4_HTTP_FROM_RESET_TAK_CACHE = "jetty://{{reset.cache.url}}";

  @Autowired
  ResetTakCacheProcessor resetTakCacheProcessor;

  @Override
  public void configure() throws Exception {
     from(NETTY4_HTTP_FROM_RESET_TAK_CACHE).routeId(RESET_TAK_CACHE_ROUTE)
        .process(resetTakCacheProcessor);
  }
}
