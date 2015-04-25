package storm.starter.trident.octorater.db;

import org.elasticsearch.action.admin.indices.create.CreateIndexRequestBuilder;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;

import storm.starter.trident.octorater.utilities.Constants;


/**
 * @author
 *  George Mathew (george2),
 *  Kapil Somani  (kmsomani),
 *	Kumar Utsav	  (kutsav),
 *	Shubham Bhawsinka (sbhawsi)
 */
public class ElasticDB {
	private static TransportClient client = null; 
	
	/***
	 * Static Method to create client
	 * @return
	 */
	public static TransportClient createClient() {
		if (client == null) {
			client = new TransportClient();
			client = client.addTransportAddress(new InetSocketTransportAddress("localhost", 9300));
		}
		return client;
	}
	
	
	public void createIndex() {
		createClient();
		CreateIndexRequestBuilder createIndexRequestBuilder = client.admin().indices().prepareCreate(Constants.DB_INDEX);
		createIndexRequestBuilder.execute().actionGet();
	}
	
	
	public void getScore(){}
	
	
	public static void main(String[] args) {
		new ElasticDB().createIndex();
	}
	
	
}
