package sample.stream.demo


import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model.{HttpResponse, HttpRequest}
import akka.util.ByteString
import com.redis._
import com.typesafe.config.ConfigFactory
import scala.concurrent._

import scala.concurrent.Future
import akka.stream.scaladsl._

import sample.stream.lib.http.ReactiveStream
import sample.stream.demo.JHTTP

import scala.util.{Failure, Success}

object RESTFulClient {

  def getByReactiveStream(endpoint: String, port: Int, message: String = "") {

    val rs = new ReactiveStream

    val connectionFlow: Flow[HttpRequest, HttpResponse, Future[Http.OutgoingConnection]] =
      Http().outgoingConnection(endpoint, port = port)

    val wordList: Iterator[List[String]] = scala.io.Source.fromFile("src/main/resources/2008.csv", "utf-8").getLines().map(line => line.replace(",", " ").split("\\s+").toList)
    val source = Source.fromIterator(() => wordList).mapConcat(identity)

    source.map {
      d =>
        Thread.sleep(2000)
        rs.post("/user/" + d, "")
    }.via(connectionFlow)
      .runWith(Sink.foreach {
        res => res.status.isSuccess() match {
          case true =>
            //println("==> send ok")
            println(res.entity)
          case _ =>
            println("==> send error")
        }
      })

  }

  def getByHTTP = {

    val ret = JHTTP.post("http://localhost:8080/user/2", "")
    println(ret)
  }

}
