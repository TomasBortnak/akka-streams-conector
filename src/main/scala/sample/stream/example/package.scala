package sample.stream

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer

package object example {

  implicit val system = ActorSystem("Sys")
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher
}
