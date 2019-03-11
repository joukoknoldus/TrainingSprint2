
import edu.stanford.nlp.ling._
import edu.stanford.nlp.pipeline._
import java.util._

import edu.stanford.nlp.naturalli.NaturalLogicAnnotations
import edu.stanford.nlp.simple.Sentence
import edu.stanford.nlp.util.CoreMap
import edu.stanford.nlp.simple._
import org.apache.spark.mllib.feature.{HashingTF, IDF, Word2Vec, Word2VecModel}
import org.apache.log4j.BasicConfigurator
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql._
import org.apache.spark.sql.functions._
import Word2VecExample._
import org.apache.spark.mllib.linalg.Vector

object TfIdf {
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
    //val path=projectDir+"/SmallData"
    val path=projectDir+"/data"
    val rddDocuments=sc.wholeTextFiles(path).map( x => x._2.replaceAll("[^a-zA-Z .-]", "").toLowerCase.split(" ").toSeq )

    calcIdf(rddDocuments, spark, projectDir)

  }

  def calcIdf(rddDocuments: RDD[Seq[String]], spark: SparkSession, projectDir: String): Unit = {
    import spark.implicits._
    val df=rddDocuments.toDF
    val df2=df.withColumn("doc_id", monotonically_increasing_id())
    val columns=Array(col("doc_id")):+(explode(col("value")) as "token")
    val unfoldedDocs=df2.select(columns: _*)
    val tf=unfoldedDocs.groupBy("doc_id", "token").agg(count("doc_id") as "tf")
    val freq=tf.groupBy("token").agg(countDistinct("doc_id") as "df")
    val ndoc=rddDocuments.count()
    //val thresholds=Array(0.99, 0.98, 0.97, 0.96, 0.95, 0.90, 0.80)
    val thresholds=Array(0.70, 0.60, 0.50)
    for (threshold <- thresholds) {
      val stopWords = freq.filter(col("df") > ndoc * threshold)
      stopWords.show(false)
      val path=projectDir+"/StopWords/"+(threshold*100)
      stopWords.select("token").repartition(1).toJavaRDD.saveAsTextFile(path)
    }
  }
}

