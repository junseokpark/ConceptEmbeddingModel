package edu.kaist.corus.nlp.chemspider

import scala.collection.mutable
import scala.collection.mutable.HashMap
import scalaj.http.Http

/**
  * Created by Junseok Park on 2017-04-09.
  */
object ChemSpiderAPI {
  // private final  val LOG:Logger = LogManager.getLogger(ChemSpiderAPI.getClass.getName)
  private val chemSpidertoken = "d6ca2df2-ff35-40ea-bd1f-c6734f328dab"
  private val chemSpiderSearchAddr = "http://www.chemspider.com/Search.asmx"

  def simpleSearch(chemicalName:String): Unit = {
    // Do simple search to get CS ID from ChemSpider
    val response = Http(chemSpiderSearchAddr+"/SimpleSearch").headers(Seq("Content-Type"->"application/x-www-form-urlencoded"))
      .postForm(Seq("query"->chemicalName,"token"->chemSpidertoken))

    // get HttpResponse then convert it to string with only body contents
    val resultBody = response.asString.body
    // convert the string to XML then extract int(CSID) from XML contents
    val chemSpiderCodes = ( scala.xml.XML.loadString(resultBody.substring(resultBody.indexOf('\n')+1)) \ "int").map(_.text)

    if (chemSpiderCodes.length > 1) {
      var i = 0
      for (k <- chemSpiderCodes.iterator) {
        // get only first result (For test use)
        if (i == 0) {
          val compoundInfoHash = getCompoundInfo(k)
          println("Result Information")
          println("==================")
          println(k)
          println(compoundInfoHash.get("InChI").fold("")(_.toString))
          println(compoundInfoHash.get("InChIKey").fold("")(_.toString))
          println(compoundInfoHash.get("SMILES").fold("")(_.toString))
        }
        i += 1
      }
    } else {
      // nothing to return

    }

  }

  def getCompoundInfo(CSID:String) : HashMap[String,String] = {
    var resultHashMap = mutable.HashMap.empty[String,String]
    val response = Http(chemSpiderSearchAddr+"/GetCompoundInfo").headers(Seq("Content-Type"->"application/x-www-form-urlencoded"))
      .postForm(Seq("CSID"->CSID,"token"->chemSpidertoken))

    // get HttpResponse then convert it to string with only body contents
    val resultBody = response.asString.body
    // convert the string to XML
    val compoundInfo = scala.xml.XML.loadString(resultBody.substring(resultBody.indexOf('\n')+1))

    println("====ALL Information of your compound")
    println(compoundInfo)

    val InChI = (compoundInfo \ "InChI").map(_.text)
    if (InChI.length > 0) resultHashMap.put("InChI",InChI.head)

    val InChIKey = (compoundInfo \ "InChIKey").map(_.text)
    if (InChIKey.length > 0) resultHashMap.put("InChIKey",InChIKey.head)

    val SMILES = (compoundInfo \ "SMILES").map(_.text)
    if (SMILES.length > 0) resultHashMap.put("SMILES",SMILES.head)

    resultHashMap

  }

  def main(args: Array[String]) {

    simpleSearch("oil")

  }
}
