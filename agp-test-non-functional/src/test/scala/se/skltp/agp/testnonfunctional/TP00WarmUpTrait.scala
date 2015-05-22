package se.skltp.agp.testnonfunctional

import scala.concurrent.duration._
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef._
import se.skltp.agp.testnonfunctional.scenarios.GetAggregatedSomeScenario

/**
 * Simple requests to warm up service.
 */
trait TP00WarmUpTrait extends Simulation {

  var baseUrl:String         = "http://33.33.33.33:8081/GetAggregatedObservations/service/v1"
  var serviceName:String     = "Observations"
  var urn:String             = "urn:riv:clinicalprocess:healthcond:basic:GetObservationsResponder:1"
  var responseElement:String = "GetObservationsResponse"     
  var responseItem:String    = "observationGroup"
  
  
  val testDuration     = 1 minute
  val minWaitDuration  = 2 seconds
  val maxWaitDuration  = 5 seconds
  val times:Int        = 1 // 6

  val warmUp = scenario("warm up")
                 .repeat(times) {
                // ---
                // either run all the patients
                // feed(csv("patients.csv").queue)
                // ---
                // or just tolvan tolvansson
                   exec(session => {
                     session.set("status","200").set("patientid","121212121212").set("name","Tolvan Tolvansson").set("count","3")
                   })
                // ---
                   .exec(GetAggregatedSomeScenario.request(serviceName, urn, responseElement, responseItem))
                   .pause(1 second)
                  }

  setUp (warmUp.inject(atOnceUsers(1)).protocols(http.baseURL(baseUrl).disableResponseChunksDiscarding))
}
