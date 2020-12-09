/*
 * Jensen Collins
 * CSC 483, Fall 2020
 * IMB Watson Project, CreateIndex.java
 * Instructor: Mihai Surdeanu
 * TA: Mithun
 * Due: 12/09/2020
 * 
 * The purpose of this file is to create the index for all of the Wiki documents found in the 80
 * Wiki files given. It goes through each Wiki file and parses each document found which
 * are seperated by the titles found. An example of a title will be [['title']]. Each Wiki
 * document will be added to the Lucene index to be used to any the 100 query questions. This
 * file has the ability to create an index with no lemmaization and stemming, just lemmaization,
 * or just stemming.
 * 
 * If this file is ran as is, it will create an index with the contents found inside the Wiki files
 * as they are. 
 * 
 * For a lemmatize index:
 * 		- Uncomment lines: 151, 159, 167, and 168
 * 
 * For a stemmed index:
 * 		- Uncomment lines: 153, 161, 170, and 171
 * 
 * This class should not be called by the grader, and should use the index already created
 * which should be found within the report via the URL given.
 */

import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.tartarus.snowball.ext.PorterStemmer;
import edu.stanford.nlp.simple.Sentence;

public class CreateIndex {
	private String indexFilePath;
	
	public CreateIndex(String indexFilePath) {
		this.indexFilePath = indexFilePath;	
	}
	 
	/* parseWikiFiles
	 * The purpose of this method is to parse all 80 Wiki files that were given. It will create the index 
	 * directory to store the index and set up the IndexWriter to be able to add to Lucene. Then iterate
	 * over all the Wiki files to be parsed. 
	 * Parameters: directoryPath, the path to where the Wiki files are stored to be parsed
	 * Return: N/A
	 */ 
	public void parseWikiFiles(String directoryPath) throws java.io.FileNotFoundException,java.io.IOException {
		StandardAnalyzer standardAnalyzer = new StandardAnalyzer();
		Directory watsonIndex = FSDirectory.open(new File(indexFilePath).toPath());
		IndexWriterConfig indexConfig = new IndexWriterConfig(standardAnalyzer);
		IndexWriter indexWriter = new IndexWriter(watsonIndex, indexConfig);
		File directory = new File(directoryPath);
		// Goes through each Wiki file containing all the Wiki documents
		for(String eachFile : directory.list()) {
			parseEachWikiFile(directoryPath + "/" + eachFile, indexWriter);
			System.out.println("Finished parsing: " + eachFile);
		}
		indexWriter.close();
		watsonIndex.close();
	}
	
	/* parseEachWikiFile
	 * The purpose of this method is to go through each Wiki file and parse it so the contents
	 * can be added into the Lucene index. It will go through and look for each title which will
	 * indiciate the start of a new document. Then it will look for categories that document may have
	 * and then add the remaining contents within that document. The index will ignore the contents
	 * in 'Further reading', 'External Links', 'See also', and 'References' because none of that data
	 * will help in answering the query correctly. Also attachments such as 'File:' and 'Image:' will
	 * also be skipped for the same reason
	 * Parameters: eachWikiFile, each Wiki file that needs to be parsed
	 * 			   indexWriter, the index writer to add to the Lucene index
	 * Return: N/A
	 */
	public void parseEachWikiFile(String eachWikiFile, IndexWriter indexWriter) {
		String title = ""; 							// A title found inside each Wiki file
		String category = ""; 						// The category found inside each title
		String textInDoc = ""; 						// A string that will hold the text inside each title
		File wikiFile = new File(eachWikiFile); 	// Each Wiki file that needs to be processed
		try (Scanner scanner = new Scanner(wikiFile, "utf-8")) {
			while (scanner.hasNextLine()) {
				String line = scanner.nextLine();
				int lineLength = line.length();
				// Parse the Title
				if(lineLength > 4 && line.substring(0, 2).equals("[[") && line.substring(lineLength - 2, lineLength).equals("]]")) {
					// Check to see if end of document to be added into index
					if(!title.equals("") && !title.contains("File:") && !title.contains("Image:")) {
						addDoc(indexWriter, title, category.trim(), textInDoc.toString().trim());
					}				
					title = line.substring(2, lineLength - 2);
					textInDoc = "";
				}
				// Parse the Category
				else if(line.indexOf("CATEGORIES:") == 0) {
					category = line.substring(12);
				}
				// Parse the Header
				else if(lineLength > 2 && line.charAt(0) == '=' && line.charAt(lineLength - 1) == '=') {
					// Take away all of the '=' to the start and end of the line to add the line
					while(line.length() > 2 && line.charAt(0) == '=' && line.charAt(line.length() - 1) == '=') {
						line = line.substring(1, line.length() - 1);
					}
					if(!line.equals("Further reading") && !line.equals("External links") && !line.equals("See also") && !line.equals("References")) {
						textInDoc += line + " ";
					}
				}
				// Any other contents inside the doucment
				else {
					textInDoc += line + " ";
				}
			}
			// Need to add the last document after while Scanner loop
			addDoc(indexWriter, title, category.trim(), textInDoc.trim());
			scanner.close();
		} catch (IOException exception) {
			exception.printStackTrace();
		}
	}
	
	/*
	 * addDoc
	 * The purpose of this method is to add the all the contents of each document into the Lucene index. First
	 * it will add the Title. Then, it will store the categories if they are present. Then, it will store the
	 * title + the rest of the documents contents. That way all of the contents found will be under that specific
	 * title.
	 * Parameters: indexWriter, the index writer to add to the Lucene index
	 * 			   title, the title found within the Wiki file
	 * 			   category, the categories found within the Wiki file for this title
	 * 			   documentText, the document contents found within the Wiki file for this title
	 * Return: N/A
	 */
	private void addDoc(IndexWriter indexWriter, String title, String category, String documentText) throws IOException {
		Document document = new Document();
		document.add(new StringField("title", title, Field.Store.YES));
		if(category.equals("") && documentText.equals("")) {
			document.add(new TextField("category", " ", Field.Store.YES));
			document.add(new TextField("text", title + " ", Field.Store.YES));
		} else if(!category.equals("") && documentText.equals("")) {
			// If both lines 151 & 153 are commented out, then it will add the categories as they were found
			// Comment line 151 out if you DON'T want to lemmatize the categories
			//category = makeLemmas(category);
			// Comment line 153 out if you DON'T want to stem the categories
			//category = makeStems(category);
			document.add(new TextField("category", category.toLowerCase(), Field.Store.YES));
			document.add(new TextField("text", title + " " + category.toLowerCase(), Field.Store.YES)); 
		} else if (category.equals("") && !documentText.equals("")) {
			// If both lines 159 & 161 are commented out, then it will add the doucment text as they were found
			// Comment line 159 out if you DON'T want to lemmatize the document text
			//documentText = makeLemmas(documentText);
			// Comment line 161 out if you DON't want to stem the document text
			//documentText = makeStems(documentText);
			document.add(new TextField("category", " ", Field.Store.YES));
			document.add(new TextField("text", title + " " + documentText.toLowerCase(), Field.Store.YES)); 
		} else {
			// If lines 167, 168, 170, and 171 are commented out, then it will add the categories and document text as they were found
			// Comment lines 167 & 168 out if you DON'T want to lemmatize the categories and document text
			//category = makeLemmas(category);
			//documentText = makeLemmas(documentText);
			// Comment lines 170 & 171 out if you DON'T want to stem the categories and document text
			//category = makeStems(category);
			//documentText = makeStems(documentText);
			document.add(new TextField("category", category.toLowerCase().trim(), Field.Store.YES));
			document.add(new TextField("text", title + " " + category.toLowerCase() + " " + documentText.toLowerCase(), Field.Store.YES)); 
		}
		indexWriter.addDocument(document);
	}
	
	/*
	 * makeLemmas
	 * The purpose of this method is to take in a string of words and lemmatize them before 
	 * they are added into the Lucene index. The string could be the categories or document text
	 * Parameters: stringToLemma, the string that needs to be lemmatized
	 * Return: The lemmatized string that now can be added into the index.
	 */
	private String makeLemmas(String stringToLemma) {
		String lemmaReturn = "";
		Sentence sentence = new Sentence(stringToLemma.toLowerCase());
		for (String wordLemma : sentence.lemmas()){
			lemmaReturn += wordLemma + " ";
		}
		return lemmaReturn;
	}
	
	/*
	 * makeStems
	 * The purpose of this method is to take in a string of words and stem them before they are
	 * added into the Lucene index. The string could be the categories or document text
	 * Parameters: stringToStem, the string that needs to be stemmed
	 * Return: The stemmed string that now can be added into the index. 
	 */
	private String makeStems(String stringToStem) {
		String returnStemString = "";
		PorterStemmer porterStemmer;
		Sentence sentence = new Sentence(stringToStem.toLowerCase());
		for (String eachWord : sentence.words()) {
			porterStemmer = new PorterStemmer();
			porterStemmer.setCurrent(eachWord);
			porterStemmer.stem();
			returnStemString += eachWord + " ";
		}
		return returnStemString;
	}
}
