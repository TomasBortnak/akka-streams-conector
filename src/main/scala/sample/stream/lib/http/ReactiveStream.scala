package sample.stream.lib.http

import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model.{HttpResponse, HttpRequest}
import akka.util.ByteString
import com.redis._
import com.typesafe.config.ConfigFactory

import scala.concurrent.Future
import akka.stream.scaladsl._

/**
 * - should have some custom exception class
 *
 * http://doc.akka.io/docs/akka/2.4/scala/http/client-side/connection-level.html
 *
 */
class ReactiveStream {

  val host = ConfigFactory.load().getString("redis.host")
  val port = ConfigFactory.load().getString("redis.port")

  var redis = new RedisClientPool(host, port.toInt)
  var enableRecovery = true


  /**
   * By using HTTP get to send the message by reactive stream
   *
   * @param queryString the query string
   * @param message the message of the HTTP request data
   * @return Future[HTTPResponse]
   */
  def get(queryString: String, message: String) = {

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

    val data = ByteString(message)
    HttpRequest(POST, uri = queryString, entity = data)

  }

  def retry(method: String, endpoint: String, port: Int, queryString: String, identity: String) = {

    if (enableRecovery) {

      redis.withClient {
        client => {
          val content = client.get(identity).getOrElse(null)
          if (content != null) {

          } else
            null
        }
      }
    } else
      null

  }

  def save(identity: String, content: String) = {

    // println("Redis saving, '" + identity + "' :: " + content)
    redis.withClient {
      client => {
        client.set(identity, content)
      }
    }

  }

}
