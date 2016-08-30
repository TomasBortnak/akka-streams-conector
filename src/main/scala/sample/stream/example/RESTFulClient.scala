package sample.stream.example


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

import sample.stream.lib.http.{QueryInfomation, ReactiveHttpRequest}
import sample.stream.example.JHTTP

import scala.util.{Failure, Success}
import org.json4s._
import org.json4s.native.JsonMethods._
import org.json4s.JsonDSL._

/**
 * This class to show how to use the library, the data is coming from a JSON file.
 */
object RESTFulClient {

  implicit val formats = DefaultFormats

  /**
   * To test the library by get a message from the service
   *
   * @param endpoint the endpoint of the service
   * @param port the port of the service
   * @param message the data to send by the request
   */
  def get(endpoint: String, port: Int, message: String = "") {

    val rs = new ReactiveHttpRequest
    val info = scala.io.Source.fromFile("src/main/resources/example.json", "utf-8").mkString("")

    val json = parse(info)
    val data = json.extract[List[QueryInfomation]]

    def success(response: HttpResponse): Unit = {

      println(response.entity)

    }

    def error(response: HttpResponse): Unit = {
      println("send error")
      println(response)
    }

    rs.get(endpoint, port, Iterator(data), success, error)

  }

  /**
   * To send the http request in sync mode
   */
  def getBySyncHTTP = {

    val ret = JHTTP.post("http://localhost:8080/user/2", "")
    println(ret)
  }

}
