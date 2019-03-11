
import edu.stanford.nlp.ling._
import edu.stanford.nlp.pipeline._
import java.util._
import scala.io.Source


import edu.stanford.nlp.naturalli.NaturalLogicAnnotations
import edu.stanford.nlp.simple.Sentence
import edu.stanford.nlp.util.CoreMap
import edu.stanford.nlp.simple._
import org.apache.spark.mllib.feature.{HashingTF, IDF, Word2Vec, Word2VecModel}
import org.apache.spark.mllib.linalg.Vector
import org.apache.log4j.BasicConfigurator
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql._

import scala.annotation.tailrec

object Word2VecExample {
  def main(args: Array[String]): Unit = {
    val conf=new SparkConf().setMaster("local[2]").setAppName("Word2Vec")

    val sc=new SparkContext(conf)

    val spark=SparkSession
      .builder()
      .appName("Word2Vec")
      .getOrCreate()

    spark.sparkContext.setLogLevel("WARN")

    BasicConfigurator.configure()

    val homeDir="/home/jouko"
    val projectDir=homeDir+"/dev/project/TrainingSprints/TrainingSprint2/Word2Vec"
    val path=projectDir+"/data"
    //val path=projectDir+"/SmallData"

    val rdd=sc.wholeTextFiles(path)
    println(rdd.count())
    val rddCleaned=rdd.flatMap( x => getCleanedSentences(x._2) )

    println(rddCleaned.count())
    //rddCleaned.take(100).foreach(println)
    rddCleaned.sample(false, 0.0001, 10).foreach(println)
    //rddCleaned.sample(false, 0.01, 10).foreach(println)
    val rddCleanedAndSplit=rddCleaned.map( x => x.split(" ").toSeq )

    val vectorSizes=Array(10, 20, 40, 80, 150, 300)
    val windowSizes=Array(3, 5, 10)

    //val vectorSizes=Array(20)
    //val windowSizes=Array(5)
    val minCounts=Array(10, 100, 500)

    //tfIdf(rddCleanedAndSplit)
    //sweepParameters(rddCleanedAndSplit, vectorSizes, windowSizes, minCounts)

    val modelPath=projectDir+"/Output/model_StopWords"
    val word2vec=new Word2Vec().setVectorSize(150).setWindowSize(3).setMinCount(100)

    val model=word2vec.fit(rddCleanedAndSplit)
    model.save(sc, modelPath)
  }

  def sweepParameters(rddCleaned: RDD[Seq[String]], vectorSizes: Array[Int], windowSizes: Array[Int], minCounts: Array[Int]): Unit = {
    for (vectorSize <- vectorSizes) {
      for (windowSize <- windowSizes) {
        for (minCount <- minCounts) {findSynonyms(rddCleaned, vectorSize, windowSize, minCount)}
      }
    }
  }

  def findSynonyms(rddCleaned: RDD[Seq[String]], vectorSize: Int, window: Int, minCount: Int): Unit = {
    println()
    println()
    println("vectorSize= "+vectorSize+" window= "+window+ " minCount= "+minCount)
    val word2vec=new Word2Vec().setVectorSize(vectorSize).setWindowSize(window).setMinCount(minCount)

    val model=word2vec.fit(rddCleaned)

    val synonyms=model.findSynonyms("beautiful", 10)

    for ((synonym, cosineSimilarity) <- synonyms) {
      println(s"$synonym $cosineSimilarity")
    }
  }
  /*
  def tfIdf(rddCleaned: RDD[Seq[String]]): Unit = {
    val hashingTF=new HashingTF()
    val tf: RDD[Vector]=hashingTF.transform(rddCleaned)
    val idf=new IDF().fit(tf)
    val tfidf=idf.transform(tf)
    tf.take(10).foreach(println)
    tfidf.take(10).foreach(println)
  }
  */

  def findSynonymsModel(model: Word2VecModel, word: String): Unit = {
    println()
    println()
    println(word)
    val synonyms=model.findSynonyms(word, 4)

    for ((synonym, cosineSimilarity) <- synonyms) {
      println(s"$synonym $cosineSimilarity")
    }
  }

  def getStopWordsFromFile(): Array[String] = {
    val fileName="/home/jouko/dev/project/TrainingSprints/TrainingSprint2/Word2Vec/StopWords/50.0/part-00000"
    val bufferedSource=Source.fromFile(fileName)
    val lines = (for (line <- bufferedSource.getLines()) yield line).toArray
    lines.map( x => x.replace("[", "").replace("]", "") )
  }

  def getListOfStopWords(): Array[String] = {
    //https://github.com/Yoast/YoastSEO.js/blob/develop/src/config/stopwords.js
    val stopWords: Array[String]=Array("-rsb-", "-lrb-", "-rrb-", "-lsb-", ",", "!", "?", ";", ".", ",", "'", "=", "-", "'s", "--", "#", "@", "_", "''", "`", "``", "(", ")", "[", "]", "/", "<", ">", "<p>", "</p>", "oh", "ooh", "uh", "um", "umm", "huh", "hmm", "hmmm", "a", "about", "above", "after", "again", "against", "all", "am", "an", "and", "any", "are", "as", "at", "be", "because", "been", "before", "being", "below", "between", "both", "but", "by", "could", "did", "do", "does", "doing", "down", "during", "each", "few", "for", "from", "further", "had", "has", "have", "having", "he", "he'd", "he'll", "he's", "her", "here", "here's", "hers", "herself", "him", "himself", "his", "how", "how's", "i", "i'd", "i'll", "i'm", "i've", "if", "in", "into", "is", "it", "it's", "its", "itself", "let's", "me", "more", "most", "my", "myself", "nor", "of", "on", "once", "only", "or", "other", "ought", "our", "ours", "ourselves", "out", "over", "own", "same", "she", "she'd", "she'll", "she's", "should", "so", "some", "such", "than", "that", "that's", "the", "their", "theirs", "them", "themselves", "then", "there", "there's", "these", "they", "they'd", "they'll", "they're", "they've", "this", "those", "through", "to", "too", "under", "until", "up", "very", "was", "we", "we'd", "we'll", "we're", "we've", "were", "what", "what's", "when", "when's", "where", "where's", "which", "while", "who", "who's", "whom", "why", "why's", "with", "would", "you", "you'd", "you'll", "you're", "you've", "your", "yours", "yourself", "yourselves")
    stopWords++getStopWordsFromFile()
  }

  def stemWord(word: String): String = {
    word.replaceFirst("ing$", "").replaceFirst("ed$", "")
  }

  def tokensToCleanedSentence(tokens: List[CoreLabel], stopWords: Array[String], n: Int): String = {
    if (n==tokens.size()) { "" }
    else {
      val lemma = tokens.get(n).get(classOf[CoreAnnotations.LemmaAnnotation]).toLowerCase
      val restOfSentence=tokensToCleanedSentence(tokens, stopWords, n+1)
      if (!stopWords.contains(lemma) && !lemma.contains(':') && !lemma.contains('#')) { lemma+" "+restOfSentence}
      else { restOfSentence }
    }
  }

  @tailrec
  def cleanedSentencesToArray(result: Array[String], sentences: List[CoreMap], stopWords: Array[String], n: Int): Array[String] = {
    if (n==sentences.size) { result }
    else {
      val tokens = sentences.get(n).get(classOf[CoreAnnotations.TokensAnnotation])
      val cleanedSentence: String=if (tokens.size<100) { tokensToCleanedSentence(tokens, stopWords, 0) } else { "" }
      cleanedSentencesToArray(cleanedSentence+:result, sentences, stopWords, n+1)
    }
  }

  def getCleanedSentences(text: String): Array[String] = {
    val props = new Properties()
    props.setProperty("annotators", "tokenize,ssplit,pos,lemma")
    val pipeline=new StanfordCoreNLP(props)
    val doc=new Annotation(text)
    pipeline.annotate(doc)
    val sentences=doc.get(classOf[CoreAnnotations.SentencesAnnotation])
    val stopWords: Array[String]=getListOfStopWords()
    val result: Array[String] = Array()
    val cleanedSentences=cleanedSentencesToArray(result, sentences, stopWords, 0)
    cleanedSentences.filter( x => x!="" && !x.matches(".*\\d+.*") ).map( x => x.replaceAll("[^a-zA-Z -]", ""))
  }
}
