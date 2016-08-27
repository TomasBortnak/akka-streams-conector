package sample.stream.lib.http

import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model.{HttpResponse, HttpRequest}
import akka.util.ByteString
import com.redis._
import com.typesafe.config.ConfigFactory

import scala.concurrent.Future
import akka.stream.scaladsl._

import org.json4s._
import org.json4s.native.JsonMethods._
import org.json4s.JsonDSL._

/**
 *
 * http://doc.akka.io/docs/akka/2.4/scala/http/client-side/connection-level.html
 *
 */
class ReactiveStream {

  implicit val formats = DefaultFormats

  val host = ConfigFactory.load().getString("redis.host")
  val port = ConfigFactory.load().getString("redis.port")

  var redis = new RedisClientPool(host, port.toInt)
  var enableRecovery = true

  var latestQueryString = ""
  var latestMessage = ""
  var json = ""

  /**
   * By using HTTP get to send the message by reactive stream
   *
   * @param queryString the query string
   * @param message the message of the HTTP request data
   * @return Future[HTTPResponse]
   */
  def get(queryString: String, message: String) = {

    latestQueryString = queryString
    latestMessage = message

    val data = ByteString(message)
    HttpRequest(GET, uri = queryString, entity = data)

  }

  /**
   * By using HTTP post to send the message by reactive stream
   *
   * @param queryString the query string
   * @param message the message of the HTTP request data
   * @return Future[HTTPResponse]
   */
  def post(queryString: String, message: String) = {

    latestQueryString = queryString
    latestMessage = message

    val data = ByteString(message)
    HttpRequest(POST, uri = queryString, entity = data)

  }

  def error {

    if (enableRecovery) {

      val m = Map("query" -> latestQueryString, "message" -> latestMessage)

      //print(compact(render(m)))

      if (json.isEmpty) {

        json = "[" + compact(render(m)) + "]"

        redis.withClient {
          client =>
            client.set("reactive_stream_recovery", json)
        }

      } else {

        redis.withClient {
          client =>
            json = client.get("reactive_stream_recovery").getOrElse("")
            json = json.replace("[", "").replace("]", "")
            json = "[" + json + "," + compact(render(m)) + "]"
            println(json)
            client.set("reactive_stream_recovery", json)
            json = "not empty"
        }

      }

    }

  }

  def retry = {

    if (enableRecovery) {

      redis.withClient {
        client => client.get("reactive_stream_recovery").getOrElse("")
      }
    } else {
      null
    }
  }
}
