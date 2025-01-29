package edu.kaist.corus.nlp.word2vec

import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer
import org.deeplearning4j.text.sentenceiterator.BasicLineIterator
import org.deeplearning4j.text.tokenization.tokenizer.preprocessor.CommonPreprocessor
import org.deeplearning4j.text.tokenization.tokenizerfactory.DefaultTokenizerFactory
import org.nd4j.linalg.io.ClassPathResource
import org.slf4j.LoggerFactory

/**
  * Created by Junseok Park on 2017-05-18.
  * original code : https://github.com/deeplearning4j/dl4j-examples/blob/master/dl4j-examples/src/main/java/org/deeplearning4j/examples/nlp/word2vec/Word2VecRawTextExample.java
  */
object Word2Vec {
  private val log = LoggerFactory.getLogger(Word2Vec.getClass)

  def main(args: Array[String]): Unit = {
    // Gets Path to Text file
    val filePath = new ClassPathResource("raw_sentences.txt").getFile().getAbsolutePath()

    log.info("Load & Vectorize Sentences...")
    // Strip white space before and after for each line
    val iter = new BasicLineIterator(filePath)
    // split on white space in the line to get words
    val t = new DefaultTokenizerFactory

    /**
      * CommonPreprocessor will apply the following regex to each token: [\d\.:,"'\(\)\[\]|/?!;]+
      * So, effectively all numbers, punctuation symbols and some special symbols are stripped off.
      * Additionally it forces lower case for all tokens.
      * */


    t.setTokenPreProcessor(new CommonPreprocessor)
    log.info("Building model......")
    val word2Vec = new org.deeplearning4j.models.word2vec.Word2Vec.Builder()
      .minWordFrequency(5)
      .iterations(1)
      .layerSize(100)
      .seed(42)
      .windowSize(5)
      .iterate(iter)
      .tokenizerFactory(t)
      .build()

    log.info("Fitting Word2Vec model...")
    word2Vec.fit()


    log.info("Writing word vectors to text file.....")


    // Write word vectors to file
    WordVectorSerializer.writeWordVectors(word2Vec,"result")

    // Prints out the closest 10 words to "day". An example on what to do with these Word Vectors.
    log.info("Closest Words:")
    val lst = word2Vec.wordsNearest("day",10)
    println("10 Words closes to 'day: "+lst)


  }


}
