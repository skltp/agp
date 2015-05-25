package se.skltp.agp.testnonfunctional

import scala.concurrent.duration._
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef._
import se.skltp.agp.testnonfunctional.scenarios.GetAggregatedSomeScenario
import io.gatling.core.structure.PopulatedScenarioBuilder

/**
 * Load test aggregated service.
 */
abstract class TP03LoadAbstract extends Simulation {

  val testDuration            = 140 seconds
  val numberOfConcurrentUsers =  45
  val rampDuration            =  10 seconds
  val minWaitDuration         =   2 seconds
  val maxWaitDuration         =   5 seconds
  
  def load(serviceName:String, urn:String, responseElement:String, responseItem:String) = scenario("load")
                 .during(testDuration) {
                   exec(session => {
                     session.set("status","200").set("patientid","121212121212").set("name","Tolvan Tolvansson").set("count","3")
                   })
                   .exec(GetAggregatedSomeScenario.request(serviceName, urn, responseElement, responseItem))
                   .pause(minWaitDuration, maxWaitDuration)
                  }
  
   def setUpAbstract(serviceName:String, urn:String, responseElement:String, responseItem:String, baseUrl:String) : PopulatedScenarioBuilder = {
     load(serviceName, urn, responseElement, responseItem).inject(rampUsers(numberOfConcurrentUsers) over (rampDuration)).protocols(http.baseURL(baseUrl).disableResponseChunksDiscarding) 
   }
}
