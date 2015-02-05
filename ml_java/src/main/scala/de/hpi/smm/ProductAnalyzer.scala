package de.hpi.smm

import java.io.File

import com.blog_intelligence.nto.Document
import de.hpi.smm.data_reader.DataReader
import weka.classifiers.{Evaluation, Classifier}
import weka.core.{DenseInstance, Utils, Instances, Attribute}
import scala.collection.JavaConverters._

import scala.collection.mutable

class ProductAnalyzer() {

	val dataReader = new DataReader(
		new File("../n2o_data/linked_in_posts.csv"),
		new File("../n2o_data/brochures.csv"))

	var wordCountWithTfIdf = mutable.Map[String, mutable.Map[String, Double]]()

	var featureAttributes: java.util.ArrayList[Attribute] = null
	var featureWords: Map[String, Int] = null
	var classAttr: Attribute = null

	private var classifier: Classifier = null
	private var evaluation: Evaluation = null

	private var trainInstances: Instances = null
	private var testInstances: Instances = null

	private var brochures = mutable.ArrayBuffer[Document]()

	init()

	private def init() = {
		val wordCount = mutable.Map[String, mutable.Map[String, Int]]()
		val documentCount = mutable.Map[String, Int]().withDefaultValue(0)
		var N = 0

		dataReader.readBrochuresLinewise(List("en")) { doc =>
			brochures += doc
		}
		println(s"Brochure size before ${brochures.size}")
		brochures = brochures.flatMap { doc =>
			var i = 0
			doc.sentences.grouped(6).map { sentences =>
				i += 1
				Document(s"${doc.id}-$i", "", sentences.mkString(" "), sentences, doc.documentClass)
			}
		}
		println(s"Brochure size after ${brochures.size}")
		brochures.foreach { doc =>
			val docClass = doc.documentClass

			if (!wordCount.contains(docClass))
				wordCount(docClass) = mutable.Map[String, Int]().withDefaultValue(0)
			doc.textTokens.foreach { word =>
				wordCount(docClass)(word) += 1
			}
			doc.textTokens.toSet[String].foreach { word =>
				documentCount(word) += 1
			}
			N += 1
		}


		wordCountWithTfIdf = wordCount.map { case (className, counts) =>
			(className, counts.map { case (word, count) =>
				(word, count.toDouble * Math.log(N.toDouble / documentCount(word).toDouble))
			})
		}

		featureWords = determineFeatures(wordCountWithTfIdf).zipWithIndex.toMap
		println(featureWords)
		val classes = new java.util.ArrayList[String](wordCountWithTfIdf.keySet.asJava)
		classes.add("None")
		classAttr = new Attribute("@@class@@", classes)
		println(classAttr)

		featureAttributes = new java.util.ArrayList[Attribute](featureWords.keys.map(new Attribute(_)).asJavaCollection)
		featureAttributes.add(classAttr)
	}

	def setClassifier(classifier: Classifier): Unit = {
		this.classifier = classifier
	}

	private def determineFeatures(wordCounts: mutable.Map[String, mutable.Map[String, Double]]): Array[String] = {
		var result = mutable.Set[String]()
		wordCounts.foreach { case (className, counts) =>
			counts.toList.sortBy(-_._2).take(10).foreach { case (word, _) =>
				result += word
			}
		}
		result.toArray
	}

	private def constructFeatureValues(doc: Document): Array[Double] = {
		val result = new Array[Double](featureWords.size + 1)
		doc.textTokens.foreach { word =>
			if(featureWords.contains(word))
				result(featureWords(word)) += 1.0
		}
		result(result.size - 1) = classAttr.indexOfValue(doc.documentClass)
		result
	}

	private def normalize(features: Array[Double]): Array[Double] = {
		return features
		val lastIndex = features.size - 1
		val instanceClass = features(lastIndex)
		features(lastIndex) = 0.0
		if (features.sum == 0.0) {
			features(lastIndex) = instanceClass
			return features
		}
		Utils.normalize(features)
		features(lastIndex) = instanceClass
		features
	}

	def buildTrainInstances() : Unit = {
		trainInstances = new Instances("train", featureAttributes, featureAttributes.size())
		trainInstances.setClassIndex(featureAttributes.size() - 1)
		brochures.foreach { doc =>
			val features = constructFeatureValues(doc)
			trainInstances.add(new DenseInstance(1.0, normalize(features)))
		}
		evaluation = new Evaluation(trainInstances)
	}

	def buildClassifier(): Unit = {
		classifier.buildClassifier(trainInstances)
	}

	private def readTestInstances(): Unit = {

		testInstances = new Instances("test", featureAttributes, featureAttributes.size())
		testInstances.setClassIndex(featureAttributes.size() - 1)
		dataReader.readPostsLinewise { doc =>
			val features = constructFeatureValues(doc)
			testInstances.add(new DenseInstance(1.0, normalize(features)))
		}("category")
	}

	def validate(): Unit = {
		buildTrainInstances()
		readTestInstances()
		evaluation.evaluateModel(classifier, testInstances)

	}

	def printEvaluation() : Unit  = {
		println(evaluation.toSummaryString(f"%nResults%n======%n", false))
		println(evaluation.toMatrixString)
	}

	def distributionForInstance(doc: Document): Array[Double] = {
		val instance = new DenseInstance(1.0, constructFeatureValues(doc))
		val dummyInstances = new Instances("bla", featureAttributes, featureAttributes.size())
		dummyInstances.setClassIndex(featureAttributes.size() - 1)
		instance.setDataset(dummyInstances)
		classifier.distributionForInstance(instance)
	}

}
