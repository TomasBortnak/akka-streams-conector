package sample.stream.demo

import scala.concurrent.Future
import scala.io.StdIn
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import spray.json.DefaultJsonProtocol._

/**
 * A RESTFul server
 */
object RESTFulServer {


  final case class User(
                         name: String,
                         email: String,
                         firstname: String,
                         lastname: String,
                         city: String,
                         state: String,
                         country: String)


  // formats for unmarshalling and marshalling
  implicit val itemFormat = jsonFormat7(User)

  // (fake) async database query api
  def fetchUser(id: Long): Future[Option[User]] = {

    Future {
      id match {
        case 0 => None
        case 1 =>
          Some(new User("normal speed", "test@test.com", "test", "test", "test", "test", "test"))
        case 2 =>
          Some(new User("slow speed", "test@test.com", "test", "test", "test", "test", "test"))
        case _ =>
          Some(new User("unknow", "test@test.com", "test", "test", "test", "test", "test"))
      }
    }
  }

  def run(): Unit = {

    val route =
      post {
        pathPrefix("user" / LongNumber) { id =>

          val maybeItem: Future[Option[User]] = fetchUser(id)
          onSuccess(maybeItem) {
            case Some(item) => complete(item)
            case None => complete(StatusCodes.NotFound)
          }
        }
      }

    val bindingFuture = Http().bindAndHandle(route, "localhost", 8080)

    println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
    StdIn.readLine() // let it run until user presses return
    bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port .onComplete(_ ⇒ system.terminate()) // and shutdown when done

  }
}
