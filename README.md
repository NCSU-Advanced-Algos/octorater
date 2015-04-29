# octorater
Rate My Movie implementation in Trident


####Steps:
* Navigate to ```storm-starter/src/jvm/storm/starter/trident``` in a terminal
* Clone the project into this repository.
* Add following dependencies to pom.xml
```
  <dependency>
       <groupId>edu.stanford.nlp</groupId>
       <artifactId>stanford-corenlp</artifactId>
       <version>3.5.1</version>
    </dependency>
    <dependency>
       <groupId>edu.stanford.nlp</groupId>
       <artifactId>stanford-corenlp</artifactId>
       <version>3.5.1</version>
       <classifier>models</classifier>
  </dependency>
```
* Run ```mvn package``` in $TRIDENT_SRC
* Start Coding

#### For ElasticSearch
* Download ElasticSearch for ur particular OS and install it from [here](https://www.elastic.co/downloads/elasticsearch)
* Once installed/unzipped , navigate to the bin folder and run elasticsearch. (.bat for windows and .sh for *nix based)
* Run octorater.db.ElasticDB.java to populate your db
* See if it works by navigating to http://127.0.0.1:9200/rotten/words/_search
