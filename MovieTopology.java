package storm.starter.trident.octorater;

import storm.starter.trident.octorater.filters.PrintFilter;
import storm.starter.trident.octorater.spout.RottenSpout;
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
	public static StormTopology buildTopology(LocalDRPC drpc) {
		TridentTopology topology = new TridentTopology();
		IBatchSpout rottenSpout = new RottenSpout();
		topology.newStream("rotten", rottenSpout)
				.each(new Fields("text"), new PrintFilter());
		return topology.build();
	}
	
	public static void main(String[] args) {
		Config conf = new Config();
    	conf.setDebug( false );
    	conf.setMaxSpoutPending( 10 );
    	LocalCluster cluster = new LocalCluster();
    	LocalDRPC drpc = new LocalDRPC();
    	cluster.submitTopology("get_count",conf,buildTopology(drpc));
    	System.out.println("STATUS: OK");
	}
}
