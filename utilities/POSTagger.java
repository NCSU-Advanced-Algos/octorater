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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import storm.starter.trident.octorater.db.ElasticDB;
import storm.starter.trident.octorater.models.Word;
/**
 *
 * @author shubham
 */
public class POSTagger implements Serializable{
    String comment;
    private static MaxentTagger tagger;
    private ElasticDB eDB;
    private Map<String, Float> fbWords;
    
    public POSTagger() {
        eDB = new ElasticDB();
        fbWords = new HashMap<String, Float>();
    }
    
    public static MaxentTagger getTagger() {
    	if (tagger == null) {
            tagger = new MaxentTagger("data/english-left3words-distsim.tagger");
    	}
    	return tagger;
    }

    public static String getTag(String word) {
    	return getTagger().tagString(word).split(Constants.TAG_SEPERATOR)[1];
    }
    
    
    public List<String> getWords(String sentence){
    	List<String> validWords = new ArrayList<String>();
    	for (String word : getTagger().tagString(sentence).split(" ")) {
    		if (Constants.ValidTags.contains(getTag(word))) {
    			validWords.add(word.split("_")[0]);
    		}
    	}
    	return validWords;
    }
    
    public float evaluate(String sentence, float movieScore) {
        List<String> words = getWords(sentence);
        float score = 0;
        int count = 0;
        Word w;
        for(String word: words) {       
            w = eDB.getWord(word);
            if(w == null) {
            	updateMap(fbWords, word, 0f);
            	continue;
            }
            score += w.getScore();
            count++;
            int wordRank = Utils.getRank(w.getScore());
            int oracleRank = Utils.getRank(movieScore);
            if (wordRank > oracleRank) {
            	updateMap(fbWords, word, Constants.DELTA);
            } else if (wordRank < oracleRank) {
				updateMap(fbWords, word, -Constants.DELTA);
			}
        }
        if(count > 0) {
            score = score/count;
        }
        return score;
    }
    
    private void updateMap(Map<String, Float> map, String wordName, float delta) {
    	if (map.get(wordName) == null ){
    		map.put(wordName, delta);
    		return;
    	}
    	map.put(wordName, map.get(wordName)+ delta);
    }
    
    public void feedback() {
		if (fbWords.keySet().size() < Constants.FEEDBACK_THRESHOLD) {
			return;
		}
		for (String word : fbWords.keySet()) {
			eDB.updateWord(word, fbWords.get(word));
		}
		fbWords = new HashMap<String, Float>();
	}
    
    public static void main(String args[]){
       System.out.println(getTagger().tagString("I am George. I am an engineer. Muhahaha"));	
    }
}
