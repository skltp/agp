package se.skltp.agp.testnonfunctional.scenarios

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.http.check.HttpCheck

object PingForConfigurationScenario {
  
  val testtitle = if (System.getProperty("testtitle") != null && !System.getProperty("testtitle").isEmpty()) { 
                    System.getProperty("testtitle") 
                  } else {
                    "PingForConfiguration"
                  }

  val aggregatedService = if (System.getProperty("aggregatedService") != null && !System.getProperty("aggregatedService").isEmpty()) { 
                            System.getProperty("aggregatedService") 
                          } else {
                            throw new IllegalArgumentException("missing system property aggregatedService (for example, -DaggregatedService=GetAggregatedFunctionalStatus)")
                          }
  
  val headers = Map(
    "Accept-Encoding" -> "gzip,deflate",
    "Content-Type"    -> "text/xml;charset=UTF-8",
    "SOAPAction"      -> "urn:riv:itintegration:monitoring:PingForConfigurationResponder:1:PingForConfiguration",
    "Keep-Alive"      -> "115")

  val request = exec(
        http(testtitle)
          .post("")
          .headers(headers)
          .body(RawFileBody("PingForConfiguration.xml"))
          .check(status.is(200))
          .check(substring("Applikation"))
          .check(substring(aggregatedService))
          .check(xpath("soap:Envelope", List("soap" -> "http://schemas.xmlsoap.org/soap/envelope/")).exists)
          .check(regex(aggregatedService).exists)
      )
}
