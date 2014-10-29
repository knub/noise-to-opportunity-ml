package de.hpi.smm

import java.io.StringReader

import org.apache.lucene.analysis.tokenattributes.CharTermAttribute
import org.apache.lucene.analysis.{TokenStream, Analyzer}
import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.util.Version
import org.apache.mahout.math.{SequentialAccessSparseVector, RandomAccessSparseVector}
import org.apache.mahout.vectorizer.encoders.{FeatureVectorEncoder, StaticWordValueEncoder}

object Main {

	def main(args: Array[String]): Unit = {
		val encoder: FeatureVectorEncoder = new StaticWordValueEncoder("text")
		val analyzer: Analyzer = new StandardAnalyzer(Version.LUCENE_46)
		val in = new StringReader("text to magically vectorize")

		val ts: TokenStream = analyzer.tokenStream("body", in)
		val termAtt = ts.addAttribute(classOf[CharTermAttribute])
		val v1 = new RandomAccessSparseVector(100)
		ts.reset()
		while (ts.incrementToken()) {
			val termBuffer = termAtt.buffer()
			val termLen = termAtt.length()
			val w = new String(termBuffer, 0, termLen)
			encoder.addToVector(w, 1, v1)
		}
		System.out.printf("%s\n", new SequentialAccessSparseVector(v1))
	}

}
