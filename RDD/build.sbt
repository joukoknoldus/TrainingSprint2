name := "RDD"

version := "0.1"

scalaVersion := "2.11.12"

val sparkVersion="2.4.0"

resolvers ++= Seq("apache-snapshots" at "http://repository.apache.org/snapshots")

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % sparkVersion % "provided",
  "org.apache.spark" %% "spark-sql" % sparkVersion % "provided",
  "org.apache.spark" %% "spark-mllib" % sparkVersion % "provided",

  "org.slf4j" % "slf4j-api" % "1.7.5",
  "org.slf4j" % "slf4j-log4j12" % "1.7.5"
)