package sample.stream

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer

package object demo {

  implicit val system = ActorSystem("Sys")
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher
}
