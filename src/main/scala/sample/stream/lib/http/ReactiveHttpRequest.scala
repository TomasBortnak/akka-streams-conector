package sample.stream.lib.http


import org.json4s._
import org.json4s.native.JsonMethods._
import org.json4s.JsonDSL._

import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model.{HttpResponse, HttpRequest}
import akka.util.ByteString
import com.redis._
import com.typesafe.config.ConfigFactory

import scala.concurrent.Future
import akka.stream.scaladsl._

/**
 * The case class to include the data model of each request
 *
 * @param query the path and query string of each request url
 * @param data the data to send 
 */
case class QueryInfomation(query: String, data: String)

/**
 * The class to provide basic http request based on akka HTTP & Reactive Stream
 */
class ReactiveHttpRequest {

  implicit val formats = DefaultFormats


  /**
   * The redis client handler to connect to the redis service
   */
  val redis = new RedisClientPool(ConfigFactory.load().getString("sample.stream.lib.http.redis.host"), ConfigFactory.load().getString("sample.stream.lib.http.redis.port").toInt)

  /**
   * The configurable option to enable recovery or not
   */
  val enableRecovery = ConfigFactory.load().getString("sample.stream.lib.http.recovery.enabled").toBoolean

  /**
   * The latest path and query string of the request, to be saved for recovery 
   */
  var latestQueryString = ""

  /**
   * the latest data of the request, to be saved for recovery
   */
  var latestMessage = ""

  /**
   * The recovery data to save all information for recovery, basically a json format
   */
  var recoveryData = ""

  /**
   * To send a stream of http request by HTTP GET with akka stream & http
   *
   * @param queryInfo the query information including query string and the data to send
   * @param success the callback to deal with success response
   * @param error the callback to deal with error response
   * @return
   */

  def get(queryInfo: Iterator[List[QueryInfomation]], success: HttpResponse => Unit, error: HttpResponse => Unit) = {

    val endpoint = ConfigFactory.load().getString("sample.stream.lib.http.api.endpoint")
    val port = ConfigFactory.load().getString("sample.stream.lib.http.api.port")

    val connectionFlow: Flow[HttpRequest, HttpResponse, Future[Http.OutgoingConnection]] =
      Http().outgoingConnection(endpoint, port = port.toInt)

    val source = Source.fromIterator(() => queryInfo).mapConcat(identity)

    // println(enableRecovery)

    source
      .map {
        info =>

          latestQueryString = info.query
          latestMessage = info.data

          val data = ByteString(info.data)
          HttpRequest(GET, uri = info.query, entity = data)
      }
      .via(connectionFlow)
      .runWith(Sink.foreach {
        res => res.status.isSuccess() match {
          case true =>
            success(res)
          case _ =>
            error(res)
        }
      })
  }

  /**
   * To send a stream of http request by HTTP GET with akka stream & http
   *
   * @param queryInfo the query information including query string and the data to send
   * @param success the callback to deal with success response
   * @param error the callback to deal with error response
   * @return
   */

  def post(queryInfo: Iterator[List[QueryInfomation]], success: HttpResponse => Unit, error: HttpResponse => Unit) = {

    val endpoint = ConfigFactory.load().getString("sample.stream.lib.http.api.endpoint")
    val port = ConfigFactory.load().getString("sample.stream.lib.http.api.port")

    val connectionFlow: Flow[HttpRequest, HttpResponse, Future[Http.OutgoingConnection]] =
      Http().outgoingConnection(endpoint, port = port.toInt)

    val source = Source.fromIterator(() => queryInfo).mapConcat(identity)

    source
      .map {
        info =>

          latestQueryString = info.query
          latestMessage = info.data

          val data = ByteString(info.data)
          HttpRequest(POST, uri = info.query, entity = data)
      }
      .via(connectionFlow)
      .runWith(Sink.foreach {
        res => res.status.isSuccess() match {
          case true =>
            success(res)
          case _ =>
            error(res)
        }
      })

  }

  /**
   * To save the latest request info to redis when it's not success, this is for recovery option.
   */
  def error = {

    if (enableRecovery) {

      val m = Map("query" -> latestQueryString, "data" -> latestMessage)

      //print(compact(render(m)))

      if (recoveryData.isEmpty) {

        recoveryData = "[" + compact(render(m)) + "]"

        redis.withClient {
          client =>
            client.set("sample.stream.lib.http.recovery", recoveryData)
        }

      } else {

        redis.withClient {
          client =>
            recoveryData = client.get("sample.stream.lib.http.recovery").getOrElse("")
            recoveryData = recoveryData.replace("[", "").replace("]", "")
            recoveryData = "[" + recoveryData + "," + compact(render(m)) + "]"
            println(recoveryData)
            client.set("sample.stream.lib.http.recovery", recoveryData)
            recoveryData = "not empty"
        }
      }
    }
  }

  /**
   * To get the saved recovery data in redis
   *
   * @return the saved recovery data or null if not exists
   */
  def retry = {

    if (enableRecovery) {

      redis.withClient {
        client => client.get("sample.stream.lib.http.recovery").getOrElse("")
      }
    }

    null
  }


}
