name := "message-processor"
version := "1.0"
scalaVersion := "2.13.8"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.6.19",
  "com.typesafe.akka" %% "akka-http" % "10.2.9",
  "com.typesafe.akka" %% "akka-stream" % "2.6.19",
  "org.apache.kafka" % "kafka-clients" % "3.3.1",
  "io.spray" %% "spray-json" % "1.3.6"
)

libraryDependencies += "com.typesafe.akka" %% "akka-http-spray-json" % "10.2.9"
