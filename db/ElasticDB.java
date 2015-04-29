package storm.starter.trident.octorater.db;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.elasticsearch.action.admin.indices.create.CreateIndexRequestBuilder;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;

import storm.starter.trident.octorater.models.Word;
import storm.starter.trident.octorater.utilities.Constants;
import storm.starter.trident.octorater.utilities.POSTagger;
import storm.starter.trident.octorater.utilities.Utils;


/**
 * @author
 *  George Mathew (george2),
 *  Kapil Somani  (kmsomani),
 *	Kumar Utsav	  (kutsav),
 *	Shubham Bhawsinka (sbhawsi)
 */
public class ElasticDB {
	
	/***
	 * Static Method to create client
	 * @return
	 */
	@SuppressWarnings("resource")
	public Client createClient() {
		Client client = new TransportClient().addTransportAddress(new InetSocketTransportAddress("localhost", 9300));
		return client;
	}
	
	/***
	 * Close an existing Elastic Search client
	 * @param client
	 */
	public void closeClient(Client client) {
		client.close();
	}

	/***
	 * Create a Node for connection
	 * @return
	 */
	public Node createNode() {
		Node node = NodeBuilder.nodeBuilder().client(true).node();
		return node;
	}
	
	public void closeNode(Node node) {
		node.close();
	}
	
	/***
	 * Create An Index to store words
	 */
	public void createIndex() {
		Client client = createClient();
		CreateIndexRequestBuilder createIndexRequestBuilder = client.admin().indices().prepareCreate(Constants.DB_INDEX);
		createIndexRequestBuilder.execute().actionGet();
		closeClient(client);
	}
	
	/***
	 * Drop the existing index
	 */
	public void dropIndex() {
		Client client = createClient();
		try {
			DeleteIndexResponse delete = client.admin().indices().delete(new DeleteIndexRequest(Constants.DB_INDEX)).actionGet();
			if (!delete.isAcknowledged())
				System.out.println("Index was not deleted");
		} catch (Exception e) {
			System.out.println("Index does not exist");
			e.printStackTrace();
		}
		closeClient(client);
	}
	
	/***
	 * Add a single word to the elastic search DB
	 * @param word
	 */
	public void addWord(Word word){
		Client client = createClient();
		IndexRequest indexRequest = new IndexRequest(Constants.DB_INDEX, Constants.DB_TYPE);
		indexRequest.source(word.toMap());
		client.index(indexRequest).actionGet();
		closeClient(client);
	}
	
	/***
	 * Add a bunch of words to the elastic search db
	 * @param words
	 */
	@SuppressWarnings("unchecked")
	public void bulkAddWords(List<Word> words) {
		Node node = createNode();
		Client client = node.client();
		BulkRequestBuilder bulkBuilder = client.prepareBulk();
		int bulkSize = 0;
		for (Word word: words) {
			bulkBuilder.add(client.prepareIndex(Constants.DB_INDEX, Constants.DB_TYPE).setSource(word.toMap()));
			bulkSize++;
			if (bulkSize % 1000 == 0) {
				System.out.println("Adding Batch");
				BulkResponse response = bulkBuilder.execute().actionGet();
				if (response.hasFailures()) {
					System.err.println("Error occured whilst adding bulk messages " + response.buildFailureMessage());
				}
				bulkBuilder = client.prepareBulk();
			}
		}
		if(bulkBuilder.numberOfActions() > 0){
	       BulkResponse bulkRes = bulkBuilder.execute().actionGet();
	       if(bulkRes.hasFailures()){
	          System.err.println("##### Bulk Request failure with error: " +   bulkRes.buildFailureMessage());
	       }
	       bulkBuilder = client.prepareBulk();
	    }
		closeNode(node);
	}
	
	/***
	 * Retrieve a word from elastic Search
	 * @param wordName - Word to be retrieved.
	 * @return - Word object of that particular word
	 */
	public Word getWord(String wordName) {
		Client client = createClient();
		SearchResponse response = client.prepareSearch(Constants.DB_INDEX)
								.setTypes(Constants.DB_TYPE)
								.setQuery(QueryBuilders.termQuery("name", wordName))
								.execute().actionGet();
		if (response.getHits().getHits().length == 0) {
			closeClient(client);
			return null;
		}
		Map<String, Object> wordMap = response.getHits().getHits()[0].getSource();
		Word word = new Word();
		word.setID(response.getHits().getHits()[0].getId());
		word.setName(wordMap.get("name").toString());
		word.setTag(wordMap.get("tag").toString());
		word.setScore(Float.parseFloat(wordMap.get("score").toString()));		
		closeClient(client);
		return word;
	}
	
	/***
	 * Update a certain word in elastic search
	 * @param wordName - Word to be updated
	 * @param delta - Margin to increase score by
	 */
	public void updateWord(String wordName, Float delta){
		Word word = getWord(wordName);
		if (word == null) {
			// If word not found create default word.
			word = new Word();
			word.setName(wordName);
			word.setTag(POSTagger.getTag(wordName));
			word.setScore(Constants.DEFAULT);
			return;
		}
		Client client = createClient();
		UpdateRequest request = new UpdateRequest(Constants.DB_INDEX, Constants.DB_TYPE, word.getID());
		try {
			float updatedScore = word.getScore() + delta;
			if (updatedScore > 95)
				updatedScore = 95;
			else if (updatedScore < 5)
				updatedScore = 5;
			request.doc(XContentFactory.jsonBuilder()
					.startObject()
						.field("score", updatedScore)
					.endObject());
			client.update(request).get();
			closeClient(client);
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("Error occured while updating word. Error : " + e.getMessage());
		} catch (InterruptedException e) {
			e.printStackTrace();
			System.err.println("Error occured while updating word. Error : " + e.getMessage());
		} catch (ExecutionException e) {
			e.printStackTrace();
			System.err.println("Error occured while updating word. Error : " + e.getMessage());
		}
	}
	
	
	public static void main(String[] args) {
		ElasticDB elasticDB = new ElasticDB();
		elasticDB.dropIndex();
		elasticDB.createIndex();
		Utils.parseWords();
	}	
}