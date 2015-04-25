package storm.starter.trident.octorater.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 * @author
 *  George Mathew (george2),
 *  Kapil Somani  (kmsomani),
 *	Kumar Utsav	  (kutsav),
 *	Shubham Bhawsinka (sbhawsi)
 */
public class Movie implements Serializable {

	private static final long serialVersionUID = -5292308552205299936L;
	
	private String name;
	private String id;
	private int rating;
	private int score;
	private List<String> comments;
	
	public Movie(String id) {
		this.id = id;
	}
	
	public Movie(String id, String name, int score) {
		this(id);
		this.name = name;
		this.score = score;
		this.comments = new ArrayList<String>();
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public int getRating() {
		return rating;
	}

	public void setRating(int rating) {
		this.rating = rating;
	}

	public int getScore() {
		return score;
	}

	public void setScore(int score) {
		this.score = score;
	}

	public List<String> getComments() {
		return comments;
	}

	public void setComments(List<String> comments) {
		this.comments = comments;
	}
	
	public void addComment(String comment){
		if (this.comments == null)
			this.comments = new ArrayList<String>();
		this.comments.add(comment);
	}
	
	public static Movie makeMovie(JSONObject movieJSON) {
		try {
			Movie movie = new Movie(movieJSON.get("id").toString(), movieJSON.get("title").toString(), Integer.parseInt(((JSONObject)movieJSON.get("ratings")).get("audience_score").toString()));
			return movie;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public void updateComments(String reviewsAsString) {
		try {
			JSONParser parser = new JSONParser();
			JSONArray reviews = (JSONArray)((JSONObject) parser.parse(reviewsAsString)).get("reviews");
			if (comments == null) 
				comments = new ArrayList<String>();
			JSONObject reviewObj;
			for (Object review : reviews) {
				reviewObj = (JSONObject) review;
				comments.add(reviewObj.get("quote").toString());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public String toString() {
		return "Movie [name=" + name + ", id=" + id + ", rating=" + rating
				+ ", score=" + score + ", comments=" + comments + "]";
	}
}
