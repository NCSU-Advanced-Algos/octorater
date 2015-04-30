/**
 * @author: George Mathew (george2@ncsu.edu)
 */
package storm.starter.trident.octorater.models;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author root
 *
 */
public class Word implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4184505316879531959L;

	private String name;
	
	private String tag;
	
	private float score;
	
	private String _id;
	
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public float getScore() {
		return score;
	}

	public void setScore(float score) {
		this.score = score;
	}

	public void setID(String _id){
		this._id = _id;
	}
	
	public String getID() {
		return _id;
	}
	
	@Override
	public String toString() {
		return "Word [name=" + name + ", tag=" + tag + ", score=" + score + "]";
	}
	
	/***
	 * Utility method to convert a word object to a hash map
	 * @param word
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Map toMap(){
		Map wordMap = new HashMap();
		wordMap.put("name", this.name);
		wordMap.put("tag", this.tag);
		wordMap.put("score", this.score);
		return wordMap;
	}
	
}
