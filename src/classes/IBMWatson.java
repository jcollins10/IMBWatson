package classes;
/*
 * Jensen Collins
 * CSC 483, Fall 2020
 * IMB Watson Project, IBMWatson.java
 * Instructor: Mihai Surdeanu
 * TA: Mithun
 * Due: 12/09/2020
 * 
 * The purpose of this file is to run and the IMBWatson machine. At first it will create and make
 * the index and then it will run the queries with different scoring functions. First it will use 
 * the standard lucene scoring function which is the BM25 model, then it will test with the td/idf
 * scoring model, then it will test with a boolean scoring model, and finally with the Jelinek
 * Mercer scoring model with a lambda score of 0.5. It will print out the Precision at 1 score for
 * each of the scoring functions. 
 * 
 * Creating the index will be commented out because the grader should have the index already
 * created from the URL given in the report. 
 */

import java.io.FileNotFoundException;
import java.io.IOException;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.BooleanSimilarity;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.search.similarities.LMJelinekMercerSimilarity;

public class IBMWatson {
	// indexFilePath will need to be named whatever directory holds the index
	static String indexFilePath = "index/stems";
	static String inputDirectory = "WikiDocs";
	static String questionQueries = "questions.txt";
		
	public static void main(String[] args ) throws FileNotFoundException, IOException {
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
		System.out.println("\nRunning the vector model with tf/idf.\n");
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
