name := """SIP"""

version := "1.0"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.8"

libraryDependencies ++= Seq(
  jdbc,
  cache,
  ws,
  specs2 % Test,
  "com.github.nscala-time" %% "nscala-time" % "2.14.0",
  "org.json4s" %% "json4s-native" % "3.3.0",
  "org.json4s" %% "json4s-ext" % "3.3.0", 
  "org.mongodb.scala" %% "mongo-scala-driver" % "1.2.0-beta1"
)

//libraryDependencies += "com.google.guava" % "guava" % "19.0"

resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"

// Play provides two styles of routers, one expects its actions to be injected, the
// other, legacy style, accesses its actions statically.
//routesGenerator := InjectedRoutesGenerator

scalacOptions ++= Seq("-feature")

fork in run := false