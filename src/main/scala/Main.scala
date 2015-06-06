import io.finch._
import io.finch.request._
import io.finch.response._
import io.finch.route._

import com.twitter.finagle.Service
import com.twitter.finagle.Httpx
import com.twitter.util.Await

import redis.RedisClient
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import org.apache.commons.validator.routines.UrlValidator

object Main extends App {
  val baseDomain = "http://ec2-54-165-63-62.compute-1.amazonaws.com:8081/"

  val endpoint = (Get / ("shorten") / string /> shorten) | Get / string /> convert

  val url = paramOption("url").withDefault("")

  def shorten(path: String) = new Service[HttpRequest, HttpResponse] {
    def apply(req: HttpRequest) = for {
      u <- url(req)
    } yield {
      val shortened = processUrl(u)
      Ok(s"Received: $shortened")
    }
  }

  def convert(encoded: String) = new Service[HttpRequest, HttpResponse] {
    def apply(req: HttpRequest) = {
      implicit val akkaSystem = akka.actor.ActorSystem()

      val redis = RedisClient()

      val futureResult = fetchShort(redis, encoded)

      val url = scala.concurrent.Await.result(futureResult, 5 seconds)
      Ok("redirect to: " + url).toFuture
    }
  }

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

  def fetchShort(redis: RedisClient, encoded: String): Future[String] = {
    for {
      v <- redis.get(encoded)
    } yield {
      v.map(_.utf8String) mkString("")
    }
  }

  def genShort(redis: RedisClient, url: String): Future[String] = {
    for {
      uniqId <- redis.incr("SHORTURLID")
      val base62 = new Base62
      val encoded = base62.encode(uniqId)
      s <- redis.set(encoded.toString(), url)
    } yield {
      println("*** uniqId generated: " + uniqId)
      println("*** uniqId encoded to: " + encoded)
      encoded.toString()
    }
  }

  Await.ready(Httpx.serve(":8081", endpoint))
}