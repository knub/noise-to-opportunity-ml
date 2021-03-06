package de.hpi.smm

import java.io.File

import com.blog_intelligence.nto.Document
import de.hpi.smm.Constants._
import de.hpi.smm.classification.ProductClassifier
import de.hpi.smm.data_reader.DataReader
import de.hpi.smm.domain.Word
import de.hpi.smm.nlp.NLP
import weka.classifiers.functions.MultilayerPerceptron

import scala.collection.mutable
import scala.util.Random

object ProductClassifierEvaluation {

	val BUILD_RANDOM_POSTS = true
	val INCLUDE_NONE_POSTS = false

	val postsFile = new File(POSTS_PATH)
	val brochuresFile = new File(BROCHURES_PATH)
	val classificationFile = new File(CLASSIFICATION_JSON)
	val stopWordsFile = new File(STOPWORDS_PATH)
	val posModelFile = new File(POSMODEL_PATH)
	val nlp = new NLP(stopWordsFile, posModelFile)
	val dataReader = new DataReader(postsFile, brochuresFile, classificationFile, nlp, includeNone = INCLUDE_NONE_POSTS)

	var posts = mutable.ArrayBuffer[Document]()
	var brochures = mutable.ArrayBuffer[Document]()

	def readData(): Unit = {
		posts = mutable.ArrayBuffer[Document]()
		brochures = mutable.ArrayBuffer[Document]()

		if (dataReader != null) {
			dataReader.readPostsLinewise { post =>
				posts += post
			}("category")

			dataReader.readBrochuresLinewise(List("en")) { brochure =>
				brochures += brochure
			}
		}
	}

	val groupSizes  = List(6)
	val classifiers = List(
//		new Logistic
//		, new SMO()
//		,
		new MultilayerPerceptron()
		//		,
		//		new TheirClassifier
	)
	val binaryFeatures = List(/*false, */true)
	val normalize = List(/*false, */true)

	def main(args: Array[String]): Unit = {
		readData()

		val postsSentenceSet = mutable.Map[String, mutable.Set[Seq[Word]]]()

		posts.foreach { post =>
	val docClass = post.documentClass
			if (!postsSentenceSet.contains(docClass)) {
				postsSentenceSet(docClass) = mutable.Set[Seq[Word]]()
			}
			postsSentenceSet(docClass) ++= post.sentences
		}

		if (BUILD_RANDOM_POSTS)
			buildRandomPosts(postsSentenceSet)

		println(s"Now we have ${posts.size} posts.")

		groupSizes.foreach { groupSize =>
			classifiers.foreach { classifier =>
				binaryFeatures.foreach { useBinaryFeature =>
					normalize.foreach { normalizeFeatures =>
						println(f"Classifier: ${classifier.getClass.getSimpleName}, GroupSize: $groupSize, binaryFeature: $useBinaryFeature, normalize: $normalizeFeatures")

						val analyzer = new ProductClassifier(brochures, nlp, groupSize, classifier, useBinaryFeature, normalizeFeatures, INCLUDE_NONE_POSTS)
						analyzer.buildClassifier()

						val productEvaluation = analyzer.validate(posts)
//						println(productEvaluation.toSummaryString("", false))
						println(s"${classifier.getClass.getSimpleName}\t$groupSize\t${productEvaluation.pctCorrect}")
//						println(productEvaluation.toMatrixString)
					}
				}
			}
		}
	}

	def buildRandomPosts(sentenceSet: mutable.Map[String, mutable.Set[Seq[Word]]]) {
		val r = new Random(44)
		val NUM_DOCS = 30
		val NUM_SENTENCES = 2

		posts.clear()
		sentenceSet.foreach { case (docClass, sentences) =>
			println(s"$docClass ${sentences.size} ${sentences.flatten.size}")
			val sentenceList = sentences.toList
			for (i <- 1 to NUM_DOCS) {
				val newSentences = r.shuffle(sentenceList).take(NUM_SENTENCES + (r.nextInt(4) + 1) / 4)
				posts += Document(r.nextInt().toString, "", newSentences.flatten.mkString(" "), newSentences, docClass)
			}
		}
	}
}
