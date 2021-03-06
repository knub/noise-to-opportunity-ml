import com.blog_intelligence.nto.*;
import scala.Tuple2;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class JavaExample {

	static DataBaseConfiguration CONFIG = new DataBaseConfiguration(
			"141.89.225.134", 				// ip
			"30315",						// port
			"SMA1415", 						// user
			"Popcorn54",					// password
			"SMA1415.CLASSIFIED_POSTS"		// database
	);

	static File DEMAND_MODEL_FILE = new File("demand.model");
	static File PRODUCT_MODEL_FILE = new File("product.model");

	private static void predictSingleDoc(){
		System.out.println("Execute example for predicting a single post");
		/**
		 * Building classifier
		 */

		NTOClassifier classifier;
		if (DEMAND_MODEL_FILE.exists() && PRODUCT_MODEL_FILE.exists()) {
			System.out.print("Reading from model file");
			long l1 = System.currentTimeMillis();
			classifier = readFromModel();
			long l2 = System.currentTimeMillis();
			System.out.println(" in " + (l2 - l1) + " ms.");
		} else {
			System.out.print("Creating new model");
			long l1 = System.currentTimeMillis();
			classifier = buildModelFromScratch();
			long l2 = System.currentTimeMillis();
			System.out.println(" in " + (l2 - l1) + " ms.");
		}

		/**
		 * Predicting demand and product
		 */
		String post = "Hi! I am the CTO of Startup Inc. Lately, I have problems organising my customers. " +
				"Do you have any recommendations for a good crm system to handle them?";

		System.out.println(post);

		double probDemand = classifier.predictDemand(post);
		System.out.println("Demand probability " + probDemand);

		List<ProductClassification> probsProduct = classifier.predictProduct(post);
		for (ProductClassification classification : probsProduct) {
			System.out.println(classification.product() + ": " + classification.prob());
		}
	}

	public static void predictMultipleDocs() {
		System.out.println("Execute example for predicting a multiple posts");
		/**
		 * Building classifier
		 */
		NTOClassifier classifier;
		if (DEMAND_MODEL_FILE.exists() && PRODUCT_MODEL_FILE.exists()) {
			System.out.print("Reading from model file");
			long l1 = System.currentTimeMillis();
			classifier = readFromModel();
			long l2 = System.currentTimeMillis();
			System.out.println(" in " + (l2 - l1) + " ms.");
		} else {
			System.out.print("Creating new model");
			long l1 = System.currentTimeMillis();
			classifier = buildModelFromScratch();
			long l2 = System.currentTimeMillis();
			System.out.println(" in " + (l2 - l1) + " ms.");
		}


		/**
		 * Fill posts
		 */
		List<String> posts = new ArrayList<>();
		posts.add("Hi! I am the CTO of Startup Inc. Lately, I have problems organising my customers. " +
				"Do you have any recommendations for a good crm system to handle them?");

		posts.add("Hi! I am the CTO of Startup Inc. Lately, I have problems reach my customers. " +
				"Do you have any recommendations for a commerce software?");

		posts.add("Hi! I am the CTO of Startup Inc. Lately, I have problems virtual resources. " +
				"Do you have any recommendations for a good virtualization software?");

		posts.add("Hi! I am the CTO of Startup Inc. Lately, I have problems organising my employees. " +
				"Do you have any recommendations for a good HR software?");

		/**
		 * Predict for each class the most certain
		 */
		for(String productClass: new String[] {"CRM", "ECOM", "HCM", "LVM"}){
			List<PredictedPost> predictedPosts = classifier.extractMostCertainPosts(
					3,				// top most
					productClass, 	// for this class
					posts);			// from these posts

			System.out.println("best posts for " + productClass);
			for(PredictedPost predictedPost: predictedPosts){
				System.out.println("Text: " + predictedPost.text());
				System.out.println("Demand prob: " + predictedPost.fullPrediction().demandProb());
				System.out.println("Product prob: " + predictedPost.fullPrediction().productProb());
				System.out.println("===================================");
			}
		}


	}

	public static void main(String[] args) {

		predictSingleDoc();
		predictMultipleDocs();

	}

	public static NTOClassifier readFromModel() {
		NTOClassifier classifier = new NTOClassifier(
				new File("stopwords.txt"),
				new File("german-fast.tagger")
		);

		classifier.loadDemand(DEMAND_MODEL_FILE);
		classifier.loadProduct(PRODUCT_MODEL_FILE);

		return classifier;
	}

	public static NTOClassifier buildModelFromScratch() {
		/**
		 * Reading training data
		 */
		DocumentExtractor documentExtractor = new DocumentExtractor(
				new File("stopwords.txt"),
				new File("german-fast.tagger")
		);

		// Adapt files here if necessary.
		ReadingResult csvDocs = documentExtractor.readFromCSV(
				new File("linked_in_posts.csv"),
				new File("brochures.csv"),
				new File("classification.json")
		);

		// Load documents from database. Can be used in the same way as csvDocs, or even combined with csvDocs.
		// ReadingResult dbDocs = documentExtractor.readFromDB(CONFIG);
		// Like this:
		// List<Document> combined = new ArrayList<>();
		// combined.addAll(csvDocs.demandDocuments());
		// combined.addAll(dbDocs.demandDocuments());

		/**
		 * Building classifier
		 */
		NTOClassifier classifier = new NTOClassifier(
				new File("stopwords.txt"),
				new File("german-fast.tagger")
		);

		// Training
		classifier.trainDemand(csvDocs.demandDocuments());
		classifier.trainProduct(csvDocs.productDocuments());

		// Persisting for next run
		classifier.persistDemand(DEMAND_MODEL_FILE);
		classifier.persistProducts(PRODUCT_MODEL_FILE);

		return classifier;
	}

}
