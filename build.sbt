name := "shorturl"

version := "0.1"

scalaVersion := "2.11.5"

resolvers += "rediscala" at "http://dl.bintray.com/etaty/maven"

libraryDependencies ++= Seq(
  "com.github.finagle" %% "finch-core" % "0.6.0",
  "com.twitter" %% "finagle-httpx" % "6.25.0",
  "com.twitter" %% "finagle-http" % "6.25.0",
  "com.github.finagle" %% "finch-argonaut" % "0.6.0",
  "com.github.finagle" %% "finch-json" % "0.6.0",
  "com.etaty.rediscala" %% "rediscala" % "1.4.0",
  "commons-validator" % "commons-validator" % "1.4.0",
  "commons-beanutils" % "commons-beanutils" % "1.9.2",
  "io.argonaut" %% "argonaut" % "6.0.4",
  "org.scalatest" % "scalatest_2.11" % "2.2.4" % "test",
	"com.typesafe" % "config" % "1.3.0"
)