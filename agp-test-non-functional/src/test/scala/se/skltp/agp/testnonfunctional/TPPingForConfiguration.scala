package se.skltp.agp.testnonfunctional

import scala.concurrent.duration._
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef._
import se.skltp.agp.testnonfunctional.scenarios.PingForConfigurationScenario

/**
 * Ping for configuration run against remote service - returns ok.
 */
class TPPingForConfiguration extends Simulation {

  val serviceLowercase = if (System.getProperty("serviceLowercase") != null && !System.getProperty("serviceLowercase").isEmpty()) { 
                           System.getProperty("serviceLowercase") 
                         } else {
                           ""
                         }
  
  val baseUrl = if (System.getProperty("baseUrl") != null && !System.getProperty("baseUrl").isEmpty()) { 
                  System.getProperty("baseUrl") 
                } else {
                  if (serviceLowercase != null && !serviceLowercase.isEmpty()) {
                  "http://33.33.33.33:8081/agp/" + serviceLowercase + "/itintegration/monitoring/PingForConfiguration/1/rivtabp21"
                  } else {
                    throw new IllegalArgumentException("system variable serviceLowercase is missing - should be defined as '-DserviceLowercase=getaggregatedobservations'")
                  }
                }
  val httpProtocol = http.baseURL(baseUrl).disableResponseChunksDiscarding  
  
  val pingForConfiguration = scenario("ping for configuration")
                 .repeat(2) {
                    exec(PingForConfigurationScenario.request)
                  }

  setUp (pingForConfiguration.inject(atOnceUsers(1)).protocols(httpProtocol))
}
