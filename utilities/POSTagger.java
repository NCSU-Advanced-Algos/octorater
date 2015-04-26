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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;
/**
 *
 * @author shubham
 */
public class POSTagger {
    String comment;
    PriorityQueue<String> priQueue;
   // int k=Integer.MAX_VALUE;
    private static MaxentTagger tagger;
    
    public static MaxentTagger getTagger() {
    	if (tagger == null) {
    		try {
				tagger = new MaxentTagger("data/left3words-wsj-0-18.tagger");
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
    	}
    	return tagger;
    }

    public static String getTag(String word) {
    	return getTagger().tagString(word).split("/")[1];
    }
    
    public void addToTagger(String element){
    	String tagged = getTagger().tagString(element);
    	System.out.println(tagged);
    }
    
    public List<String> printTopK(int k){
        List<String> ls = new ArrayList<String>();
        Iterator<String> iter = priQueue.iterator();
        while(iter.hasNext() && k>=1){
            ls.add(iter.next());
            k--;
        }
        return ls;
    }
    
    public static void main(String args[]){
        POSTagger p = new POSTagger();
        p.addToTagger("I");
    }
}
