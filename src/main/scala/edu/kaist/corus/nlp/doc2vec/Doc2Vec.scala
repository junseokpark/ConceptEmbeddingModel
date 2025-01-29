package edu.kaist.corus.nlp.doc2vec

import edu.kaist.corus.core.utils.File
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer
import org.deeplearning4j.models.paragraphvectors.ParagraphVectors
import org.deeplearning4j.models.word2vec.VocabWord
import org.deeplearning4j.models.word2vec.wordstore.inmemory.AbstractCache
import org.deeplearning4j.plot.BarnesHutTsne
import org.deeplearning4j.text.documentiterator.{LabelAwareIterator, LabelsSource}
import org.deeplearning4j.text.sentenceiterator.BasicLineIterator
import org.deeplearning4j.text.tokenization.tokenizer.preprocessor.CommonPreprocessor
import org.deeplearning4j.text.tokenization.tokenizerfactory.DefaultTokenizerFactory
//import org.nd4j.jita.conf.CudaEnvironment

import scala.collection.mutable.ListBuffer
//import org.deeplearning4j.ui.api.UIServer
//import org.deeplearning4j.ui.stats.StatsListener
//import org.deeplearning4j.ui.storage.InMemoryStatsStorage
import org.nd4j.linalg.io.ClassPathResource
import org.slf4j.LoggerFactory

import org.apache.commons.math3.stat.correlation.SpearmansCorrelation

/**
  * Created by Junseok Park on 2017-05-18.
  * https://github.com/deeplearning4j/dl4j-examples/blob/master/dl4j-examples/src/main/java/org/deeplearning4j/examples/nlp/paragraphvectors/ParagraphVectorsTextExample.java
  *
  * /**
  * This is example code for dl4j ParagraphVectors implementation. In this example we build distributed representation of all sentences present in training corpus.
  * However, you still use it for training on labelled documents, using sets of LabelledDocument and LabelAwareIterator implementation.
  *
  * *************************************************************************************************
  * PLEASE NOTE: THIS EXAMPLE REQUIRES DL4J/ND4J VERSIONS >= rc3.8 TO COMPILE SUCCESSFULLY
  * *************************************************************************************************
  *
  * @author raver119@gmail.com
  */
  *
  */
object Doc2Vec {
  private final val log = LoggerFactory.getLogger(Doc2Vec.getClass)


  def learningModel (paragraphFileSource:String,labelFileSource:String) : ParagraphVectors = {
    val filePath = new ClassPathResource(paragraphFileSource).getFile.getAbsolutePath

    //val uiServer = UIServer.getInstance()
    //val statsStorage = new InMemoryStatsStorage()
    //uiServer.attach(statsStorage)

    log.info("Load & Vectorize document sentences....");
    // Strip white space before and after for each line

    val iter = new BasicLineIterator(filePath)
    val cache = new AbstractCache[VocabWord]()

    val t = new DefaultTokenizerFactory
    t.setTokenPreProcessor(new CommonPreprocessor)

    /*
      if you don't have LabelAwareIterator handy, you can use synchronized labels generator
      it will be used to label each document/sequence/line with it's own label.
      But if you have LabelAwareIterator ready, you can can provide it, for your in-house labels
    */
    val labelPath = new ClassPathResource(labelFileSource).getFile.getAbsolutePath
    val labelFile = scala.io.Source.fromFile(labelPath)
    import collection.JavaConverters._
    val labels = (for (line <- labelFile.getLines()) yield line).toSeq

    //val source = new LabelsSource("DOC_")
    val source = new LabelsSource(labels.asJava)
    labelFile.close()

    val vec = new ParagraphVectors.Builder()
      .batchSize(1000) //  amount of words you process at a time, the number of training examples in one forward/backward pass. The higher the batch size, the more memory space you'll need
      .minWordFrequency(1) // the minimum number of times a word must appear in the corpus
      .iterations(10) // the number of times you allow the net to update its coefficients for on batch of the data. number of passes, each pass using [batch size] number of examples
      .epochs(30) // one epoch = one forward pass and one backward pass of all the training examples
      .layerSize(300) // specifies the number of features in the word/sentence vector. This is qual to the number of dimensions in the featurespace.
      .learningRate(0.025) // the step size for each update of the coefficients, as words are repositioned in the feature space
      .minLearningRate(0.001) // the floor on the learning rate.
      .labelsSource(source) //
      .windowSize(5) //
      .iterate(iter) // the net what batch of the dataset it's training on
      .trainWordVectors(true) //
      .trainElementsRepresentation(true)
      .trainSequencesRepresentation(true)
      .vocabCache(cache)
      .useAdaGrad(true) // Adagrad creates a differnt gradient for each feature
      .tokenizerFactory(t) // fedds it the words from the current batch
      .sampling(0)
      .build()

    vec.fit()

    vec

  }

  def visualizeModel(vec:ParagraphVectors) : Unit = {
    // lvdmaaten.github.io/tsne
    log.info("Plot TSNE......")
    val tsne:BarnesHutTsne = new BarnesHutTsne.Builder()
      .setMaxIter(1000)
      .stopLyingIteration(250)
      .learningRate(500)
      .useAdaGrad(false)
      .theta(0.5)
      .setMomentum(0.5)
      .normalize(true)
      .build()


    //vec.lookupTable().plotVocab(tsne,1,"dsalfkja")
  }

  def testSetOfCaviedesAndCimino() : List[(String,String)] = {
    List (
      ("C0012242","C0014869"),
      ("C0012242","C0033968"),
      ("C0033968","C0039971"),
      ("C0012242","C0039971"),
      ("C0012242","C0039979"),
      ("C0033968","C0039979"),
      ("C0014869","C0033968"),
      ("C0014869","C0039971"),
      ("C0014869","C0039979"),
      ("C0039971","C0039979")
    )
  }


  def testConceptEmbeddingSamples(fileNameWithDir:String,resultFileName:String,c1Idx:Int,c2Idx:Int,vec:ParagraphVectors=null) : Unit = {
    val lines = scala.io.Source.fromFile(fileNameWithDir).getLines()
    var rowNum = 0
    for (line <- lines) {
      val row = line.toString.split(",")

      // Write Result
      if (rowNum == 0)
        File.writeStringToFile("rownum"+","+row(c1Idx)+","+row(c2Idx)+",ce\n",resultFileName)
      else {
        // Test Random Set
        println(row(c1Idx),row(c2Idx))
        val similarity = vec.similarity(row(c1Idx),row(c2Idx))
        log.info(row(c1Idx)+"-"+row(c2Idx)+" similarity" + similarity)
        File.writeStringToFile((rowNum-1)+","+row(c1Idx)+","+row(c2Idx)+","+similarity+"\n",resultFileName)
      }
      rowNum += 1
    }

  }

  def loadResultDataForTestingConceptEmbeddingModel(fileNameWithDir:String,idx:Int) : List[Double] = {
    val lines = scala.io.Source.fromFile(fileNameWithDir).getLines()
    val listBuffer = ListBuffer.empty[Double]
    var rowNum = 0
    for (line <- lines) {
      val row = line.toString.split(",")
      //  Read
      val resultValue = {
        if (row(idx).toDouble.isNaN)
          0
        else
          row(idx).toDouble
      }

      if (rowNum != 0) listBuffer.append(resultValue)
      rowNum += 1
    }
    listBuffer.toList
  }


  def main(args: Array[String]) :Unit = {

    /*
    CudaEnvironment.getInstance().getConfiguration()
      .setMaximumDeviceCacheableLength(1024*1024*1024L)
      .setMaximumDeviceCache(10L * 1024 * 1024 * 1024L)
      .setMaximumHostCacheableLength(1024*1024*1024L)
      .setMaximumHostCache(10L*1024*1024*1024L)
    */

    // Learning Model
    //val vec = learningModel("MRDEF_WIKI_DOC_PATH.csv","MRDEF_WIKI_DOC_LABEL_PATH.csv")
    //println(vec.getWordVector("C0156543"))
    //println(vec.getWordVector("C0000786"))

    // Line 30: C0003164 :A form of pneumoconiosis caused by inhalation of dust that contains both CARBON and crystalline SILICON DIOXIDE. These foreign matters induce fibrous nodule formation in the lung.
    // Line 31: C0003949 :A form of pneumoconiosis caused by inhalation of asbestos fibers which elicit potent inflammatory responses in the parenchyma of the lung. The disease is characterized by interstitial fibrosis of the lung, varying from scattered sites to extensive scarring of the alveolar interstitium.
    //val similarity = vec.similarity("C0156543","C0000786")
    //log.info("C0156543/C0000786 similarity : "+similarity)
    //WordVectorSerializer.writeParagraphVectors(vec,"models/doc2vec/UMLS_DOC_MODEL_20170816_PathSet_epoch30_layer300_batch_1000_iteration_10_allOn.txt")


    // Test Model
    //val vec = WordVectorSerializer.readParagraphVectors("models/doc2vec/UMLS_DOC_MODEL_20170811_PathSet.txt")
    val vec = WordVectorSerializer.readParagraphVectors("models/doc2vec/UMLS_DOC_MODEL_20170815_PathSet_epoch3_layer200_iter_5_on.txt")
    println(vec.getWordVector("C0156543"))
    println(vec.getWordVector("C0000786"))
    val similarity = vec.similarity("C0156543","C0000786")
    log.info("C0156543/C0000786 similarity : " + similarity )

    // Generate Test Set

    // (1) Random Set Test
    val directory = "files/conceptEmbeddingResults/"
    val randomSetDirectory = "randomSet/"
    val testDate = "20180411-000"
    val resultDirectory = "result_"+testDate+"/"

    for (idx <- 1 to 10) {
      val fileNameWithDir  = directory+randomSetDirectory+"randomCUISet_"+idx+".csv"
      val resultFileName = directory+resultDirectory+"randomCUISet_"+idx+"_ours.csv"
      testConceptEmbeddingSamples(fileNameWithDir,resultFileName,1,2,vec)
    }

    // (2) Pedersen's Test
    val fileNameWithDirP = directory+"Pederson_DataSet_WithMayoClinic.csv"
    val resultFileNameP = directory+resultDirectory+"Pederson_DataSet_WithMayoClinic_Scores_"+testDate+"_ours.csv"
    testConceptEmbeddingSamples(fileNameWithDirP,resultFileNameP,1,2,vec)

    // (3) CaviedesAndCimino Test
    val fileNameWithDirC = directory+"Caviedes.csv"
    val resultFileNameC = directory+resultDirectory+"Caviedes_Scores_"+testDate+"_ours.csv"
    testConceptEmbeddingSamples(fileNameWithDirC,resultFileNameC,1,2,vec)

    // (4) HPO Test
    val fileNamewithDirH = "files/conceptEmbeddingResults/random70pair100Set/"+"randomPair70_100Set.csv"
    val resultFileNameH = directory+resultDirectory+"PlosOne_Random_Scores_"+testDate+"_ours.csv"
    testConceptEmbeddingSamples(fileNamewithDirH,resultFileNameH,2,3,vec)

    // (5) HPO Random Test
    val fileNameWithDirR = "files/conceptEmbeddingResults/random70pair100Set/"


    // Test Start
    // (1) Coverage Counts for Random Set
    val vectorRandomResultList = ListBuffer.empty[Double]
    val ceRandomResultList = ListBuffer.empty[Double]
    for (idx <- 1 to 10) {
      val fileNameWithDir  = directory+randomSetDirectory+"randomCUISet_"+idx+"_vector.csv"
      vectorRandomResultList.append(loadResultDataForTestingConceptEmbeddingModel(fileNameWithDir,3).count(_ == 0))
      val fileNameWithDirC = directory+resultDirectory+"randomCUISet_"+idx+"_ours.csv"
      ceRandomResultList.append(loadResultDataForTestingConceptEmbeddingModel(fileNameWithDirC,3).count(_ == 0))
    }

    log.info("vector/concept embedding randomResults: ",vectorRandomResultList.sum,ceRandomResultList.sum)

    // (2) Spearman's rank correlation



    // Test End


    /*
    val testSet = testSetOfCaviedesAndCimino()
    for (value <- testSet) {
      println(value._1)
      val relatedness = vec.similarity(value._1,value._2)
      log.info(value._1+"-"value._2+"similarity : " + similarity)
    }
    */




    // Below is example tests for raw_sentences.txt
    /*
            In training corpus we have few lines that contain pretty close words invloved.
            These sentences should be pretty close to each other in vector space
            line 3721: This is my way .
            line 6348: This is my case .
            line 9836: This is my house .
            line 12493: This is my world .
            line 16393: This is my work .
            this is special sentence, that has nothing common with previous sentences
            line 9853: We now have one .
            Note that docs are indexed from 0
         */


    /*
    println(vec.getWordVector("DOC_9835"))
    println(vec.getWordVector("DOC_12492"))

    val similarity1 = vec.similarity("DOC_9835","DOC_12492")
    log.info("9836/12493  ('This is my house .'/'This is my world .') similarity: " + similarity1)

    // likelihood in this case should be significantly lower
    val similarityX = vec.similarity("DOC_3720", "DOC_9852")
    log.info("3721/9853 ('This is my way .'/'We now have one .') similarity: " + similarityX +
      "(should be significantly lower)")
    */

  }

}

