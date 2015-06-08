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
abstract class TP04RobustnessAbstract extends Simulation {

  val testDuration            =  12 hours
  val numberOfConcurrentUsers =   5
  val rampDuration            =   1 minute
  val minWaitDuration         =   2 seconds
  val maxWaitDuration         =   4 seconds
  
  def robustness(serviceName:String, urn:String, responseElement:String, responseItem:String) 
                 = scenario("robustness")
                 .during(testDuration) {
                    feed(csv("patients.csv").circular)
                   .exec(GetAggregatedSomeScenario.request(serviceName, urn, responseElement, responseItem))
                   .pause(minWaitDuration, maxWaitDuration)
                  }
  
   def setUpAbstract(serviceName:String, urn:String, responseElement:String, responseItem:String, baseUrl:String) : PopulatedScenarioBuilder = {
     robustness(serviceName, urn, responseElement, responseItem).inject(rampUsers(numberOfConcurrentUsers) over (rampDuration)).protocols(http.baseURL(baseUrl).disableResponseChunksDiscarding) 
   }
}
