package se.skltp.agp.testnonfunctional

import scala.concurrent.duration._
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef._
import se.skltp.agp.testnonfunctional.scenarios.GetAggregatedSomeScenario
import io.gatling.core.structure.PopulatedScenarioBuilder

/**
 * Robustness test aggregated service.
 * Runs against all patients (happy path, exceptions, delays)
 */
abstract class TPPatientsAbstract extends Simulation {

  val numberOfConcurrentUsers:Int = if (System.getProperty("numberOfConcurrentUsers") != null && !System.getProperty("numberOfConcurrentUsers").isEmpty()) {
                                        new Integer(System.getProperty("numberOfConcurrentUsers"))
                                    } else {
                                        5
                                    }

  val testDuration:Duration       = if (System.getProperty("testDuration") != null && !System.getProperty("testDuration").isEmpty()) {
                                       Duration((new Integer(System.getProperty("testDuration"))).longValue(), "minutes")
                                    } else {
                                       12 hours
                                    }
  
  val rampDuration:FiniteDuration = if (System.getProperty("rampDuration") != null && !System.getProperty("rampDuration").isEmpty()) {
                                        FiniteDuration((new Integer(System.getProperty("rampDuration"))).longValue(), "seconds")
                                    } else {
                                        1 minute
                                    }
  
  val minWaitDuration:Duration    = if (System.getProperty("minWaitDuration") != null && !System.getProperty("minWaitDuration").isEmpty()) {
                                        Duration((new Integer(System.getProperty("minWaitDuration"))).longValue(), "seconds")
                                    } else {
                                        2 seconds
                                    }
  
  val maxWaitDuration:Duration    = if (System.getProperty("maxWaitDuration") != null && !System.getProperty("maxWaitDuration").isEmpty()) {
                                        Duration((new Integer(System.getProperty("maxWaitDuration"))).longValue(), "seconds")
                                    } else {
                                        4 seconds
                                    }
  
  val patientsFileName:String     = if (System.getProperty("patientsFileName") != null && !System.getProperty("patientsFileName").isEmpty()) {
                                        System.getProperty("patientsFileName")
                                    } else {
                                        "patients.csv"
                                    }
  
  def patients(serviceName:String, urn:String, responseElement:String, responseItem:String, responseItemUrn:Option[String] = None) 
                 = scenario("patients")
                 .during(testDuration) {
                    feed(csv(patientsFileName).circular)
                   .exec(GetAggregatedSomeScenario.request(serviceName, urn, responseElement, responseItem, responseItemUrn))
                   .pause(minWaitDuration, maxWaitDuration)
                  }
  
   def setUpAbstract(serviceName:String, urn:String, responseElement:String, responseItem:String, baseUrl:String, responseItemUrn:Option[String] = None) : PopulatedScenarioBuilder = {
     patients(serviceName, urn, responseElement, responseItem, responseItemUrn).inject(rampUsers(numberOfConcurrentUsers) over (rampDuration)).protocols(http.baseURL(baseUrl).disableResponseChunksDiscarding) 
   }
}
