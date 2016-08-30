package sample.stream.lib

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer

/**
 * A simple http request library with reacitve stream from akka 2.x
 *
 * How it works?
 *
 * - The library provide too http request method GET and POST
 *
 * - For each method, it provide a reactive stream to send a series of http request
 *
 * - For each request, it will return response to success or error callback
 *
 * - It had an option to save the query info to redis if it's not success
 *
 * - It had an option to get all query info from redis, user can send it later for recovery
 */
package object http {

  implicit val system = ActorSystem("Sys")
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher
}
