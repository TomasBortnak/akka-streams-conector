package sample.stream.demo


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

import sample.stream.lib.http.{QueryInfomation, ReactiveStream}
import sample.stream.demo.JHTTP

import scala.util.{Failure, Success}
import org.json4s._
import org.json4s.native.JsonMethods._
import org.json4s.JsonDSL._

object RESTFulClient {

  implicit val formats = DefaultFormats

  def getByReactiveStream(endpoint: String, port: Int, message: String = "") {

    val rs = new ReactiveStream

    //val wordList: Iterator[List[String]] = scala.io.Source.fromFile("src/main/resources/2008.csv", "utf-8").getLines().map(line => line.replace(",", " ").split("\\s+").toList)
    //val source = Source.fromIterator(() => wordList).mapConcat(identity)

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

  def getByHTTP = {

    val ret = JHTTP.post("http://localhost:8080/user/2", "")
    println(ret)
  }

}
