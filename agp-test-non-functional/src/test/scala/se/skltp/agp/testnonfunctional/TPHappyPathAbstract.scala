package se.skltp.agp.testnonfunctional

import scala.concurrent.duration._
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef._
import se.skltp.agp.testnonfunctional.scenarios.GetAggregatedSomeScenario

/**
 * Simple requests to warm up service.
 */
abstract class TPHappyPathAbstract extends Simulation {

  val numberOfConcurrentUsers:Int  = if (System.getProperty("numberOfConcurrentUsers") != null && !System.getProperty("numberOfConcurrentUsers").isEmpty()) {
                                         new Integer(System.getProperty("numberOfConcurrentUsers"))
                                     } else {
                                         1
                                     }

  val testDuration:Duration        = if (System.getProperty("testDuration") != null && !System.getProperty("testDuration").isEmpty()) {
                                         Duration((new Integer(System.getProperty("testDuration"))).longValue(), "seconds")
                                     } else {
                                         60 seconds
                                     }
  
  val rampDuration:FiniteDuration  = if (System.getProperty("rampDuration") != null && !System.getProperty("rampDuration").isEmpty()) {
                                         FiniteDuration((new Integer(System.getProperty("rampDuration"))).longValue(), "seconds")
                                     } else {
                                         10 seconds
                                     }
  
  val minWaitDuration:Duration     = if (System.getProperty("minWaitDuration") != null && !System.getProperty("minWaitDuration").isEmpty()) {
                                         Duration((new Integer(System.getProperty("minWaitDuration"))).longValue(), "seconds")
                                     } else {
                                         1 seconds
                                     }
  
  val maxWaitDuration:Duration     = if (System.getProperty("maxWaitDuration") != null && !System.getProperty("maxWaitDuration").isEmpty()) {
                                         Duration((new Integer(System.getProperty("maxWaitDuration"))).longValue(), "seconds")
                                     } else {
                                         1 seconds
                                     }
  
  val senderId:String              = if (System.getProperty("senderid") != null && !System.getProperty("senderid").isEmpty()) {
										new String(System.getProperty("senderid"))										
									 } else {
										throw new IllegalArgumentException("Note: This tests requires senderid. Usage -Dsenderid=xxx")
									 }
  def happyPath(serviceName:String, urn:String, responseElement:String, responseItem:String, responseItemUrn:Option[String] = None) 
                = scenario("happy path")
                 .during(testDuration) {
                   exec(session => {
                     session.set("status","200").set("patientid","121212121212").set("name","Tolvan Tolvansson").set("count","3").set("senderid", senderId)
                   })
                   .exec(GetAggregatedSomeScenario.request(serviceName, urn, responseElement, responseItem, responseItemUrn))
                   .pause(minWaitDuration, maxWaitDuration)
                  }
  
   def setUpAbstract(serviceName:String, urn:String, responseElement:String, responseItem:String, baseUrl:String, responseItemUrn:Option[String] = None) : io.gatling.core.structure.PopulatedScenarioBuilder = {
     happyPath(serviceName, urn, responseElement, responseItem, responseItemUrn).inject(rampUsers(numberOfConcurrentUsers) over (rampDuration)).protocols(http.baseURL(baseUrl).disableResponseChunksDiscarding) 
   }
}
