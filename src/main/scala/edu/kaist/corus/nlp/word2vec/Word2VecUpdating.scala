package edu.kaist.corus.nlp.word2vec

import org.deeplearning4j.models.embeddings.inmemory.InMemoryLookupTable
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer
import org.deeplearning4j.models.word2vec.VocabWord
import org.deeplearning4j.models.word2vec.wordstore.inmemory.InMemoryLookupCache
import org.deeplearning4j.text.sentenceiterator.BasicLineIterator
import org.deeplearning4j.text.tokenization.tokenizer.preprocessor.CommonPreprocessor
import org.deeplearning4j.text.tokenization.tokenizerfactory.DefaultTokenizerFactory
import org.nd4j.linalg.io.ClassPathResource
import org.slf4j.LoggerFactory

/**
  * Created by Junseok Park on 2017-05-18.
  * https://github.com/deeplearning4j/dl4j-examples/blob/master/dl4j-examples/src/main/java/org/deeplearning4j/examples/nlp/word2vec/Word2VecUptrainingExample.java
  */
object Word2VecUpdating {

  private val log = LoggerFactory.getLogger(Word2VecUpdating.getClass)

  def main(args: Array[String]): Unit = {
    /*
        Initial model training phase
     */

    val filePath = new ClassPathResource("raw_sentences.txt").getFile.getAbsolutePath

    log.info("Load & Vectorize Sentences....");
    // Strip white space before and after for each line
    val iter = new BasicLineIterator(filePath)
    // Split on white spaces in the line to get words
    val t = new DefaultTokenizerFactory
    t.setTokenPreProcessor(new CommonPreprocessor)

    // manual creation of VocabCache and WeightLookupTable usually isn't necessary
    // but in this case we'll need them
    val cache = new InMemoryLookupCache()
    val table = new InMemoryLookupTable.Builder[VocabWord]
      .vectorLength(100)
      .useAdaGrad(false) // AdaGrad
      .cache(cache)
      .lr(0.025f).build() // lr

    log.info("Building model....")
    val vec = new org.deeplearning4j.models.word2vec.Word2Vec.Builder()
      .minWordFrequency(5) // minWordFrequency
      .iterations(1) // iteration
      .epochs(1) // epoch
      .layerSize(100) // layerSize
      .seed(42) // seed
      .windowSize(5) // windowSize
      .iterate(iter)
      .tokenizerFactory(t)
      .lookupTable(table)
      .vocabCache(cache)
      .build()

    log.info("Fitting Word2Vec model....")
    vec.fit()

    var lst = vec.wordsNearest("day",10)
    log.info("Closest words to 'day' on 1st run: " + lst)

    /* at this moment we're supposed to have model built, and it can be saved for future use.
    * */
    WordVectorSerializer.writeFullModel(vec, "pathToSaveModel.txt")

    /*
        Let's assume that some time passed, and now we have new corpus to be used to weights update.
        Instead of building new model over joint corpus, we can use weights update mode.
     */
    val word2Vec = WordVectorSerializer.loadFullModel("pathToSaveModel.txt")

    /*
        PLEASE NOTE: after model is restored, it's still required to set SentenceIterator and TokenizerFactory, if you're going to train this model
     */
    val iterator = new BasicLineIterator(filePath)
    val tokenizerFactory = new DefaultTokenizerFactory
    tokenizerFactory.setTokenPreProcessor(new CommonPreprocessor)

    word2Vec.setTokenizerFactory(tokenizerFactory)
    word2Vec.setSentenceIterator(iterator)

    log.info("Word2vec uptraining...")

    word2Vec.fit()

    lst = word2Vec.wordsNearest("day",10)
    log.info("Closest word to 'day on 2nd run: " +lst)

    /* Model can be saved for future use now */

  }

}
