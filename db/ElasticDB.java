package storm.starter.trident.octorater.db;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.elasticsearch.action.admin.indices.create.CreateIndexRequestBuilder;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.logging.ESLoggerFactory;
import org.elasticsearch.common.logging.slf4j.Slf4jESLoggerFactory;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;

import storm.starter.trident.octorater.models.Word;
import storm.starter.trident.octorater.utilities.Constants;
import storm.starter.trident.octorater.utilities.POSTagger;
import storm.starter.trident.octorater.utilities.TFIDF;
import storm.starter.trident.octorater.utilities.Utils;


/**
 * @author
 *  George Mathew (george2),
 *  Kapil Somani  (kmsomani),
 *	Kumar Utsav	  (kutsav),
 *	Shubham Bhawsinka (sbhawsi)
 */
public class ElasticDB implements Serializable{
	
	private static final long serialVersionUID = -6045218054015854801L;
	/***
	 * Static Method to create client
	 * @return
	 */
	private static Node node; 
	
	@SuppressWarnings("resource")
	public Client createClient() {
		ESLoggerFactory.setDefaultFactory(new Slf4jESLoggerFactory());
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
	public static Node createNode() {
		if (node == null) {
			ESLoggerFactory.setDefaultFactory(new Slf4jESLoggerFactory());
			node = NodeBuilder.nodeBuilder().client(true).node();
		}
		return node;
	}
	
	
	/***
	 * Create An Index in elastic search
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
	 * Add a bunch of words to the elastic search db of type words.
	 * We use bulk updates to reduce the number of clients created.
	 * @param words - List of Word Objects to be added to the index.
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
		closeClient(client);
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
			word.setTag(POSTagger.getTag(wordName).trim());
			word.setScore(Constants.DEFAULT + delta);
			addWord(word);
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
	/***
	 * Retrieve the total number of comments seen so far.
	 * Used in computing IDF in TF-IDF
	 * @return - Count of total documents
	 */
	public Integer getTotalDocs(){
		Node node = createNode();
		Client client = node.client();
		GetResponse getResponse = client.prepareGet(
				Constants.DB_INDEX,
				Constants.TOTAL_DOCUMENT_TYPE,
				Constants.TOTAL_DOCUMENT_ID).execute().actionGet();
		if (getResponse == null || getResponse.getSource() == null) {
			return null;
		}
		closeClient(client);
		return Integer.parseInt(getResponse.getSource().get("score").toString());
	}
	
	/***
	 * Update the total documents seen so far.
	 * @param documents
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void updateTotalDocs(int documents) {
		Node node = createNode();
		Client client = node.client();
		Integer totalDocs =  getTotalDocs();
		try {
			if (totalDocs == null) {
				Map docMap = new HashMap();
				docMap.put("score",documents);
				IndexRequest indexRequest = new IndexRequest(
						Constants.DB_INDEX,
						Constants.TOTAL_DOCUMENT_TYPE, 
						Constants.TOTAL_DOCUMENT_ID)
		        .source(docMap);
				client.index(indexRequest).actionGet();
			} else {
				UpdateRequest updateRequest;
				
					updateRequest = new UpdateRequest(
							Constants.DB_INDEX,
							Constants.TOTAL_DOCUMENT_TYPE, 
							Constants.TOTAL_DOCUMENT_ID)
							.doc(XContentFactory.jsonBuilder()
									.startObject()
										.field("score", totalDocs+documents)
									.endObject());
				client.update(updateRequest).get();
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Exception occured while updating total document count " + e.getMessage());
		} catch (InterruptedException e) {
			e.printStackTrace();
			System.out.println("Exception occured while updating total document count " + e.getMessage());
		} catch (ExecutionException e) {
			e.printStackTrace();
			System.out.println("Exception occured while updating total document count " + e.getMessage());
		} finally{
			closeClient(client);
		}
	}	
	
	/***
	 * Get the number of documents which contains the word
	 * from elastic search
	 * @param wordName
	 * @return
	 */
	public Word getWordDocFrequency(String wordName){
		Node node = createNode();
		Client client = node.client();
		GetResponse getResponse = client.prepareGet(
				Constants.DB_INDEX,
				Constants.WORD_DOC_FREQ_TYPE,
				wordName).execute().actionGet();
		if (getResponse == null || getResponse.getSource() == null) {
			return null;
		}
		closeClient(client);
		Word word = new Word();
		word.setName(wordName);
		word.setDocCount(Integer.parseInt(getResponse.getSource().get("count").toString()));
		return word;
	}
	/***
	 * Bulk update number of documents for each word.
	 * Performs both update and insert operations.
	 * @param wordFreq - Map indicating the word and their frequency
	 * @param onlyInsert - Flag to indicate only insert.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void bulkUpdateWordDocFrequency(Map<String, Integer> wordFreq, boolean onlyInsert){
		Node node = createNode();
		Client client = node.client();
		BulkRequestBuilder bulkBuilder = client.prepareBulk();
		int bulkSize = 0;
		Map wordFreqMap;
		Word word;
		for (String wordName: wordFreq.keySet()) {
			wordFreqMap = new HashMap();
			// Hack for faster inserts first time
			if (onlyInsert) {
				word = null;
			} else {
				word = getWordDocFrequency(wordName);
			}
			if (word == null) {
				// INSERT
				wordFreqMap.put("count", wordFreq.get(wordName));
				bulkBuilder.add(client.prepareIndex(
						Constants.DB_INDEX,
						Constants.WORD_DOC_FREQ_TYPE,
						wordName).setSource(wordFreqMap));
			} else {
				// UPDATE
				wordFreqMap.put("count", word.getDocCount() + wordFreq.get(wordName));
				bulkBuilder.add(client.prepareUpdate(
						Constants.DB_INDEX,
						Constants.WORD_DOC_FREQ_TYPE,
						wordName).setDoc(wordFreqMap));
			}
			
			bulkSize++;
			if (bulkSize % 1000 == 0) {
				System.out.println("Adding Word Doc Frequency Batch");
				BulkResponse response = bulkBuilder.execute().actionGet();
				if (response.hasFailures()) {
					System.err.println("Error occured whilst adding bulk word document frequencies " + response.buildFailureMessage());
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
		closeClient(client);
	}
	
	/***
	 * Main Function that actually populates your Elastic Search DB.
	 * FYI : Make sure elastic search server is up and running on your local machine.
	 * @param args
	 */
	public static void main(String[] args) {
		ElasticDB elasticDB = new ElasticDB();
		elasticDB.dropIndex();
		elasticDB.createIndex();
		Utils.parseWords();
		TFIDF.buildWordMap(true);
		System.exit(1);
	}
}