name := "CleanText"

version := "0.1"

scalaVersion := "2.11.12"

val sparkVersion="2.4.0"
val nlpVersion="3.9.2"

resolvers ++= Seq("apache-snapshots" at "http://repository.apache.org/snapshots")

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % sparkVersion % "provided",
  "org.apache.spark" %% "spark-sql" % sparkVersion % "provided",
  
  "edu.stanford.nlp" % "stanford-corenlp" % nlpVersion,
  "edu.stanford.nlp" % "stanford-corenlp" % nlpVersion classifier "models-english",
  "edu.stanford.nlp" % "stanford-corenlp" % nlpVersion classifier "models",
  
  "org.slf4j" % "slf4j-api" % "1.7.5",
  "org.slf4j" % "slf4j-log4j12" % "1.7.5"
)