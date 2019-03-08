import org.apache.commons.lang3.RandomStringUtils
import org.apache.log4j.BasicConfigurator
import org.apache.spark.mllib.random.RandomRDDs
import org.apache.spark.rdd.RDD
import org.apache.spark._
import org.apache.spark.sql._

import scala.util.Random
import java.nio.file.{Files, Paths}

//import RDDExample.CustomPartitioner

object RDDExample {
  def main(args: Array[String]): Unit = {
    val conf=new SparkConf().setMaster("local[2]").setAppName("RDDExample")

    val sc=new SparkContext(conf)

    val spark=SparkSession
      .builder()
      .appName("RDDExample")
      .getOrCreate()

    spark.sparkContext.setLogLevel("WARN")

    BasicConfigurator.configure()

    val homeDir="/home/jouko"
    val projectDir=homeDir+"/dev/project/TrainingSprints/TrainingSprint2/RDD/Output"
    val outPath1=projectDir+"/Output1"
    val outPath2=projectDir+"/Output2"
    val pathPartitionedById=projectDir+"/OutputPartitionedById"
    val pathPartitionedByName=projectDir+"/OutputPartitionedByName"

    val rddSize=10000
    val numPartitions=100
    val rdd1=createSaveLoadRdd(rddSize, outPath1, sc)
    val rdd2=createSaveLoadRdd(rddSize, outPath2, sc)

    repartitionByIdAndJoin(rdd1, rdd2, numPartitions, pathPartitionedById)

    val rddName1=repartitionByName(rdd1)
    val rddName2=repartitionByName(rdd2)
    joinByName(rddName1, rddName2, pathPartitionedByName)

    convertToDfAndJoin(rddName1, rddName2, spark)
  }

  def convertToDfAndJoin(rddName1: RDD[(String, Int)], rddName2: RDD[(String, Int)], spark: SparkSession): DataFrame = {
    import spark.implicits._

    val df1=rddName1.map( x => (x._2, x._1) ).toDF.as("df1")
    val df2=rddName2.map( x => (x._2, x._1) ).toDF.as("df2")

    val timeStart=System.currentTimeMillis
    val nameCol1: Column=df1.col("df1._2")
    val nameCol2: Column=df2.col("df2._2")

    val dfJoined=df1.join(df2, nameCol1===nameCol2, "inner")
    val timeEnd=System.currentTimeMillis

    println("Join DF by name took "+(timeEnd-timeStart)+" ms")
    //33 ms with 10,000 rows

    df1.show()
    dfJoined.show()

    dfJoined
  }

  def repartitionByIdAndJoin(rdd1: RDD[(Int, String)], rdd2: RDD[(Int, String)], numPartitions: Int, path: String): Unit = {
    val rangePartitioner=new RangePartitioner(numPartitions, rdd1)
    val rddRepartitioned1=rdd1.partitionBy(rangePartitioner)
    val rddRepartitioned2=rdd2.partitionBy(rangePartitioner)

    val rddJoined=rddRepartitioned1.join(rddRepartitioned2)

    rddJoined.take(10).foreach(println)

    rddJoined.saveAsTextFile(path)
  }

  def repartitionByName(rdd: RDD[(Int, String)]): RDD[(String, Int)] = {
    val rddFlipped=rdd.map( x => (x._2, x._1) )

    val namePartitioner=new CustomPartitioner(26)
    rddFlipped.partitionBy(namePartitioner)
  }

  def joinByName(rdd1: RDD[(String, Int)], rdd2: RDD[(String, Int)], path: String): RDD[((Int, Int), String)] = {
    val timeStart=System.currentTimeMillis
    val rddJoinedByName=rdd1.join(rdd1).map( x => (x._2, x._1) )
    val timeEnd=System.currentTimeMillis

    println("Join RDD by name took "+(timeEnd-timeStart)+" ms")
    //6 ms with 10,000 rows

    rddJoinedByName.saveAsTextFile(path)

    rddJoinedByName

  }

  def createRandomRdd(rddSize: Int, sc: SparkContext): RDD[(Int, String)] = {
    val names: Array[String] = Array("Jouko", "Susan", "Ram", "Jukka", "Katy", "Seija", "Peter", "Annika", "Sinikka", "Pam")
    val rdd: RDD[(Int, String)] = RandomRDDs.uniformRDD(sc, rddSize).map(d => (Random.nextInt(rddSize), names(Random.nextInt(names.length))))

    rdd.take(10).foreach(println)

    rdd
  }

  def createSaveLoadRdd(rddSize: Int, path: String, sc: SparkContext): RDD[(Int, String)] = {
    val rdd=createRandomRdd(rddSize, sc)
    rdd.saveAsObjectFile(path)
    sc.objectFile[(Int, String)](path)
  }

}

class CustomPartitioner(numParts: Int) extends Partitioner {
  override def numPartitions: Int = numParts
  override def getPartition(key: Any): Int = {
    key.toString.charAt(0).toInt-'A'.toInt
  }
}
