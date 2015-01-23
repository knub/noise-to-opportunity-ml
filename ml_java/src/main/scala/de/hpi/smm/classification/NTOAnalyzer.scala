package de.hpi.smm.classification


import de.hpi.smm.FeatureExtractorBuilder
import scala.collection.JavaConverters._

case class ClassificationOutput(prob: Double, relevantFeatures: Array[Array[Any]] = Array())
case class Classification(cls: String, classificationOutput: ClassificationOutput)

class NTOAnalyzer(featureExtractorBuilder: FeatureExtractorBuilder) {

	var demandClassifier: Classifier = null

	def trainDemand(): Unit = {
		demandClassifier = new Classifier("demand",
			featureExtractorBuilder.posts,
			featureExtractorBuilder.buildForDemand())
	}

	val classNames = List("CRM", "ECOM", "HCM", "LVM", "None")

	val productClassifier = new GroupedProductClassifier(
		featureExtractorBuilder.brochures,
		featureExtractorBuilder.postForCategory,
		classNames)

//	val productClassifier = new MultiProductClassifier(
//		featureExtractorBuilder.brochures,
//		featureExtractorBuilder.postForCategory,
//		classNames,
//		featureExtractorBuilder.dataReader)

	val CRMClassifier = new ProductClassifier("CRM",
		featureExtractorBuilder.brochures)

	val ECOMClassifier = new ProductClassifier("ECOM",
		featureExtractorBuilder.brochures)

	val HCMClassifier = new ProductClassifier("HCM",
		featureExtractorBuilder.brochures)

	val LVMClassifier = new ProductClassifier("LVM",
		featureExtractorBuilder.brochures)

	def classifyDemand(text: String): Classification = {
		Classification("demand", demandClassifier.classProbability(text))
	}

	/**
	 * Returns an ordered list of classifications.
	 */
	def classifyProduct(text: String): List[Classification] = {

//		val classification = productClassifier.classProbability(text)
		val classification =  List[Classification](
			Classification("HCM" , HCMClassifier.classProbability(text)),
			Classification("ECOM", ECOMClassifier.classProbability(text)),
			Classification("CRM" , CRMClassifier.classProbability(text)),
			Classification("LVM" , LVMClassifier.classProbability(text))
		)
			classification.sortBy(-_.classificationOutput.prob)
	}

	def classifyProductAsJavaList(text: String): java.util.List[Classification] = {

		//		val classification = productClassifier.classProbability(text)
		val classification =  List[Classification](
			Classification("HCM" , HCMClassifier.classProbability(text)),
			Classification("ECOM", ECOMClassifier.classProbability(text)),
			Classification("CRM" , CRMClassifier.classProbability(text)),
			Classification("LVM" , LVMClassifier.classProbability(text))
		)
		classification.sortBy(-_.classificationOutput.prob).asJava
	}
}
