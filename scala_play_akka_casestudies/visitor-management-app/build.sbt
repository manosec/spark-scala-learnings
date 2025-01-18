name := """Visitor Management System"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.13.15"

libraryDependencies ++= Seq(
  guice,
  ws,
  // Database dependencies
  "org.playframework" %% "play-slick" % "6.1.0",
  "org.playframework" %% "play-slick-evolutions" % "6.1.0",
  "mysql" % "mysql-connector-java" % "8.0.26",
  
  // JSON handling
  "com.typesafe.play" %% "play-json" % "2.9.2",
  
  // Kafka for notifications
  "org.apache.kafka" % "kafka-clients" % "3.4.0",
  
  // JWT for authentication
  "com.auth0" % "java-jwt" % "4.3.0",
  
  // Testing
  "org.scalatestplus.play" %% "scalatestplus-play" % "7.0.0" % Test,
  
  filters,
  
  "com.typesafe" % "config" % "1.4.2"
)

libraryDependencies ++= Seq(
  "javax.mail" % "javax.mail-api" % "1.6.2",
  "com.sun.mail" % "javax.mail" % "1.6.2"
)