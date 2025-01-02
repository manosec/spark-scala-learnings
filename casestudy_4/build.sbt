ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.12.10"

lazy val root = (project in file("."))
  .settings(
    name := "CaseStudy4"
  )
// Define Spark version
val sparkVersion = "3.5.1"

// Define Akka version
val akkaVersion = "2.6.20"

// Define Akka Stream Kafka version
val akkaKafkaVersion = "2.1.0"

// Define ScalaPB version
val scalaPBVersion = "0.11.12"

// Define Akka HTTP version
val akkaHttpVersion = "10.2.10"

libraryDependencies ++= Seq(
  // Spark dependencies
  "org.apache.spark" %% "spark-core" % sparkVersion,
  "org.apache.spark" %% "spark-sql" % sparkVersion,
  "org.apache.spark" %% "spark-streaming" % sparkVersion,
  "org.apache.spark" %% "spark-streaming-kafka-0-10" % sparkVersion,
  "org.apache.spark" %% "spark-sql-kafka-0-10" % sparkVersion,
  "org.apache.spark" %% "spark-avro" % sparkVersion,

  // Akka dependencies
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  "com.typesafe.akka" %% "akka-stream-kafka" % akkaKafkaVersion,

  // Akka HTTP dependency
  "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,

  // Akka HTTP testkit dependency
  "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion % Test,

  // Add Akka TestKit dependency
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion % Test,

  // Kafka dependencies
  "org.apache.kafka" % "kafka-clients" % "3.4.0",

  // Spark Protobuf module
  "org.apache.spark" %% "spark-protobuf" % sparkVersion,

  // Protobuf runtime dependency
  "com.google.protobuf" % "protobuf-java" % "3.24.3",

  // ScalaPB dependencies
  "com.thesamet.scalapb" %% "scalapb-runtime" % scalaPBVersion, // ScalaPB runtime
  "com.thesamet.scalapb" %% "scalapb-runtime-grpc" % scalaPBVersion, // For gRPC support

  // Other dependencies
  "org.scalatest" %% "scalatest" % "3.2.17" % Test, // Updated to the latest version
  "com.datastax.spark" %% "spark-cassandra-connector" % "3.0.1",
  "com.github.jnr" % "jnr-posix" % "3.1.7",
  "joda-time" % "joda-time" % "2.12.5", // Updated version
  "com.google.cloud.bigdataoss" % "gcs-connector" % "hadoop3-2.2.5",

  // CORS dependency
  "ch.megard" %% "akka-http-cors" % "1.1.3",

  // Spray JSON dependency
  "io.spray" %% "spray-json" % "1.3.6"
)

dependencyOverrides ++= Seq(
  "org.scala-lang.modules" %% "scala-parser-combinators" % "2.3.0"
)
libraryDependencies ++= Seq(
  "io.circe" %% "circe-core" % "0.14.5",
  "io.circe" %% "circe-generic" % "0.14.5",
  "io.circe" %% "circe-parser" % "0.14.5"
)
libraryDependencies += "com.typesafe.play" %% "play-json" % "2.10.0"