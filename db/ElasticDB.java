package storm.starter.trident.octorater.db;

import org.elasticsearch.action.admin.indices.create.CreateIndexRequestBuilder;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexAction;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse;
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
	
	public void dropIndex() {
		createClient();
		
		try {
			DeleteIndexResponse delete = client.admin().indices().delete(new DeleteIndexRequest(Constants.DB_INDEX)).actionGet();
			if (!delete.isAcknowledged())
				System.out.println("Index was not deleted");
		} catch (Exception e) {
			System.out.println("Index does not exist");
			e.printStackTrace();
		}
		
	}
	
	public void getScore(){
		
	}
	
	public static void main(String[] args) {
		ElasticDB elasticDB = new ElasticDB();
		elasticDB.createIndex();
		//`elasticDB.dropIndex();
	}
	
	
}
