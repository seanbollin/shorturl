import java.net.InetSocketAddress
import java.nio.charset.Charset

import com.twitter.finagle.Service
import com.twitter.finagle.httpx.Request
import org.jboss.netty.buffer.ChannelBuffers
import org.jboss.netty.handler.codec.http.{DefaultHttpRequest, HttpMethod, HttpVersion}
import org.scalatest._
import net.liquid_silk.shorturl._

import com.twitter.util.Await

import io.finch._

class MainSpec extends FlatSpec with Matchers {

  val api: Service[HttpRequest, HttpResponse] = endpoint.serviceUrls
  val await = mkAwait(api)

  it should "respond with a 201 when posting a valid URL" in {
    val req = POST("/shorturl", "{\"url\": \"http://www.liquid-silk.net\"}")

    //await(req).status shouldBe io.finch.response.Created().status
  }

  def POST(path: String, body: String): Request = POST(path, Option(body))

  def POST(path: String): Request = POST(path, None)

  def POST(path: String, body: Option[String]): Request = {
    val r = mkHttpRequest(HttpMethod.POST, path)
    body.foreach { b =>
      val buf = ChannelBuffers.copiedBuffer(b, Charset.defaultCharset())
      r.setContent(buf)
      r.headers().add("content-length", buf.readableBytes())
    }

    request(r)
  }

  def mkAwait(service: Service[HttpRequest, HttpResponse]): Request => HttpResponse =
    (req) => Await.result(service(req))

  def GET(path: String): Request = {
    val r = mkHttpRequest(HttpMethod.GET, path)
    request(r)
  }

  def request(r: DefaultHttpRequest): Request =
    new Request {
      val httpRequest = r
      lazy val remoteSocketAddress = new InetSocketAddress(0)
    }

  def mkHttpRequest(m: HttpMethod, p: String): DefaultHttpRequest = {
    val r = new DefaultHttpRequest(HttpVersion.HTTP_1_1, m, p)
    r
  }
}