package sample.stream.example

import scala.concurrent.Future
import scala.io.StdIn
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import spray.json.DefaultJsonProtocol._

/**
 * A RESTFul server to test the library, the server will listen to "localhost:8080",
 * and it will send back JSON data as the result
 */
object RESTFulServer {


  /**
   * The case class to model the result JSON data, a simple user data
   *
   * @param name the user name
   * @param email the user email
   * @param firstname the user first name
   * @param lastname the user last name
   * @param city the city of user address
   * @param state the state of user address
   * @param country the country of user address
   */
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

  /**
   * To fetch the user data by different user id (fake)
   *
   * @param id the user id
   * @return
   */
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

  /**
   * To run the server, it had a http GET service to send back user data by user id
   */
  def run(): Unit = {

    val route =
      get {
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
