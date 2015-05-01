/**
 * @author: George Mathew (george2@ncsu.edu)
 */
package storm.starter.trident.octorater.utilities;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author
 *  George Mathew (george2),
 *  Kapil Somani  (kmsomani),
 *	Kumar Utsav	  (kutsav),
 *	Shubham Bhawsinka (sbhawsi)
 */
public class Constants {
	/***
	 * Rotten Tomatoes API Key
	 */
	public static final String API_KEY = "qynq4687htc3z7mq2ec7y67x";
	/***
	 * Index Name of Elastic Search
	 */
	public static final String DB_INDEX = "rotten";
	/***
	 * Type to store subjectivity
	 */
	public static final String DB_TYPE = "words";
	/***
	 * Best Score
	 */
	public static final float BEST = 90;
	/***
	 * Good Score
	 */
	public static final float GOOD = 80;
	/***
	 * Default Score
	 */
	public static final float DEFAULT = 50;
	/***
	 * Bad movie score
	 */
	public static final float AVERAGE = 20;
	/***
	 * Horrible movie score
	 */
	public static final float BAD = 10;
	/***
	 * Weak Subjectivity
	 */
	public static final String WEAK_SUB = "weaksubj";
	/***
	 * Strong Subjectivity
	 */
	public static final String STRONG_SUB = "strongsubj";
	/***
	 * Positive Emotion
	 */
	public static final String POSITIVE = "positive";
	/***
	 * Negative Emotion
	 */
	public static final String NEGATIVE = "negative";
	/***
	 * Path for Subjectivity CSV
	 */
	public static final String SUBJECTIVITY_PATH= "data/subjectivity.csv";
	/***
	 * Set of valid tags, includes, adverbs, adjectives, verbs and improper nouns
	 */
	public static Set<String> ValidTags = new HashSet<String>(
			Arrays.asList(new String[]{
					"JJ", "JJR", "JJS", "RB", "RBR", "RBS"
					,"VB", "VBD", "VBG", "VBN", "VBP", "VBZ"
					,"NN" , "NNS"}
			));
	/***
	 * Seperator used by POS tagger
	 */
	public static final String TAG_SEPERATOR="_";
	/***
	 * Feedback incremental Delta
	 */
	public static final float DELTA = 1f;
	/***
	 * Threshold of words for performing feedback
	 */
	public static final int FEEDBACK_THRESHOLD = 1000;
	/***
	 * File to write output to
	 */
	public static final String OUTPUT_FILE_PATH = "results/result.txt";
	/***
	 * Current year
	 */
	public static final int CURRENT_YEAR = 2015;
	/***
	 * Path for file that contains reviews used to populate TFIDF
	 */
	public static final String REVIEWS_PATH = "data/reviews.txt";
	/**
	 * Elastic Search type that stores the total number of documents  
	 */
	public static final String TOTAL_DOCUMENT_TYPE = "totalDocs";
	/**
	 * Elastic Search document ID that stores the total number of documents  
	 */
	public static final String TOTAL_DOCUMENT_ID = "total";
	/**
	 * Elastic Search type that stores the document frequency for each word  
	 */
	public static final String WORD_DOC_FREQ_TYPE = "wordDocFreq";
	/***
	 * Minimum threshold for a score to be considered
	 */
	public static final int MIN_THRESHOLD = 5;
	/****
	 * Score that splits good and bad
	 */
	public static final float SCORE_THRESHOLD = 50f;
}