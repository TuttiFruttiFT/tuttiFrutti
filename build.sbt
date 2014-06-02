name := """tuttiFrutti"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.11.1"

libraryDependencies ++= Seq(
  javaJdbc,
  javaEbean,
  cache,
  javaWs
)

libraryDependencies += "org.mongodb" % "mongo-java-driver" % "2.11.4"

libraryDependencies += "org.mongodb.morphia" % "morphia" % "0.107"

libraryDependencies += "org.jongo" % "jongo" % "1.0"

libraryDependencies += "org.projectlombok" % "lombok" % "1.12.6"

libraryDependencies += "org.elasticsearch" % "elasticsearch" % "1.1.1"