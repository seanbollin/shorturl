package net.liquid_silk.shorturl

import io.finch.route._

object endpoint {
  import service._
  // REST endpoints "/shorturl" for creation (POST) and root "/" to access provided short URL, e.g. "/1h5C"
  val serviceUrls = (Post / ("shorturl") /> shorten) | Get / string /> convert
}