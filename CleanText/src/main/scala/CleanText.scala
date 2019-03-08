import edu.stanford.nlp.naturalli.NaturalLogicAnnotations
import edu.stanford.nlp.util.CoreMap
import edu.stanford.nlp.pipeline.StanfordCoreNLP
import edu.stanford.nlp.ling._
import edu.stanford.nlp.pipeline._
import java.util._

import edu.stanford.nlp.pipeline.CoreNLPProtos.DependencyGraph.Node
import org.apache.log4j.BasicConfigurator
import org.apache.spark.sql.SparkSession
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.functions._

import scala.xml.XML

object CleanText {
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setMaster("local[2]").setAppName("CleanText")

    val sc = new SparkContext(conf)

    val spark = SparkSession
      .builder()
      .appName("CleanText")
      .getOrCreate()

    spark.sparkContext.setLogLevel("WARN")

    val homeDir = "/home/jouko"
    val path = homeDir + "/dev/project/TrainingSprints/TrainingSprint2/CleanText/data"

    val rdd = sc.wholeTextFiles(path)
    val rddParagraphs = rdd.flatMap( x => getXmlParagraphs(x._1, x._2))
    val rddSentences = rddParagraphs.flatMap(x => getSentences(x))

    rddParagraphs.collect().foreach(println)
    rddSentences.collect.foreach(println)
  }

  def cleanParagraph(p: String): String = {
    val p2=p.replaceFirst("\\<p .*?\\>", "")
    val p3=p2.reverse.replaceFirst(">p/<", "").reverse
    val p4=p3.replaceAll("\\<.*?\\>.*?\\</.*?\\>", "")
    val p5=p4.replaceAll("\\<.*?\\>", "")
    p5
  }

  def paragraphsToArray(it: scala.Iterator[xml.Node], file: String, n: Int): Array[(String, Int, String)] = {
    if (it.hasNext) {
      (file, n, cleanParagraph(it.next.toString))+:paragraphsToArray(it, file, n+1)
    } else { Array() }
  }

  def getXmlParagraphs(file: String, text: String): Array[(String, Int, String)] = {
    val cleanedText=text.replaceFirst("\\<\\!DOCTYPE .*?\\>", "")
    val xmlText=XML.loadString(cleanedText)
    val nodes=xmlText \\ "p"
    val it: scala.Iterator[xml.Node]=nodes.iterator

    paragraphsToArray(it, file, 0)
  }

  def sentencesToArray(file: String, pId: Int, sentences: List[CoreMap], n: Int): Array[(String, Int, String)] = {
    if (n==sentences.size()) { Array() }
    else {
      (file, pId, sentences.get(n).toString)+:sentencesToArray(file, pId, sentences, n+1)
    }
  }

  def getSentences(text: (String, Int, String)): Array[(String, Int, String)] = {
    val props = new Properties()
    props.setProperty("annotators", "tokenize,ssplit")
    val pipeline = new StanfordCoreNLP(props)
    val doc=new Annotation(text._3)
    pipeline.annotate(doc)
    val sentences=doc.get(classOf[CoreAnnotations.SentencesAnnotation])
    sentencesToArray(text._1, text._2, sentences, 0)
  }
}


