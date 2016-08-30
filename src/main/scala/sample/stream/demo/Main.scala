package sample.stream.demo

import scala.concurrent.Future

import org.json4s._
import org.json4s.native.JsonMethods._
import org.json4s.JsonDSL._


object Main {

  implicit val formats = DefaultFormats


  def main(args: Array[String]) {

    Future {
      RESTFulServer.run()
    }

    // Comparing the HTTP reactive stream and the HTTP sync request
    // For each HTTP request, it will wait for 2s by Thread.sleep for next

    // Waiting for the server running

    Thread.sleep(2000)

    println("Trying to see how the client work...")

    println("== The HTTP sync request test == ")

    for (a <- 1 to 5) {
      //println("The number " + a + " request now")
      //RESTFulClient.getByHTTP
      //Thread.sleep(2000)
    }

    println("== The reactive stream HTTP request test ==")

    RESTFulClient.getByReactiveStream("localhost", 8080, "")

  }
}
