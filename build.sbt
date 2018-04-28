name := """SIP"""

version := "1.0.10"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.12"

libraryDependencies ++= Seq(
  jdbc,
  cache,
  ws,
  specs2 % Test,
  "com.github.nscala-time" %% "nscala-time" % "2.16.0",
  "org.mongodb.scala" %% "mongo-scala-driver" % "1.2.1"
)

libraryDependencies += "org.scalikejdbc" %% "scalikejdbc"                  % "2.5.2"
libraryDependencies += "org.scalikejdbc" %% "scalikejdbc-config"           % "2.5.2"
libraryDependencies += "org.scalikejdbc" %% "scalikejdbc-play-initializer" % "2.5.1"

mappings in Universal ++=
(baseDirectory.value / "report_template" * "*" get) map
    (x => x -> ("report_template/" + x.getName))

mappings in Universal ++=
(baseDirectory.value / "importEPA/backup/" * "*" get) map
    (x => x -> ("importEPA/backup/" + x.getName))

resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"

// Play provides two styles of routers, one expects its actions to be injected, the
// other, legacy style, accesses its actions statically.
//routesGenerator := InjectedRoutesGenerator

scalacOptions ++= Seq("-feature")

fork in run := false