package storm.starter.trident.octorater.utilities;


import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.storm.http.HttpResponse;
import org.apache.storm.http.client.methods.HttpGet;
import org.apache.storm.http.impl.client.CloseableHttpClient;
import org.apache.storm.http.impl.client.DefaultHttpClient;
import org.apache.storm.http.util.EntityUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import storm.starter.trident.octorater.db.ElasticDB;
import storm.starter.trident.octorater.models.Movie;
import storm.starter.trident.octorater.models.Word;


/**
 * @author
 *  George Mathew (george2),
 *  Kapil Somani  (kmsomani),
 *	Kumar Utsav	  (kutsav),
 *	Shubham Bhawsinka (sbhawsi)
 */
@SuppressWarnings("deprecation")
public class Utils {
	
	/***
	 * Get Stream URL for movie based on a query
	 * @param query
	 * @param pageLimit
	 * @param pageNumber
	 * @param apiKey
	 * @return  "URL for streaming movies"
	 */
	public static String getMovieStreamURL(String query, int pageLimit, int pageNumber, String apiKey) {
		return "http://api.rottentomatoes.com/api/public/v1.0/movies.json?q="
				+ query + "&page_limit="+pageLimit+"&page=" + pageNumber + "&apikey=" + apiKey; 
	}
	
	/***
	 * Get list of reviews for a movie
	 * @param id - ID for the movie
	 * @param reviewCount - Number of reviews
	 * @param apiKey - APIKey for the user
	 * @return	"URL for reviews of a movie"
	 */
	public static String getReviewsURL(String id, int reviewCount, String apiKey) {
		return "http://api.rottentomatoes.com/api/public/v1.0/movies/"+ id
				+ "/reviews.json?review_type=all&page_limit=" + reviewCount 
				+ "&page=1&country=us&apikey=" + apiKey;
	}
	
	/***
	 * Retrieve id for a certain movie
	 * @param movieName - Name of the movie
	 * @param - number of reviews for movie
	 * @param apiKey - API Key of the user.
	 * @return - Movie ID as a string
	 */
	public static Movie getMovie(String movieName, int reviewCount, String apiKey) {
		movieName = movieName.replaceAll(" ", "+");
		CloseableHttpClient httpclient = new DefaultHttpClient();
		HttpGet httpget = new HttpGet(getMovieStreamURL(movieName, 1, 1, apiKey));
		String responseBody;
		Movie movie;
		try {
			HttpResponse response = httpclient.execute(httpget);
			responseBody = EntityUtils.toString(response.getEntity());
			httpclient.close();
			if (responseBody == null || responseBody.length()==0){
				return null;
			}
			JSONParser parser = new JSONParser();
			JSONArray movies = (JSONArray)((JSONObject) parser.parse(responseBody)).get("movies");
			if (movies.size() == 0) {
				return null;
			}
			JSONObject movieJSON = ((JSONObject)movies.get(0));
			movie = Movie.makeMovie(movieJSON);
			updateReviews(movie, reviewCount, apiKey);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		return movie;
	}
	
	/***
	 * Method to update reviews for a movie.
	 * @param movie - Movie Object containing name, ID and score
	 * @param reviewCount - Number of reviews for a movie 
	 * @param apiKey - APIKEY of the user.
	 * The same movie object is updated with the comments
	 */
	public static void updateReviews(Movie movie, int reviewCount, String apiKey) {
		CloseableHttpClient httpclient = new DefaultHttpClient();
		HttpGet httpget = new HttpGet(getReviewsURL(movie.getId(), reviewCount, apiKey));
		String responseBody;
		try {
			HttpResponse response = httpclient.execute(httpget);
			responseBody = EntityUtils.toString(response.getEntity());
			httpclient.close();
			if (responseBody == null || responseBody.length()==0){
				return;
			}
			movie.updateComments(responseBody);
			// Half a second timeout because the API thresholds only 5 calls per minute.
			Thread.sleep(500);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
	
	/***
	 * Get a stream of movies for a particular query. 
	 * @param query - Query you would be searching for.
	 * @param pageLimit - Number of movies per page.
	 * @param pageNumber - Page Number of search
	 * @param apiKey - APIKEY of user.
	 * @return - List of movies.
	 */
	public static List<Movie> getMovieStream(String query, int pageLimit, int pageNumber, String apiKey) {
		List<Movie> movies = new ArrayList<Movie>();
		CloseableHttpClient httpclient = new DefaultHttpClient();
		HttpGet httpget = new HttpGet(getMovieStreamURL(query, pageLimit, pageNumber, apiKey));
		String responseBody;
		try {
			HttpResponse response = httpclient.execute(httpget);
			responseBody = EntityUtils.toString(response.getEntity());
			httpclient.close();
			if (responseBody == null || responseBody.length()==0){
				return null;
			}
			JSONParser parser = new JSONParser();
			JSONArray moviesJSON = (JSONArray)((JSONObject)parser.parse(responseBody)).get("movies");
			if (moviesJSON.size() == 0)
				return null;
			Movie movie;
			for (Object movieObj: moviesJSON) {
				movie = Movie.makeMovie((JSONObject)movieObj);
				updateReviews(movie, 25, apiKey);
				if (movie.getComments().size() > 0)
					movies.add(movie);
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		return movies;
	}
	
	public static List<Word> parseWords(){
		BufferedReader br =null;
		String line;
		List<Word> words = new ArrayList<Word>();
		Word word = null;
		String[] splits;
		ElasticDB elasticDB = new ElasticDB();
		try {
			br = new BufferedReader(new FileReader(Constants.SUBJECTIVITY_PATH));
			while ((line = br.readLine())!=null) {
				splits = line.split(",");
				word = new Word();
				word.setName(splits[0].trim());
				if (splits[1].equals(Constants.STRONG_SUB) && splits[2].equals(Constants.NEGATIVE)) {
					word.setScore(Constants.BAD);
				} else if (splits[1].equals(Constants.WEAK_SUB) && splits[2].equals(Constants.NEGATIVE)) {
					word.setScore(Constants.AVERAGE);
				} else if (splits[1].equals(Constants.WEAK_SUB) && splits[2].equals(Constants.POSITIVE)) {
					word.setScore(Constants.GOOD);
				} else if (splits[1].equals(Constants.STRONG_SUB) && splits[2].equals(Constants.POSITIVE)) {
					word.setScore(Constants.BEST);
				}
				word.setTag(POSTagger.getTag(word.getName()));
				words.add(word);
			}
			elasticDB.bulkAddWords(words);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
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
		return words;
	}
	
	/**
	 * 
	 * @param score
	 * @return
	 */
	public static int getRank(float score) {
		if (score >= Constants.BEST) {
			return 4;
		} else if (score >= Constants.GOOD) {
			return 3;
		} else if (score >= Constants.AVERAGE) {
			return 2;
		} else {
			return 1;
		} 
	}
	
	public static String categorizeRating(float rating) {
		if (rating >= Constants.BEST) {
			return "VERY GOOD";
		} else if (rating >= Constants.GOOD) {
			return "GOOD";
		} else if (rating >= Constants.AVERAGE) {
			return "AVERAGE";
		} else {
			return "BAD";
		} 
	}
	
	
	public static void main(String[] args) {
		parseWords();
	}
	
}
