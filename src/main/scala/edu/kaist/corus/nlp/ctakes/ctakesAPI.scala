package edu.kaist.corus.nlp.ctakes

import org.apache.ctakes.typesystem.`type`.refsem.UmlsConcept
import org.apache.ctakes.typesystem.`type`.syntax.BaseToken
import org.apache.ctakes.typesystem.`type`.textsem.IdentifiedAnnotation
import org.apache.uima.UIMAFramework
import org.apache.uima.analysis_engine.AnalysisEngine
import org.apache.uima.fit.util.JCasUtil
import org.apache.uima.jcas.JCas
import org.apache.uima.resource.ResourceSpecifier
import org.apache.uima.util.XMLInputSource
import play.api.libs.json.{JsValue, Json, Writes}

import scala.collection.mutable.ListBuffer

/**
  * Created by Junseok Park on 2017-04-09.
  */
object CtakesAPI {


  def getAnalysisSpecifier(desc:String) : ResourceSpecifier = {
    val in = new XMLInputSource(desc)
    UIMAFramework.getXMLParser.parseResourceSpecifier(in)
  }

  def getAnalysisEngine(specifier: ResourceSpecifier) : AnalysisEngine = {
    UIMAFramework.produceAnalysisEngine(specifier)
  }

  def casToJson(jcas: JCas) : JsValue = {
    val iter = JCasUtil.select(jcas,classOf[IdentifiedAnnotation]).iterator()

    case class TermMappingInfo(conceptID:String,
                               matchedWords:String,
                               position:List[Int],
                               polarity:Int,
                               mappingType:String,
                               mappingTypeID:Int,
                               segmentID:String,
                               uncertainty:Int,
                               subject:String,
                               discoveryTechnique:Int,
                               confidence:Float,
                               conditional:Boolean,
                               generic:Boolean,
                               historyOf:Int)
    implicit val termMappinInfoWrites = new Writes[TermMappingInfo] {
      def writes(termMappingInfo:TermMappingInfo) = Json.obj(
        "conceptID" -> termMappingInfo.conceptID,
        "matchedWords" -> termMappingInfo.matchedWords,
        "position" -> termMappingInfo.position,
        "polarity" -> termMappingInfo.polarity,
        "mappingType" -> termMappingInfo.mappingType,
        "mappingTypeID" -> termMappingInfo.mappingTypeID,
        "segmentID" -> termMappingInfo.segmentID,
        "uncertainty" -> termMappingInfo.uncertainty,
        "subject" -> termMappingInfo.subject,
        "discoveryTechnique" -> termMappingInfo.discoveryTechnique,
        "confidence" -> termMappingInfo.confidence,
        "conditional" -> termMappingInfo.conditional,
        "generic" -> termMappingInfo.generic,
        "historyOf" -> termMappingInfo.historyOf
      )
    }

    case class MappingResult(cTakesResult:Seq[TermMappingInfo])
    implicit val mappingResultWrites = new Writes[MappingResult] {
      def writes(mappingResult:MappingResult) = Json.obj(
        "cTakesResult" -> mappingResult.cTakesResult
      )
    }

    val termMappingInfoBuf = ListBuffer.empty[TermMappingInfo]

    while(iter.hasNext)
    {
      val entity = iter.next()

      val mentions = entity.getOntologyConceptArr
      var i = 0

      if (mentions!=null && mentions.size > 0) {
        val uniqueCuis = scala.collection.mutable.Set[String]()
        for (i <- i to mentions.size -1) {
          if(mentions.get(i)!=null && mentions.get(i).isInstanceOf[UmlsConcept]){
            val concept = mentions.get(i).asInstanceOf[UmlsConcept]
            uniqueCuis += concept.getCui
          }
        }
        termMappingInfoBuf += TermMappingInfo(uniqueCuis.head,entity.getCoveredText,List(entity.getBegin,entity.getEnd),
          entity.getPolarity,entity.getType.getShortName,entity.getTypeID,entity.getSegmentID,
          entity.getUncertainty,entity.getSubject,entity.getDiscoveryTechnique,
          entity.getConfidence,entity.getConditional,entity.getGeneric,
          entity.getHistoryOf)
      }

    }

    val mappingResult = MappingResult(termMappingInfoBuf)

    //return the iterator.
    JCasUtil.select(jcas,classOf[BaseToken]).iterator()
    jcas.reset()
    Json.toJson(mappingResult)

  }

  def analysisResultToJson(ae:AnalysisEngine, jcas:JCas, text:String): JsValue = {
    //val jcas = ae.newJCas()
    //jcas.reset()
    //jcas.setDocumentText(text)
    //ae.process(jcas)
    casToJson(jcas)
  }

  def processToJson(text:String) :JsValue = {
    val desc = "externalResources/cTakesResources/desc/ctakes-clinical-pipeline/desc/analysis_engine/AggregatePlaintextUMLSProcessor.xml"

    val specifier = getAnalysisSpecifier(desc)
    val ae = getAnalysisEngine(specifier)


    val jcas = ae.newJCas()

    jcas.reset()
    jcas.setDocumentText(text.replaceAll("[\n\r\t]", ""))
    ae.process(jcas)
    casToJson(jcas)

  }

  def main(args: Array[String]): Unit = {
    println(processToJson("cancer"))

  }

}
