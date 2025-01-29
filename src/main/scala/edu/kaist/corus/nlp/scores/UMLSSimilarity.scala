package edu.kaist.corus.nlp.scores

/**
  * Created by Junseok Park on 2017-04-09.
  */

import edu.kaist.corus.core.utils.{File, Json, Performance, Print}
import edu.kaist.corus.data.db.CORUSDB.{connect, getStatement}
import edu.kaist.corus.nlp.metamap.{MappingResult, MetamapAPI}

import scala.collection.mutable.ListBuffer
import scalaj.http.Http

case class SimilarityParam(measure:String, cui1:String, cui2:String)
case class SimilarityResult(cui1:String,cui2:String,measure:String,score:String,uri:String)
case class SimilarityResultJSON(result:SimilarityResult)

case class SimilarityResultWithNCTID(nctid:String,score:Double)


object UMLSSimilarity {

  private val apiAddr = "http://chrome.kaist.ac.kr:3310"
  private val cutOffValue = 0.3

  def getSimilarityScore(param:SimilarityParam): Double = {

    val data = Json.toJson(param)

    /**
      """{"measure":"vector",
        |"cui1":"colon",
        |"cui2":"breast"}
      """.stripMargin
    **/

    //println(data)
    val response = Http(apiAddr+"/umls-similarity/api/v1.47/getUMLSSimilarity").headers(Seq("Content-Type"->"application/json"))
    .postData(data).timeout(100000,1000000)

    val resultJson = Json.fromJson[SimilarityResultJSON](response.asString.body)
    println(resultJson)
    resultJson.result.score.toDouble
  }

  def getUMLSIDs(text:String) : List[String] = {
    val mmResult = Json.fromJson[MappingResult](MetamapAPI.processToJson(text).toString())
    val itr = mmResult.metamapResult.iterator
    var resultCUIs = ListBuffer.empty[String]

    while(itr.hasNext) {
      val termMappingResult = itr.next()
      val itrIn = termMappingResult.termMappingInfo.sortWith(_.conceptName < _.conceptName).iterator
      var conceptName:String = null
      while(itrIn.hasNext) {
        val termMappingInfo = itrIn.next()
        // only get first CUI per each words
        //println("previousConceptName: " + conceptName)
        //println(termMappingInfo.conceptID, termMappingInfo.conceptName)
        if (!termMappingInfo.conceptName.equalsIgnoreCase(conceptName)) resultCUIs += termMappingInfo.conceptID
        conceptName = termMappingInfo.conceptName

      }
    }

    //println("----")
    //println(resultCUIs)
    resultCUIs.toList
  }

  def calculateSimilarityScore(text:String): List[SimilarityResultWithNCTID] = {
    val cuiList = getUMLSIDs(text).distinct
    val inputLength = cuiList.length
    val resultScores = ListBuffer.empty[SimilarityResultWithNCTID]

    val (conn,stmt) = getStatement(connect())
    try {
      val rs = stmt.executeQuery("select clinicaltrialid, array_agg(umlsid) as umlsids from clinicaltrial_phenotype " +
        " where clinicaltrialid ='NCT00000300'" +
        " group by clinicaltrialid limit 1000;")

      while(rs.next) {
        // Test Time
        Performance.time {

          //val rusCuis = rs.getArray("umlsids").asInstanceOf[Array]
          val rusCuis = rs.getArray("umlsids").toString.replace("{", "").replace("}", "").split(",").toList.distinct
          val rusCuisLength = rusCuis.length
          val clinicaltrialID = rs.getString("clinicaltrialid")
          var clinicaltrialScore: Double = 0.0
          var denominator = 0
          for (cui <- cuiList) {
            //for (rusCui <- rusCuis) clinicaltrialScore += getSimilarityScore(SimilarityParam("vector",cui,rusCui))
            for (rusCui <- rusCuis) {
              val simScore = getSimilarityScore(SimilarityParam("vector", cui, rusCui))
              if (simScore >= cutOffValue) {
                clinicaltrialScore += simScore; denominator += 1
              }
            }
          }
          //resultScores += SimilarityResultWithNCTID(clinicaltrialID,(clinicaltrialScore/(rusCuisLength*inputLength)))
          val printValue = if (denominator > 0) {
            resultScores += SimilarityResultWithNCTID(clinicaltrialID, (clinicaltrialScore / denominator))
            SimilarityResultWithNCTID(clinicaltrialID, (clinicaltrialScore / denominator))
          } else {
            resultScores += SimilarityResultWithNCTID(clinicaltrialID, 0)
            SimilarityResultWithNCTID(clinicaltrialID, 0)
          }
          println(Json.toJson(printValue))
          File.writeStringToFile(Json.toJson(printValue)+"\n","20170410-CORUS-v1.0-results.txt")

          println("=====================================================================================================================================================")
          //println(Json.toJson(resultScores))
        }
      }
      resultScores.toList.sortWith(_.score > _.score)
    } finally {
      conn.close
      println(Print.prettyPrint(resultScores.toList.sortWith(_.score > _.score)))
      //return resultScores.toList
    }
  }

  def main(args: Array[String]): Unit = {
    //val data = SimilarityParam("vector","cancer","lung")
    //println(getSimilarityScore(data))

    val inputText = "Clinical trial of polyphenol extract, Seapolynol, isolated from Ecklonia Cava to verify its safety and positive effects on blood sugar level after the meal."
    //getUMLSIDs("Clinical trial of polyphenol extract, Seapolynol, isolated from Ecklonia Cava to verify its safety and positive effects on blood sugar level after the meal.")

    val result = calculateSimilarityScore(inputText)
    println(result)

  }

}
