package se.skltp.agp.testnonfunctional.scenarios

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.http.check.HttpCheck
import scala.util.Random

object GetAggregatedSomeScenario {

  def headers(urn:String) = Map(
    "Accept-Encoding"                        -> "gzip,deflate",
    "Content-Type"                           -> "text/xml;charset=UTF-8",
    "SOAPAction"                             ->  urn,
    "x-vp-sender-id"                         -> "SE5565594230-B9P",
    "x-rivta-original-serviceconsumer-hsaid" -> "NonFunctionalTest - Gatling",
    "Keep-Alive"                             -> "115")

  def request(serviceName:String, urn:String, responseElement:String, responseItem:String) = exec(
        http("GetAggregated" + serviceName + " ${patientid} - ${name}")
          .post("")
          .headers(headers(urn))
          .body(ELFileBody("Get" + serviceName + ".xml"))
          .check(status.is(session => session("status").as[String].toInt))
          .check(xpath("soap:Envelope", List("soap" -> "http://schemas.xmlsoap.org/soap/envelope/")).exists)
          .check(substring(responseElement))
          .check(xpath("//ns2:" + responseItem, List("ns2" -> urn)).count.is(session => session("count").as[String].toInt))
      )
}
