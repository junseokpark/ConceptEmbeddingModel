package study.lstm

// original source code from
// https://github.com/deeplearning4j/dl4j-examples/blob/master/dl4j-examples/src/main/java/org/deeplearning4j/examples/recurrent/regression/SingleTimestepRegressionExample.java

import java.io.File
import javax.swing.{JFrame, JPanel, WindowConstants}

import org.datavec.api.records.reader.SequenceRecordReader
import org.datavec.api.records.reader.impl.csv.CSVSequenceRecordReader
import org.datavec.api.split.NumberedFileInputSplit
import org.deeplearning4j.datasets.datavec.SequenceRecordReaderDataSetIterator
import org.deeplearning4j.eval.RegressionEvaluation
import org.deeplearning4j.nn.api.OptimizationAlgorithm
import org.deeplearning4j.nn.conf.{MultiLayerConfiguration, NeuralNetConfiguration}
import org.deeplearning4j.nn.weights.WeightInit
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator
import org.nd4j.linalg.io.ClassPathResource
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.nd4j.linalg.dataset.DataSet
import org.nd4j.linalg.dataset.api.preprocessor.NormalizerMinMaxScaler
import org.deeplearning4j.nn.conf.Updater
import org.deeplearning4j.nn.conf.layers.{GravesLSTM, RnnOutputLayer}
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork
import org.deeplearning4j.optimize.listeners.ScoreIterationListener
import org.jfree.chart.{ChartFactory, ChartPanel, JFreeChart}
import org.jfree.chart.plot.{PlotOrientation, XYPlot}
import org.jfree.data.xy.XYSeries
import org.jfree.data.xy.XYSeriesCollection
import org.jfree.chart.axis.NumberAxis
import org.jfree.ui.RefineryUtilities
import org.nd4j.linalg.activations.Activation
import org.nd4j.linalg.lossfunctions.LossFunctions
import org.nd4j.linalg.api.ndarray.INDArray


object SingleTimestepRegressionExample {

  private final val LOGGER:Logger = LoggerFactory.getLogger(SingleTimestepRegressionExample.getClass)

  def main(args: Array[String]): Unit = {
    //val baseDir: File = new ClassPathResource("files/dl4j-examples/rnnRegression").getFile
    val baseDir:File = new File("files/dl4j-examples/rnnRegression").getCanonicalFile

    val miniBatchSize: Int = 32

    // Load the training data
    val trainReader: SequenceRecordReader = new CSVSequenceRecordReader(0, ";")
    trainReader.initialize(new NumberedFileInputSplit(baseDir + "/passengers_train_%d.csv", 0, 0))

    // For regression, numPossibleLables is not used. Setting it to -1 here
    val trainIter: DataSetIterator = new SequenceRecordReaderDataSetIterator(trainReader, miniBatchSize, -1, 1, true)

    val testReader: SequenceRecordReader = new CSVSequenceRecordReader(0, ";")
    testReader.initialize(new NumberedFileInputSplit(baseDir + "/passengers_test_%d.csv", 0, 0))
    val testIter: DataSetIterator = new SequenceRecordReaderDataSetIterator(testReader, miniBatchSize, -1, 1, true)

    //Create data set from iterator here since we only have a single data set
    val trainData: DataSet = trainIter.next()
    val testData: DataSet = testIter.next()


    //Normalize data, including labels (fitLabel = true)
    val normalizer: NormalizerMinMaxScaler = new NormalizerMinMaxScaler(0, 1)
    normalizer.fitLabel(true)
    normalizer.fit(trainData) //collect training data statistics

    normalizer.transform(trainData)
    normalizer.transform(testData)

    //Configure the network
    val conf: MultiLayerConfiguration = new NeuralNetConfiguration.Builder()
      .seed(140)
      .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
      .iterations(1)
      .weightInit(WeightInit.XAVIER)
      .updater(Updater.NESTEROVS)
      .learningRate(0.0015)
      .list()
      .layer(0, new GravesLSTM.Builder().activation(Activation.TANH).nIn(1).nOut(10)
        .build())
      .layer(1, new RnnOutputLayer.Builder(LossFunctions.LossFunction.MSE)
        .activation(Activation.IDENTITY).nIn(10).nOut(1).build())
      .build()

    val net: MultiLayerNetwork = new MultiLayerNetwork(conf)
    net.init()

    net.setListeners(new ScoreIterationListener(20))

    // Train the network, evaluating the test set performance at each epoch
    val nEpochs = 300

    for (i <- 0 to nEpochs) {
      net.fit(trainData)
      LOGGER.info("Epoch" + i + " complete. Time series evaluation:")

      // Run regression evaluation on our single column input
      val evaluation: RegressionEvaluation = new RegressionEvaluation(1)
      val features: INDArray = testData.getFeatureMatrix()

      val lables: INDArray = testData.getLabels()
      val predicted: INDArray = net.output(features, false)

      evaluation.evalTimeSeries(lables, predicted)

      // Just do out here since the logger will shift the sift the columns of the stats
      println(evaluation.stats())
    }

    net.rnnTimeStep(trainData.getFeatureMatrix)
    val predicted:INDArray = net.rnnTimeStep(testData.getFeatureMatrix())

    normalizer.revert(trainData)
    normalizer.revert(testData)
    normalizer.revertLabels(predicted)

    val trainFeatures:INDArray = trainData.getFeatures
    val testFeatures:INDArray = testData.getFeatures

    //Create plot with out data
    val c:XYSeriesCollection = new XYSeriesCollection()
    createSeries(c,trainFeatures,0,"Train data")
    createSeries(c,testFeatures,99,"Actual test data")
    createSeries(c,predicted,100,"Predicted test data")

    plotDataset(c)

    LOGGER.info("----- Example Complete -----")

  }

    def createSeries(seriesCollection:XYSeriesCollection,data:INDArray,offset:Int,name:String) : Unit = {
      val nRows:Int = data.shape().apply(2)
      val series:XYSeries = new XYSeries(name)
      for (i <- 0 to nRows) {
        series.add(i + offset, data.getDouble(i))
      }
      seriesCollection.addSeries(series)

    }

    def plotDataset(c:XYSeriesCollection) : Unit = {
      val title = "Regression example"
      val xAxisLabel = "Timestep"
      val yAxisLabel = "Number of passengers"
      val orientation:PlotOrientation = PlotOrientation.VERTICAL
      val legend = true
      val tooltips = false
      val urls = false

      val chart:JFreeChart = ChartFactory.createXYLineChart(title,xAxisLabel,yAxisLabel,c,orientation,legend,tooltips,urls)

      // get a reference to the plot for further customization
      val plot:XYPlot = chart.getXYPlot()

      val rangeAxis:NumberAxis = plot.getRangeAxis().asInstanceOf[NumberAxis]
      rangeAxis.setAutoRange(true)

      val panel:JPanel = new ChartPanel(chart)

      val f:JFrame = new JFrame()
      f.add(panel)
      f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE)
      f.pack()
      f.setTitle("Training Data")

      RefineryUtilities.centerFrameOnScreen(f)
      f.setVisible(true)

    }

}
