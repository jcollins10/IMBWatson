/*
 * Jensen Collins
 * CSC 483, Fall 2020
 * IMB Watson Project, ResultClass.java
 * Instructor: Mihai Surdeanu
 * TA: Mithun
 * Due: 12/09/2020
 * 
 * The purpose of this file is to store the top 10 documents that are returned after each query
 * is processed through the Lucene index. Each document that is returned will have it's document
 * name stored, which is the title of the Wiki document, along with the score produced by the 
 * scoring function that was passed in. 
 */

import org.apache.lucene.document.Document;

public class ResultClass {
    Document DocName;
    double doc_score = 0;
}
