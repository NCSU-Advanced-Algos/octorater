package storm.starter.trident.octorater;

import storm.starter.trident.octorater.filters.PrintFilter;
import storm.starter.trident.octorater.functions.RateMyMovie;
import storm.starter.trident.octorater.spout.RottenSpout;
import storm.starter.trident.octorater.utilities.Constants;
import storm.starter.trident.octorater.utilities.POSTagger;
import storm.trident.TridentTopology;
import storm.trident.spout.IBatchSpout;
import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.LocalDRPC;
import backtype.storm.generated.StormTopology;
import backtype.storm.tuple.Fields;

/**
 * @author
 *  George Mathew (george2)
 *  Kapil Somani  (kmsomani)
 *	Kumar Utsav	  (kutsav)
 *	Shubham Bhawsinka (sbhawsi)
 */
public class MovieTopology {

	/****
	 * Method Build Topology to create a storm topolgy
	 * @param drpc - A DRPC object that can later be used to run drpc queries. Not in implementation now.
	 * @param query - Query used to stream movies.
	 * @return - A built topology
	 */
	public static StormTopology buildTopology(LocalDRPC drpc, String query) {
		TridentTopology topology = new TridentTopology();
		IBatchSpout rottenSpout = new RottenSpout(query, Constants.API_KEY);
		POSTagger tagger = new POSTagger();
		RateMyMovie movieRater = new RateMyMovie(tagger);
		PrintFilter printFilter = new PrintFilter();
		topology.newStream("rotten", rottenSpout)
				.each(new Fields("movie"), movieRater, new Fields("updatedMovie"))
				.each(new Fields("updatedMovie"), printFilter);
		return topology.build();
	}
	
	public static void main(String[] args) {
		Config conf = new Config();
    	conf.setDebug( false );
    	conf.setMaxSpoutPending( 10 );
    	LocalCluster cluster = new LocalCluster();
    	LocalDRPC drpc = new LocalDRPC();
    	cluster.submitTopology("rotten", conf, buildTopology(drpc, args[0]));
    	System.out.println("STATUS: OK");
	}
}