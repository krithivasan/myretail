package loadtest.loadtest// 1
import io.gatling.core.Predef._
import io.gatling.http.Predef._

import scala.concurrent.duration._
class LoadTest extends Simulation { // 3
  val id: String = "13860428"
  val httpConf = http // 4
    .baseURL("http://localhost:8080") // 5
    .acceptHeader("text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8") // 6
    .doNotTrackHeader("1")
    .acceptLanguageHeader("en-US,en;q=0.5")
    .acceptEncodingHeader("gzip, deflate")
    .userAgentHeader("Mozilla/5.0 (Windows NT 5.1; rv:31.0) Gecko/20100101 Firefox/31.0")
  val scn = scenario("BasicSimulation").asLongAs(true) {  // 7
    exec(http("Get Product Info")  // 8
      //.get(s"/products/${id}")
        .post("/products")
        .body(RawFileBody("input.json")).asJSON
      .check(status.is(201))) // 9
  } //.pause(5) } // 10
  setUp( // 11
    scn.inject(atOnceUsers(1), rampUsers(60) over (5 seconds)) // 12
  ).protocols(httpConf).maxDuration(1 minutes) // 13
}