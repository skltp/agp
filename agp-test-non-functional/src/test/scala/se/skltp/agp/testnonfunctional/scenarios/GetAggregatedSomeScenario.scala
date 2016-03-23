package se.skltp.agp.testnonfunctional.scenarios

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.http.check.HttpCheck
import scala.util.Random

object GetAggregatedSomeScenario {

  def headers(soapAction:String) = Map(
    "Accept-Encoding"                        -> "gzip,deflate",
    "Content-Type"                           -> "text/xml;charset=UTF-8",
    "SOAPAction"                             ->  soapAction,
    "x-vp-sender-id"                         ->  "${senderid}",
    "x-rivta-original-serviceconsumer-hsaid" -> "NonFunctionalTest - Gatling",
    "x-skltp-correlation-id"                 -> "Correlation id - NonFunctionalTest - Gatling",
    "Keep-Alive"                             -> "115")

  def assertionUrn(urn:String, responseItemUrn:Option[String]):String = {
    responseItemUrn getOrElse urn
  }
    
  def request(serviceName:String, urn:String, responseElement:String, responseItem:String, responseItemUrn:Option[String] = None) = exec(
        http("GetAggregated" + serviceName + " ${patientid} - ${name}")
          .post("")
          .headers(headers(urn + ":Get" + serviceName))
          .body(ELFileBody("Get" + serviceName + ".xml"))
          .check(status.is(session => session("status").as[String].toInt))
          .check(xpath("soap:Envelope", List("soap" -> "http://schemas.xmlsoap.org/soap/envelope/")).exists)
          .check(substring(responseElement))
          .check(xpath("//ns2:" + responseItem, List("ns2" -> assertionUrn(urn, responseItemUrn))).count.is(session => session("count").as[String].toInt))
      )
}
