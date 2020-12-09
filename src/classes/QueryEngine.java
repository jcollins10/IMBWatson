package classes;
/*
 * Jensen Collins
 * CSC 483, Fall 2020
 * IMB Watson Project, QueryEngine.java
 * Instructor: Mihai Surdeanu
 * TA: Mithun
 * Due: 12/09/2020
 * 
 * The purpose of this file is to run each of the 100 questions against the Lucene index created
 * and return the Precision at 1 score for each of the 4 similiarity types. 
 * 
 * If this file is ran as is, it will lemmatize the queries
 * 
 * For a stemmed query:
 * 		- Comment lines: 102
 * 		- Uncomment lines: 103
 */

import java.io.*;
import java.util.List;
import java.util.Scanner;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.tartarus.snowball.ext.PorterStemmer;
import edu.stanford.nlp.simple.Sentence;
import java.util.ArrayList;

public class QueryEngine {
	private String indexFilePath;
	private Similarity modelType;
	
	public QueryEngine(String indexFilePath, Similarity modelType) {
		this.indexFilePath = indexFilePath;
		this.modelType = modelType;
	}
		
	/*
	 * runQuestions
	 * The purpose of this method is to loop through all of the questions and get the
	 * category, query, and answer from each. Then it calls a method to run the query on the
	 * Lucene index already created and then tests to see if the result was the expected answer
	 * to the query. At the end of testing all the queries, it will return the Precision at 1
	 * score
	 * Parameter: questionFilePath, the file path to all the question queries
	 * Return: A string that shows the overall Precision at 1 score for the similarity
	 *         function passed in
	 */
	public String runQuestions(String questionFilePath) {
		String scoreReport = "";
		String category = "";
		String query = "";
		String answer = "";
		int totalQueriesProcessed = 0;
		int correctAnswers = 0;
		File file = new File(questionFilePath);
		try(Scanner scanner = new Scanner(file)) {
			while(scanner.hasNextLine()) {
				category = scanner.nextLine();
				query = scanner.nextLine();
				answer = scanner.nextLine();
				scanner.nextLine();
				List<ResultClass> topDocsReturned = runEachQuery(category + " " + query);
				// Got the correct answer!
				if(topDocsReturned.get(0).DocName.get("title").equals(answer)) {
					correctAnswers++;
				}
				totalQueriesProcessed++;
			}
			scoreReport = "\tPrecision at 1 score: " + correctAnswers + "/" + totalQueriesProcessed + " = " + (double)correctAnswers/totalQueriesProcessed;
		} catch (FileNotFoundException exception) {
			exception.printStackTrace();
		}
		return scoreReport;
	}
	
	/*
	 * runEachQuery
	 * The purpose of this method is to run each of the queries found within the questions file
	 * against the Lucene index. By default, the method will lemmatize the query, but it can also
	 * be stemmed. The method will return the top 10 documents with the highest scores in a list
	 * of ResultClasses. The top result will then be compared with the answer to see if it
	 * got the correct answer or not. 
	 * Parameter: eachQuery, a String of the each query found in the questions file
	 * Return: A list of the top 10 documents as ResultClasses that were produced from the
	 * 		   Lucene index
	 */
	public List<ResultClass> runEachQuery(String eachQuery) {
		List<ResultClass> queryAnswer = new ArrayList<ResultClass>();
		StandardAnalyzer analyzer = new StandardAnalyzer();
		// Comment out line 102 and uncomment line 103 if you are using stemming
		eachQuery = queryWithLemmas(eachQuery);
		//eachQuery = queryWithStems(eachQuery);
		int hitsPerPage = 10;
		try {
			Directory indexWikiDocs = FSDirectory.open(new File(indexFilePath).toPath());
			Query queryParser = new QueryParser("text", analyzer).parse(eachQuery);
			IndexReader indexReader = DirectoryReader.open(indexWikiDocs);
			IndexSearcher indexSearcher = new IndexSearcher(indexReader);
			indexSearcher.setSimilarity(modelType);
			TopDocs topDocsFound = indexSearcher.search(queryParser, hitsPerPage);
			ScoreDoc[] hitsFound = topDocsFound.scoreDocs;
			for(int i = 0; i < hitsFound.length; i++) {
				ResultClass resultClass = new ResultClass();
				int docId = hitsFound[i].doc;
				Document document = indexSearcher.doc(docId);
				resultClass.DocName = document;
				resultClass.doc_score = hitsFound[i].score;
				queryAnswer.add(resultClass);
			}
			indexReader.close();
		} catch (ParseException exception1) {
			exception1.printStackTrace();
		} catch (IOException exception2) {
			exception2.printStackTrace();
		}
		return queryAnswer;
	}
	
	/*
	 * queryWithLemmas
	 * The purpose of this method is to take each query from the question
	 * file and lemmatize it 
	 * Parameter: eachQuery, each query found in the questions file
	 * Return: the query found in the question file but lemmatized
	 */
	private String queryWithLemmas(String eachQuery) {
		String lemmaQuery = "";
		Sentence querySentence = new Sentence(eachQuery.toLowerCase());		
		for(String eachWord : querySentence.lemmas()) {
			lemmaQuery += eachWord + " ";
		}
		return lemmaQuery;
	}
	
	/*
	 * queryWithStems
	 * The purpose of this method is to take each query from the question
	 * file and stem it
	 * Parameter: eachQuery, each query found in the question file
	 * Return: the query found in the question file but stemmed
	 */
	private String queryWithStems(String eachQuery) {
		String stemQuery = "";
		PorterStemmer porterStemmer;
		Sentence sentence = new Sentence(eachQuery.toLowerCase());
		for (String eachWord : sentence.words()) {
			porterStemmer = new PorterStemmer();
			porterStemmer.setCurrent(eachWord);
			porterStemmer.stem();
			stemQuery += eachWord + " ";
		}
		return stemQuery;
	}	
}