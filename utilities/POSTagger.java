/*
 * Copyright 2015 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package storm.starter.trident.octorater.utilities;

import edu.stanford.nlp.tagger.maxent.MaxentTagger;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
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
public class POSTagger implements Serializable{
	private static final long serialVersionUID = 6164064082754267628L;
	String comment;
    private static MaxentTagger tagger;
    private ElasticDB eDB;
    private Map<String, Float> fbWords;
    private TFIDF tfidf;
    
    public POSTagger() {
        eDB = new ElasticDB();
        fbWords = new HashMap<String, Float>();
        tfidf = new TFIDF();
    }
    
    /***
     * Singleton method to create a tagger
     * @return
     */
    public static MaxentTagger getTagger() {
    	if (tagger == null) {
            tagger = new MaxentTagger("data/english-left3words-distsim.tagger");
    	}
    	return tagger;
    }

    /***
     * Method to get tag for a certain word
     * @param word
     * @return
     */
    public static String getTag(String word) {
    	String[] splits = getTagger().tagString(word).split(Constants.TAG_SEPERATOR);
    	if (splits.length > 1){
    		return splits[1];
    	} else {
    		return null;
    	}
    }
    
    /***
     * Method to get a list of valid words from a sentence
     * @param sentence
     * @return - Set of valid words without duplicates
     */
    public Set<String> getWords(String sentence){
    	Set<String> validWords = new HashSet<String>();
    	for (String word : getTagger().tagString(sentence).split(" ")) {
    		if (Constants.ValidTags.contains(getTag(word))) {
    			validWords.add(word.split("_")[0].toLowerCase());
    		}
    	}
    	return validWords;
    }
    
    /***
     * Evaluate score for a comment
     * @param sentence - Comment to be evaluated
     * @param movieScore - Actual score of a movie(Used for feed back)
     * @return - Float value which represents score of a movie
     */
    public float evaluate(String sentence, float movieScore) {
        Set<String> words = getWords(sentence);
        float score = 0;
        float sum_wt = 0;
        Word w;
        float tfIdf_score;
        for(String word: words) {       
            w = eDB.getWord(word);
            tfIdf_score = TFIDF.tfidf(word, sentence);
            if(w == null) {
            	updateMap(fbWords, word, 0f);
            	tfidf.update(word, 1);
            	continue;
            }
            score += tfIdf_score * w.getScore();
            sum_wt += tfIdf_score;
            int wordRank = Utils.getRank(w.getScore());
            int oracleRank = Utils.getRank(movieScore);
            if (wordRank > oracleRank) {
            	updateMap(fbWords, word, Constants.DELTA);
            } else if (wordRank < oracleRank) {
				updateMap(fbWords, word, -Constants.DELTA);
			}
            tfidf.update(word, 1);
        }
        if(sum_wt > 0) {
            score = score/sum_wt;
        }
        tfidf.incrementDoc();
        return score;
    }
    
    /***
     * Update feedback map with a word
     * @param map
     * @param wordName
     * @param delta
     */
    private void updateMap(Map<String, Float> map, String wordName, float delta) {
    	if (map.get(wordName) == null ){
    		map.put(wordName, delta);
    		return;
    	}
    	map.put(wordName, map.get(wordName)+ delta);
    }
    
    /***
     * Update Elastic DB for feedback
     */
    public void feedback() {
		if (fbWords.keySet().size() < Constants.FEEDBACK_THRESHOLD) {
			return;
		}
		Utils.writeToFile("******* UPDATING WORDS *********");
		for (String word : fbWords.keySet()) {
			eDB.updateWord(word, fbWords.get(word));
		}
		Utils.writeToFile("******* END UPDATING WORDS *********");
		fbWords = new HashMap<String, Float>();
		tfidf.updateDB();
	}
    
    public static void main(String args[]){
       System.out.println(getTagger().tagString("I am George. I am an engineer. Muhahaha"));	
    }
}
