

package storm.starter.trident.octorater.spout;

import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

import org.json.simple.JSONObject;


import backtype.storm.Config;
import backtype.storm.task.TopologyContext;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Values;
import storm.starter.trident.octorater.models.Movie;
import storm.starter.trident.octorater.utilities.Utils;
import storm.trident.operation.TridentCollector;
import storm.trident.spout.IBatchSpout;
/**
 * @author
 *  George Mathew (george2),
 *  Kapil Somani  (kmsomani),
 *	Kumar Utsav	  (kutsav),
 *	Shubham Bhawsinka (sbhawsi)
 */
public class RottenSpout implements IBatchSpout {

	private static final long serialVersionUID = -2400423954846554651L;
	
	private String apiKey;
	private String query;
	private LinkedBlockingQueue<Movie> movieQueue;
	private int pageNumber;
	private boolean loopOn;
	private final static int MOVIES_PER_PAGE = 5;
	private final static int RECORDS_PER_BATCH = 2;
	
	public RottenSpout(String query, String apiKey) {
		this.apiKey = apiKey;
		this.query = query;
	}
	
	@Override
	public void open(Map conf, TopologyContext context) {
		movieQueue = new LinkedBlockingQueue<Movie>();
		pageNumber = 0;
		loopOn = true;
		fetchMovies();
	}

	@Override
	public void emitBatch(long batchId, TridentCollector collector) {
		int emitted=0;
		Movie movie;
    	while(emitted<RECORDS_PER_BATCH) {
			movie = movieQueue.poll();
			if (movie != null) {
				collector.emit(new Values(movie));
				emitted++;
			} else {
				if  (loopOn)
					fetchMovies();
			}
    	}
    	if (!loopOn)
    		System.out.println("Query Exausted. Exit and try another query.");
	}

	@Override
	public void ack(long batchId) {
	}

	@Override
	public void close() {
	}

	/***
	 * Get Component Configuration
	 */
	@Override
	public Map getComponentConfiguration() {
		Config ret = new Config();
		ret.setMaxTaskParallelism(1);
		return ret;
	}

	@Override
	public Fields getOutputFields() {
		return new Fields("text");
	}
	
	private void fetchMovies() {
		pageNumber++;
		List<Movie> movies = Utils.getMovieStream(query, MOVIES_PER_PAGE, pageNumber, apiKey);
		if  ((movies == null) || (movies.size() == 0)) {
			loopOn = false;
			return;
		}
		movieQueue.addAll(movies);
	}
}