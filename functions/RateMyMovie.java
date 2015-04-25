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

	Movie movie;
	POSTagger posTagger = new POSTagger();
	
	@Override
	public void execute(TridentTuple tuple, TridentCollector collector) {
		movie = (Movie)tuple.get(0);
		List<String> comments = new ArrayList<String>();
		comments = movie.getComments();
		int count = 0;
		int commentRating = 0;
		int movieRating = 0;
		for(String comment : comments){
			//commentRating = commentRating + posTagger.evaluate(comment);
			count++;
		}
		movieRating = commentRating/count;
		movie.setRating(movieRating);
		collector.emit(new Values(movie));
	}
}