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

libraryDependencies += "org.mongodb" % "mongo-java-driver" % "2.12.2"

libraryDependencies += "org.mongodb.morphia" % "morphia" % "0.108"

libraryDependencies += "org.jongo" % "jongo" % "1.0"

libraryDependencies += "org.projectlombok" % "lombok" % "1.14.4"

libraryDependencies += "org.elasticsearch" % "elasticsearch" % "1.1.1"

libraryDependencies += "org.springframework" % "spring-context" % "3.2.10.RELEASE"

libraryDependencies += "org.springframework" % "spring-aop" % "3.2.10.RELEASE"

libraryDependencies += "org.springframework" % "spring-expression" % "3.2.10.RELEASE"

libraryDependencies += "org.springframework" % "spring-test" % "3.2.10.RELEASE"

libraryDependencies += "de.flapdoodle.embed" % "de.flapdoodle.embed.mongo" % "1.46.0"