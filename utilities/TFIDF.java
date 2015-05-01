/**
 * @author: George Mathew (george2@ncsu.edu)
 */
package storm.starter.trident.octorater.utilities;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import storm.starter.trident.octorater.db.ElasticDB;
import storm.starter.trident.octorater.models.Word;

/**
 * @author
 *  George Mathew (george2),
 *  Kapil Somani  (kmsomani),
 *	Kumar Utsav	  (kutsav),
 *	Shubham Bhawsinka (sbhawsi)
 */
public class TFIDF implements Serializable{
	
	private static final long serialVersionUID = 3969018936732030053L;
	/***
	 * Map that contains word and number of documents it occurs in.
	 */
	private Map<String, Integer> wordMap;
	/***
	 * Total number of documents
	 */
	private Integer N;
	
	public Map<String, Integer> getWordMap() {
		return wordMap;
	}

	public void setWordMap(Map<String, Integer> wordMap) {
		this.wordMap = wordMap;
	}
	
	public Integer getN() {
		return N;
	}

	public void setN(Integer n) {
		N = n;
	}
	
	public void incrementDoc(){
		this.N++;
	}

	public TFIDF() {
		setWordMap(new HashMap<String, Integer>());
		setN(0);
	}
	
	public void update(String word, Integer count){
		if (wordMap.get(word) == null){
			wordMap.put(word, count);
		} else {
			wordMap.put(word, wordMap.get(word) + count);
		}
	}
	
	public Integer getDocCount(String word){
		Integer count = wordMap.get(word);
		if (count == null)
			return 0;
		return count;
	}
	
	/***
	 * Read File and create a map of all words and the number of documents it occurs in
	 * @param updateDB - If true elastic DB will be updated
	 * @return - A TFIDF object with a map of all words and total doc count
	 */
	public static TFIDF buildWordMap(boolean updateDB) {
		TFIDF tfidf = new TFIDF();
		POSTagger tagger = new POSTagger();
		Set<String> words;
		int documents = 0;
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(new File(Constants.REVIEWS_PATH)));
			String line;
			while((line = br.readLine()) != null) {
				documents++;
				words = tagger.getWords(line);
				for (String word : words){
					tfidf.update(word, 1);
				}
			}
		} catch (FileNotFoundException e) {
			System.out.println("File " + Constants.REVIEWS_PATH + " was not found.");
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("IO exception while reading " + Constants.REVIEWS_PATH + " " + e.getMessage());
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
					throw new RuntimeException(e);
				}
			}
		}
		if (updateDB) {
			ElasticDB elasticDB = new ElasticDB();
			elasticDB.updateTotalDocs(documents);
			elasticDB.bulkUpdateWordDocFrequency(tfidf.getWordMap(), true);
		}
		tfidf.setN(documents);
		return tfidf;
	}
	
	/***
	 * Update Elastic Search for document frequencies.
	 * Used in feed back.
	 */
	public void updateDB(){
		Utils.writeToFile("******* UPDATING TFIDF *********");
		ElasticDB elasticDB = new ElasticDB();
		elasticDB.updateTotalDocs(this.N);
		setN(0);
		elasticDB.bulkUpdateWordDocFrequency(this.wordMap, false);
		setWordMap(new HashMap<String, Integer>());
	}
	
	@Override
	public String toString() {
		StringBuilder sb= new StringBuilder();
		for (String word: this.wordMap.keySet()) {
			sb.append("Word : " + word).append(", Count : " + this.getDocCount(word)).append("\n");
		}
		return sb.toString();
	}
	/***
	 * Compute TFIDF score for a word 
	 * @param word - Word to be considered
	 * @param sentence - Sentence the word belongs in
	 * @return - TFIDF weight
	 */
	public static float tfidf(String word, String sentence) {
		return TF(word, sentence)*IDF(word);
	}
	
	/***
	 * Compute Term Frequency of a word in a sentence 
	 * @param word
	 * @param sentence
	 * @return
	 */
	public static int TF(String word, String sentence){
		sentence = sentence.toLowerCase();
		List<String> words = Arrays.asList(sentence.split("[\\s.,!?]+"));
		return Collections.frequency(words, word);
	}
	
	/***
	 * Retrieve document frequency of a word from elastic search
	 * @param wordName
	 * @return
	 */
	public static float IDF(String wordName){
		ElasticDB elasticDB = new ElasticDB();
		int total = elasticDB.getTotalDocs();
		Word word = elasticDB.getWordDocFrequency(wordName);
		int docFreq = 0;
		if (word != null) {
			docFreq = word.getDocCount();
		}
		return (float) Math.log(total/(1+docFreq));
	}
	
	public static void main(String[] args) {
		//System.out.println(buildWordMap(false));
		String str = "How are you           asd. I know!!!! Moron ";
		List<String> words = Arrays.asList(str.split("[\\s.,!?]+"));
		System.out.println(words);
	}

	
}
