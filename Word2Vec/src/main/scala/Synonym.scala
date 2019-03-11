
import edu.stanford.nlp.ling._
import edu.stanford.nlp.pipeline._
import java.util._

import edu.stanford.nlp.naturalli.NaturalLogicAnnotations
import edu.stanford.nlp.simple.Sentence
import edu.stanford.nlp.util.CoreMap
import edu.stanford.nlp.simple._
import org.apache.spark.mllib.feature.{Word2Vec, Word2VecModel}
import org.apache.log4j.BasicConfigurator
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql._

import Word2VecExample._

object Synonym {
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
    val modelPath=projectDir+"/Output/model_StopWords"

    val model=Word2VecModel.load(sc, modelPath)

    for (arg <- args) {
      findSynonymsModel(model, arg)
    }
  }
}
