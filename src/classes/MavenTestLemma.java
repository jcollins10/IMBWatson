package classes;

import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.BooleanSimilarity;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.search.similarities.LMJelinekMercerSimilarity;
import org.junit.jupiter.api.Test;


public class MavenTestLemma {
	// indexFilePath will need to be named whatever directory holds the index
	static String indexFilePath = "index/lemmas";
	static String inputDirectory = "WikiDocs";
	static String questionQueries = "questions.txt";

	@Test
	public void test() {
		// Lines 35 and 36 should be commented out for graders so index is not created
		//CreateIndex index = new CreateIndex(indexFilePath);
		//index.parseFiles(inputDirectory);
		
		// BM25 scoring function
		QueryEngine queryEngine = new QueryEngine(indexFilePath, new BM25Similarity());
		System.out.println("Running the BM25 model.");
		String bm25Model = 	queryEngine.runQuestions(questionQueries);
		System.out.println("Score from using the BM25 model:");
		System.out.println(bm25Model);
		
		// td/idf scoring function
		queryEngine = new QueryEngine(indexFilePath, new ClassicSimilarity());
		System.out.println("\nRunning the vector model with tf/idf scoring 1.\n");
		String vectorModelTFIDF = queryEngine.runQuestions(questionQueries);
		System.out.println("Score from using the vector space model and tf/idf:");
		System.out.println(vectorModelTFIDF);
		
		// Boolean search scoring function
		queryEngine = new QueryEngine(indexFilePath, new BooleanSimilarity());
		System.out.println("\nRunning the Boolean model.\n");
		String booleanModel = queryEngine.runQuestions(questionQueries);
		System.out.println("Score from using the boolean model:");
		System.out.println(booleanModel);
		
		// Jelinek Mercer, with lambda score of 0.5, scoring function
		queryEngine = new QueryEngine(indexFilePath, new LMJelinekMercerSimilarity((float) 0.5));
		System.out.println("\nRunning the Jelinek Mercer model.\n");
		String jelinekMercer = 	queryEngine.runQuestions(questionQueries);
		System.out.println("Score from using the Jelinek Mercer model:");
		System.out.println(jelinekMercer);
	}
}
