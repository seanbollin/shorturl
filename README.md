# ShortURL w/ Scala, Finch/Finagle, and Redis

A proof of concept URL shortener written in Scala using Redis and Twitter's Finagle/Finch.

To build fat jar: sbt assembly

Run: java -jar target/scala-2.11/shorturl-assembly-0.1.jar

Usage:

Make POST request with Content-Type application/json and body {"url":"http://www.urltoshorten.com/arbitraryPath?params=maybe"}

Receive corresponding 201 Created response with {"shorturl":"http://serviceDomainName.com/1H"} (a Base62 shortened version of URL generated off INCR Redis key)

Make GET request to the "shorturl" provided after creating a shortened version.  This will immediately redirect with a 301 to the actual URL stored in Redis.

Requires Redis server to be running locally on default port 6379