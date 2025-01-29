package study.randomForest

/**
  * Created by Junseok Park on 2017-06-13.
  */

import org.apache.spark.{SparkConf,SparkContext}
import org.apache.spark.mllib.tree.RandomForest
import org.apache.spark.mllib.tree.model.RandomForestModel
import org.apache.spark.mllib.util.MLUtils

object RandomForestClassification {

  def main(args: Array[String]): Unit = {

    val conf = new SparkConf().setAppName("RandomForestClassification")
    val sc = new SparkContext(conf)

    val data = MLUtils.loadLabeledPoints(sc,"")

    val splits = data.randomSplit(Array(0.7,0.3))
    val (trainingData, testData) = (splits(0),splits(1))

    // Train a RandomForest model
    val numClasses = 2
    val categoricalFeaturesInfo = Map[Int,Int]()
    val numTrees = 3
    val featureSubsetStrategy = "auto"
    val impurity = "gini"
    val maxDepth= 4
    val maxBins = 32

    val model = RandomForest.trainClassifier(trainingData,numClasses,categoricalFeaturesInfo,
    numTrees,featureSubsetStrategy,impurity,maxDepth,maxBins)

    val labelAndPreds = testData.map {
      point => val predition = model.predict(point.features)
        (point.label,predition)
    }

    val testErr = labelAndPreds.filter( r => r._1 != r._2).count.toDouble / testData.count()
    println("Test Error = " + testErr)
    println("Learned classification forest model:\n" + model.toDebugString)

    // Save and load model
    model.save(sc, "")
    val sameModel = RandomForestModel.load(sc, "")

    sc.stop()

  }

}
