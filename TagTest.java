package storm.starter.trident.octorater;

import java.io.IOException;
 
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
 
public class TagTest {
    public static void main(String[] args) throws IOException,
            ClassNotFoundException {
 
        // Initialize the tagger
        MaxentTagger tagger = new MaxentTagger();
 
        // The sample string
        String sample = "This is a sample text";
 
        // The tagged string
        String tagged = tagger.tagString(sample);
 
        // Output the result
        System.out.println(tagged);
    }
}
