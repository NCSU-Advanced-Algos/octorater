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
	public static final String DB_INDEX = "rotten";
	public static final String DB_TYPE = "words";
	public static final float BEST = 80;
	public static final float GOOD = 60;
	public static final float DEFAULT = 50;
	public static final float AVERAGE = 40;
	public static final float BAD = 20;
	public static final String WEAK_SUB = "weaksubj";
	public static final String STRONG_SUB = "strongsubj";
	public static final String POSITIVE = "positive";
	public static final String NEGATIVE = "negative";
	public static final String SUBJECTIVITY_PATH= "data/subjectivity.csv";
	public static Set<String> ValidTags = new HashSet<String>(
			Arrays.asList(new String[]{
					"JJ", "JJR", "JJS", "RB", "RBR", "RBS"
					,"VB", "VBD", "VBG", "VBN", "VBP", "VBZ"}
			));
	public static final String TAG_SEPERATOR="_";
	public static final float DELTA = 1f;
	public static final int FEEDBACK_THRESHOLD = 100;
	public static final String OUTPUT_FILE_PATH = "results/result.txt";
}
