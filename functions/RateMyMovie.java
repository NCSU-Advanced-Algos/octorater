package storm.starter.trident.octorater.functions;

import java.util.ArrayList;
import java.util.List;

import storm.starter.trident.octorater.models.Movie;
import storm.starter.trident.octorater.utilities.POSTagger;
import storm.trident.operation.BaseFunction;
import storm.trident.operation.TridentCollector;
import storm.trident.tuple.TridentTuple;
import backtype.storm.tuple.Values;

// this function is used to convert the words into their lower cased form.
@SuppressWarnings("serial")
public class RateMyMovie extends BaseFunction {

	private POSTagger tagger;
	
	public RateMyMovie(POSTagger tagger) {
		this.tagger = tagger;
	}
	
	@Override
	public void execute(TridentTuple tuple, TridentCollector collector) {
		Movie movie = (Movie)tuple.get(0);
		List<String> comments = new ArrayList<String>();
		comments = movie.getComments();
		int count = 0;
		int commentRating = 0;
		int movieRating = 0;
		for(String comment : comments){
			commentRating += posTagger.evaluate(comment);
			count++;
		}
		movieRating = commentRating/count;
		movie.setRating(movieRating);
		collector.emit(new Values(movie));
	}
}