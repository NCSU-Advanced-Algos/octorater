package storm.starter.trident.octorater.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 * Model Object that holds the movie
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
	private float rating;
	private float score;
	private List<String> comments;
	private int releaseYear;
	private int positives = 0;
	private int negatives = 0;
	
	public Movie(String id) {
		this.id = id;
	}
	
	public Movie(String id, String name, float score) {
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

	public float getRating() {
		return rating;
	}

	public void setRating(float rating) {
		this.rating = rating;
	}

	public float getScore() {
		return score;
	}

	public void setScore(float score) {
		this.score = score;
	}

	public List<String> getComments() {
		return comments;
	}

	public void setComments(List<String> comments) {
		this.comments = comments;
	}
	
	public int getReleaseYear() {
		return releaseYear;
	}

	public void setReleaseYear(int releaseYear) {
		this.releaseYear = releaseYear;
	}

	public int getPositives() {
		return positives;
	}

	public void setPositives(int positives) {
		this.positives = positives;
	}

	public int getNegatives() {
		return negatives;
	}

	public void setNegatives(int negatives) {
		this.negatives = negatives;
	}

	public void addComment(String comment){
		if (this.comments == null)
			this.comments = new ArrayList<String>();
		this.comments.add(comment);
	}
	
	/***
	 * Static method to create a movie object
	 * using JSON from Rotten Tomatoes
	 * @param movieJSON - JSON from RottenTomatoes
	 * @return - Converted Movie Object
	 */
	public static Movie makeMovie(JSONObject movieJSON) {
		try {
			Movie movie = new Movie(movieJSON.get("id").toString());
			movie.setName(movieJSON.get("title").toString());
			JSONObject ratings = (JSONObject)movieJSON.get("ratings");
			if (Integer.parseInt(ratings.get("critics_score").toString()) > 0) {
				movie.setScore(Integer.parseInt(ratings.get("critics_score").toString()));
			} else {
				movie.setScore(Integer.parseInt(ratings.get("audience_score").toString()));
			}
			movie.setReleaseYear(Integer.parseInt(movieJSON.get("year").toString()));
			return movie;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	/***
	 * Update comments for a movie.
	 * @param reviewsAsString - Reviews for a movie
	 * as string(JSON Array)
	 */
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
