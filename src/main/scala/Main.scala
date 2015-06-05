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

object Main extends App {
  val endpoint = Get / ("hello" | "hi") / string /> hello
  val title = paramOption("title").withDefault("")

  def hello(name: String) = new Service[HttpRequest, HttpResponse] {
    def apply(req: HttpRequest) = for {
      t <- title(req)
    } yield Ok(s"Hello, $t $name!")
  }
  println("testing")

  implicit val akkaSystem = akka.actor.ActorSystem()

  val redis = RedisClient()

  val futurePong = redis.ping()
  println("Ping sent!")
  futurePong.map(pong => {
    println(s"Redis replied with a $pong")
  })

  scala.concurrent.Await.result(futurePong, 5 seconds)

  val futureResult = doSomething(redis)

  scala.concurrent.Await.result(futureResult, 5 seconds)

  akkaSystem.shutdown()

  def doSomething(redis: RedisClient): Future[Boolean] = {
    // launch command set and del in parallel
    val s = redis.set("redis", "is awesome")
    val d = redis.del("i")
    for {
      set <- s
      del <- d
      incr <- redis.incr("i")
      iBefore <- redis.get("i")
      incrBy20 <- redis.incrby("i", 20)
      iAfter <- redis.get("i")
    } yield {
      println("SET redis \"is awesome\"")
      println("DEL i")
      println("INCR i")
      println("INCRBY i 20")
      val ibefore = iBefore.map(_.utf8String)
      val iafter = iAfter.map(_.utf8String)
      println(s"i was $ibefore, now is $iafter")
      iafter == "20"
    }
  }

  Await.ready(Httpx.serve(":8081", endpoint))
}