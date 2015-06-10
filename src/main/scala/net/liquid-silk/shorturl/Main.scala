/*
 * Copyright 2015, by Sean Bollin.
 *
 * A proof of concept URL shortener written in Scala using Redis and Twitter's Finagle/Finch.
 *
 * To build fat jar: sbt assembly
 *
 * Run: java -jar target/scala-2.11/shorturl-assembly-0.1.jar
 *
 * Usage:
 *
 * Make POST request with Content-Type application/json and body {"url":"http://www.urltoshorten.com/arbitraryPath?params=maybe"}
 * Receive corresponding 201 Created response with {"shorturl":"http://serviceDomainName.com/1H"} (a Base62 shortened version of URL generated off INCR Redis key)
 *
 * Make GET request to the "shorturl" provided after creating a shortened version.  This will immediately redirect with a 301 to the actual URL stored in Redis.
 *
 * Requires Redis server to be running locally on default port 6379
 */

package net.liquid_silk.shorturl

import io.finch._
import io.finch.request._
import io.finch.response._
import io.finch.route._
import io.finch.json._
import io.finch.argonaut._
import _root_.argonaut._, _root_.argonaut.Argonaut._

import com.twitter.finagle.Service
import com.twitter.finagle.Httpx
import com.twitter.util.Await

import redis.RedisClient
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import org.apache.commons.validator.routines.UrlValidator

import com.github.tototoshi.base62._

object Main extends App {
  val baseDomain = "http://ec2-54-165-63-62.compute-1.amazonaws.com:8081/" // service's domain name, e.g. http://urlShortener.com/

  val endpoint = (Post / ("shorturl") /> shorten) | Get / string /> convert // REST endpoints "/shorturl" for creation (POST) and root "/" to access provided short URL, e.g. "/1h5C"

  // handle JSON parsing of POST request body
  case class UrlToShort(url: String)

  implicit def UrlToShortDecodeJson: DecodeJson[UrlToShort] =
    jdecode1L(UrlToShort.apply)("url")

  val url: RequestReader[UrlToShort] = body.as[UrlToShort]

  // process POST shorten request
  def shorten() = new Service[HttpRequest, HttpResponse] {
    def apply(req: HttpRequest) = for {
      u <- url(req)
    } yield {
			println("- received: " + u.url)
      val shortened = processUrl(u.url)
      Created(io.finch.json.Json.obj("shorturl" -> shortened)) // Created is 201 response
    }
  }

  // handle GET request for Base62 shortURL
  def convert(encoded: String) = new Service[HttpRequest, HttpResponse] {
    def apply(req: HttpRequest) = {
      implicit val akkaSystem = akka.actor.ActorSystem()

      val redis = RedisClient()

      val futureResult = fetchShort(redis, encoded)

      val url = scala.concurrent.Await.result(futureResult, 5 seconds)

      val redirect: ResponseBuilder = MovedPermanently.withHeaders("Location" -> url) // MovedPermanently is 301 redirect
      val rep: HttpResponse = redirect(url)
      rep.toFuture
    }
  }

  // get actual URL from Base62 version
  def fetchShort(redis: RedisClient, encoded: String): Future[String] = {
    for {
      v <- redis.get(encoded)
    } yield {
      v.map(_.utf8String) mkString("")
    }
  }

  // generate the shortened URL
  def processUrl(url: String): String = {
    val validator = new UrlValidator(List("http","https").toArray)
    if(validator.isValid(url)) {
      implicit val akkaSystem = akka.actor.ActorSystem()

      val redis = RedisClient()

      val futureResult = genShort(redis, url)

      val newUrl = scala.concurrent.Await.result(futureResult, 5 seconds)

      akkaSystem.shutdown()

      baseDomain + newUrl
    } else {
      throw new Exception("The supplied url is invalid.")
    }
  }

  // generate Base62 shortURL off Atomically INCRemented Redis ID
  def genShort(redis: RedisClient, url: String): Future[String] = {
    for {
      uniqId <- redis.incr("SHORTURLID")
      val base62 = new Base62
      val encoded = base62.encode(uniqId)
      s <- redis.set(encoded.toString(), url)
    } yield {
      encoded.toString()
    }
  }

  Await.ready(Httpx.serve(":8081", endpoint)) // start server
}