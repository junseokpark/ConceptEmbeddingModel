package edu.kaist.corus.nlp.metamap

import edu.kaist.corus.core.utils.File
import edu.kaist.corus.nlp.model.NERResult
import gov.nih.nlm.nls.metamap._
import play.api.libs.json._

import scala.collection.JavaConversions._
import scala.collection.mutable.ListBuffer

/**
  * Created by Junseok Park
  *
  * References;
  * https://github.com/vickysam/Metamap-Service
  */

// Term Info
case class TermInfo(id:String,
                    utternaceText:String,
                    positionStart:Int,
                    positionEnd:Int)
// phrase:String) omit for reducing data size

// Mapping Type : candidate, mapping
case class TermMappingInfo(mappingType:String,
                           mappingScore:Int,
                           conceptID:String,
                           conceptName:String,
                           prefferedName:String,
                           matchedWords:List[String],
                           semanticTypes:List[String],
                           isHead:Boolean,
                           isOvermatched:Boolean,
                           sources:List[String],
                           position:List[Int]
                          )

case class TermMappingResult(termInfo:TermInfo,termMappingInfo:Seq[TermMappingInfo])
case class MappingResult(metamapResult:Seq[TermMappingResult])


object MetamapAPI {

  /** MetaMap api Instance */
  var api: MetaMapApi = new MetaMapApiImpl
  api.setHost("143.248.30.173")
  api.setPort(8066)
  //api.setOptions("-Al")
  //api.setOptions("-C")
  //api.setOptions("-b")

  def process(text:String) : List[Result] = {
    api.processCitationsFromString(text).toList
  }

  def getPoistionFromElement(position:List[Position]) : List[Int] = {
    val itr = position.iterator
    val buf = ListBuffer.empty[Int]
    while (itr.hasNext) {
      val elm = itr.next()
      buf += elm.getX
      buf += elm.getY
    }
    buf.toList
  }

  def processToJson(text:String) : JsValue = {
    if (text.isEmpty) return null

    // Use all chrome servers metamap process
    /**
    val randomNumber = scala.util.Random.nextInt(7)
    val hostAddr:String = if (randomNumber == 0 ) {
      "chrome000"
    } else {
      "chrome01"+randomNumber.toString
    }
    println(hostAddr)
      */
    //api.setHost(hostAddr)

    val inputList:Result = api.processCitationsFromString(text).get(0)



    implicit val termInfoWrites = new Writes[TermInfo] {
      def writes(termInfo:TermInfo) = Json.obj(
        "utteranceText"->termInfo.utternaceText,
        "positionStart"->termInfo.positionStart,
        "positionEnd"->termInfo.positionEnd
      )
    }

    //
    implicit val termMappingInfoWrites = new Writes[TermMappingInfo] {
      def writes(termMappingInfo: TermMappingInfo) = Json.obj(
        "mappingType" -> termMappingInfo.mappingType,
        "mappingScore" -> termMappingInfo.mappingScore,
        "conceptID" -> termMappingInfo.conceptID,
        "conceptName" -> termMappingInfo.conceptName,
        "prefferedName" -> termMappingInfo.prefferedName,
        "matchedWords" -> termMappingInfo.matchedWords,
        "semanticTypes" -> termMappingInfo.semanticTypes,
        "isHead" -> termMappingInfo.isHead,
        "isOvermatched" -> termMappingInfo.isOvermatched,
        "sources" -> termMappingInfo.sources,
        "position" -> termMappingInfo.position
      )
    }


    implicit val termMappingResultWrites = new Writes[TermMappingResult] {
      def writes(termMappingResult: TermMappingResult) = Json.obj(
        "termInfo" -> termMappingResult.termInfo,
        "termMappingInfo" -> termMappingResult.termMappingInfo
      )
    }


    implicit val mappingResultWrites = new Writes[MappingResult] {
      def writes(mappingResult: MappingResult) = Json.obj(
        "metamapResult" -> mappingResult.metamapResult
      )
    }


    val utList = inputList.getUtteranceList
    val mappingResults = if (utList.size() > 0) {

      val termMappingResult = ListBuffer.empty[TermMappingResult]
      val termMappingBuf = ListBuffer.empty[TermMappingInfo]


      val itr = utList.iterator()

      while (itr.hasNext) {
        val element = itr.next()
        val termInfo:TermInfo = TermInfo(element.getId,element.getString,element.getPosition.getX,
          element.getPosition.getY)


        // Start Element Iteration
        val pcm = element.getPCMList
        val pcm_itr = pcm.iterator()
        while(pcm_itr.hasNext) {
          val elm = pcm_itr.next()
          // Candidates
          val candidates = elm.getCandidateList
          if (candidates.size() > 0) {
            val candidates_itr = candidates.iterator()
            while(candidates_itr.hasNext) {
              val c = candidates_itr.next()
              val positionInfo = getPoistionFromElement(c.getPositionalInfo.toList)
              termMappingBuf += TermMappingInfo("candidate",
                c.getScore,c.getConceptId,c.getConceptName,c.getPreferredName,
                c.getMatchedWords.toList,c.getSemanticTypes.toList,
                c.isHead,c.isOvermatch, c.getSources.toList,positionInfo
              )
            }

          }

          // Mapped Results
          val mappedResults = elm.getMappingList
          if (mappedResults.size() > 0) {
            val mappedResults_itr = mappedResults.iterator()
            while(mappedResults_itr.hasNext) {
              val m = mappedResults_itr.next()
              val mEv = m.getEvList
              if (mEv.size() > 0) {
                val mEv_itr = mEv.iterator()
                while(mEv_itr.hasNext) {
                  val mm = mEv_itr.next()
                  val positionInfo = getPoistionFromElement(mm.getPositionalInfo.toList)
                  termMappingBuf += TermMappingInfo("mapping",
                    mm.getScore,mm.getConceptId,mm.getConceptName,mm.getPreferredName,
                    mm.getMatchedWords.toList,mm.getSemanticTypes.toList,
                    mm.isHead,mm.isOvermatch,mm.getSources.toList,positionInfo)
                }
              }
            }
          }

        }
        // End Element iteration
        termMappingResult += TermMappingResult(termInfo,termMappingBuf)
      }
      MappingResult(termMappingResult)
    } else {
      null
    }

    api.disconnect()
    Json.toJson(mappingResults)

  }

  def getCUIList(queryString:String): java.util.List[String] = {
    val jsonResult = processToJson(queryString)
    val cuiJSValue = jsonResult \\ "conceptID"
    val cuiSet = (cuiJSValue mkString ",").replace(""""""","")
    val cuiList = cuiSet.split(",")
    seqAsJavaList(cuiList.toList)

  }

  def getNERJSONResult(queryString:String) : NERResult = {
    val jsonResult = processToJson(queryString)
    val nerResult:NERResult = new NERResult(jsonResult.toString())
    nerResult
  }


  def main(args: Array[String]): Unit = {

    //println("Processing Start")
    //val resultJsonString  = processToJson("Magnetic resonance imaging (MRI) provides detailed insights into soft tissue characteristics and this technique has particular value for imaging patients with acute myocardial infarction (MI) Recent advances in MRI have the potential to reveal new insights into the evolution and functional significance of myocardial injury and repair. Here, we will study at least 300 consecutive patients with acute ST elevation MI (STEMI) and focus on oedema, scar and bleeding in the heart using MRI in patients managed by emergency percutaneous coronary intervention (PCI). Cardiac MRI scans will be performed at 1.5 Tesla (MAGNETOM, Siemens Healthcare). MRI will be used to assess initial heart function and injury. Myocardial salvage and haemorrhage are prioritised outcomes. Novel MRI methods will also be used to quantify the extent of myocardial jeopardy representing the initial area-at-risk (AAR), and the nature of this injury (strain, haemorrhage). The MRI methods will include T1, T2 and T2* relaxometry (mapping). Secondly, we will assess coronary artery disease severity by angiography and coronary artery function at the time of the heart attack treatment using a pressure-sensitive coronary guidewire (St Jude Medical). This wire can be used instead of the usual coronary wire and can provide information on heart injury, which can be linked in turn to the MRI findings. All of this information will be linked with health outcomes in the longer term. We hypothesise that myocardial salvage, oedema, haemorrhage, and strain as revealed by MRI, have functional and prognostic significance. In all patients MRI will be performed at baseline (~day 2) and again at 6 months. In a subgroup of 30 patients, MRI will be performed on days <12 hours, and days 2, 7-10 days and 6 months post-MI. A blood and urine sample and quality of life will be obtained at baseline and at 6 months post-MI. Clinical outcomes (e.g. rehospitalisation, death) will be assessed at the end of the study (minimum 1 year) and again during longer term follow-up (minimum 3 years, maximum 20 years) by electronic linkage through central National Health Service (NHS) and government health records in order to determine the long-term prognostic significance of our initial observations with angiography, MRI and the pressure wire. The main statistical analyses will be conducted by an independent trials unit statistician.")
    val resultJsonString  = processToJson("Clinical trial of polyphenol extract, Seapolynol, isolated from Ecklonia Cava to verify its safety and positive effects on blood sugar level after the meal")
    println(Json.prettyPrint(resultJsonString))
    //println("Processing End")
    //val resultJsonString = processToJson("Adamantinoma")
    //println(Json.prettyPrint(resultJsonString))
    //val cui = resultJsonString \\ "conceptID"
    //println(cui)
    //val cuiColumn = (cui mkString "|").replace(""""""","")
    //println(cuiColumn)
    /*
    println(File.getCurrentPath())
    import scala.io.Source



    val filenames = "../corus-data/rawData/1.0/validation/filenames.csv"
    val result =
    for (line <- Source.fromFile(filenames).getLines()) {
      //val row = line.split(",")
      //val string = row(1).split("_")(0)
      val searchWord = line.replace(""""""","").trim
      val resultJsonString = processToJson(searchWord)
      val cui = resultJsonString \\ "conceptID"
      val cuiColumn = (cui mkString "/").replace(""""""","")
      println(searchWord+","+cuiColumn)
      File.writeStringToFile(searchWord+"|"+cuiColumn+"\n","../corus-data/rawData/1.0/validation/filenamesWithCUI.csv")
    }*/

    /*
    println(processToJson("Myocardial Infarction"))
    val test = processToJson("Myocardial Infarction")
    val cui = test \\ "conceptID"
    println(cui)
    val cuiColumn = (cui mkString "|").replace(""""""","")
    println(cuiColumn)
  */

    // Test Code for Get COUS

  }

}

