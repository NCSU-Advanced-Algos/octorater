/**
 * @author: George Mathew (george2@ncsu.edu)
 */
package storm.starter.trident.octorater.models;

import java.io.Serializable;

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
}
