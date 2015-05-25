package se.skltp.agp.testnonfunctional

import scala.concurrent.duration._
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef._
import se.skltp.agp.testnonfunctional.scenarios.PingForConfigurationScenario

/**
 * Ping for configuration run against remote service - returns ok.
 */
class TP01PingForConfiguration extends Simulation {

  val baseUrl = if (System.getProperty("baseUrl") != null && !System.getProperty("baseUrl").isEmpty()) { 
                  System.getProperty("baseUrl") 
                } else {
                  "http://33.33.33.33:8081/agp/getaggregatedobservations/itintegration/monitoring/PingForConfiguration/1/rivtabp21"
                }
  val httpProtocol = http.baseURL(baseUrl).disableResponseChunksDiscarding  
  
  val pingForConfiguration = scenario("ping for configuration")
                 .repeat(2) {
                    exec(PingForConfigurationScenario.request)
                  }

  setUp (pingForConfiguration.inject(atOnceUsers(1)).protocols(httpProtocol))
}
