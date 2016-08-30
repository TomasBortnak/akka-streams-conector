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

import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model.{HttpResponse, HttpRequest}
import akka.stream.ThrottleMode
import akka.util.ByteString
import com.redis._
import com.typesafe.config.ConfigFactory
import scala.concurrent._

import scala.concurrent.Future
import akka.stream.scaladsl._

import sample.stream.lib.http.ReactiveStream
import sample.stream.demo.JHTTP

import scala.util.{Failure, Success}

/**
 *
 * http://doc.akka.io/docs/akka/2.4/scala/http/client-side/connection-level.html
 *
 */

case class QueryInfomation(query: String, data: String)

class ReactiveStream {

  implicit val formats = DefaultFormats

  val host = ConfigFactory.load().getString("sample.stream.lib.http.redis.host")
  val port = ConfigFactory.load().getString("sample.stream.lib.http.redis.port")

  val redis = new RedisClientPool(host, port.toInt)
  val enableRecovery = ConfigFactory.load().getString("sample.stream.lib.http.recovery.enabled").toBoolean

  var latestQueryString = ""
  var latestMessage = ""
  var json = ""

  /**
   * To send a stream of http request by HTTP GET with akka stream & http
   *
   * @param endpoint the endponint of the service, just domain name
   * @param port the port of the service
   * @param queryInfo the query information including query string and the data to send
   * @param success the callback to deal with success response
   * @param error the callback to deal with error response
   * @return
   */

  def get(endpoint: String, port: Int, queryInfo: Iterator[List[QueryInfomation]], success: HttpResponse => Unit, error: HttpResponse => Unit) = {

    val connectionFlow: Flow[HttpRequest, HttpResponse, Future[Http.OutgoingConnection]] =
      Http().outgoingConnection(endpoint, port = port)

    val source = Source.fromIterator(() => queryInfo).mapConcat(identity)

    println(enableRecovery)

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
   * @param endpoint the endponint of the service, just domain name
   * @param port the port of the service
   * @param queryInfo the query information including query string and the data to send
   * @param success the callback to deal with success response
   * @param error the callback to deal with error response
   * @return
   */

  def post(endpoint: String, port: Int, queryInfo: Iterator[List[QueryInfomation]], success: HttpResponse => Unit, error: HttpResponse => Unit) = {

    val connectionFlow: Flow[HttpRequest, HttpResponse, Future[Http.OutgoingConnection]] =
      Http().outgoingConnection(endpoint, port = port)

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
