

package storm.starter.trident.octorater.spout;

import java.util.Map;

import org.testng.mustache.Value;

import backtype.storm.Config;
import backtype.storm.task.TopologyContext;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Values;
import storm.starter.trident.octorater.models.Movie;
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

	
	/**
	 * 
	 */
	private static final long serialVersionUID = -2400423954846554651L;

	@Override
	public void open(Map conf, TopologyContext context) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see storm.trident.spout.IBatchSpout#emitBatch(long, storm.trident.operation.TridentCollector)
	 */
	@Override
	public void emitBatch(long batchId, TridentCollector collector) {
		Movie movie = new Movie("1","batman", 92);
		movie.addComment("String Node");
		movie.addComment("String Node1");
		movie.addComment("String Node2");
		movie.addComment("String Node3");
		collector.emit(new Values(movie));
	}

	/* (non-Javadoc)
	 * @see storm.trident.spout.IBatchSpout#ack(long)
	 */
	@Override
	public void ack(long batchId) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see storm.trident.spout.IBatchSpout#close()
	 */
	@Override
	public void close() {
		// TODO Auto-generated method stub

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

	/* (non-Javadoc)
	 * @see storm.trident.spout.IBatchSpout#getOutputFields()
	 */
	@Override
	public Fields getOutputFields() {
		return new Fields("text");
	}

}
