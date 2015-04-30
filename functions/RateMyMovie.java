package storm.starter.trident.octorater.functions;

import java.util.ArrayList;
import java.util.List;

import storm.starter.trident.octorater.models.Movie;
import storm.starter.trident.octorater.utilities.Constants;
import storm.starter.trident.octorater.utilities.POSTagger;
import storm.starter.trident.octorater.utilities.Utils;
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
		float commentRating = 0;
		float movieRating = 0;
		float score;
		List<Float> scores = new ArrayList<Float>();
		for(String comment : comments){
			score = tagger.evaluate(comment, movie.getScore());
			if (score <= Constants.MIN_THRESHOLD) {
				continue;
			}
			scores.add(score);
			commentRating += score;
			count++;
		}
		Utils.writeToFile("Actual Score = " + movie.getScore());
		Utils.writeToFile(scores);
		Utils.writeToFile("Median Score = " + Utils.median(scores));
		movieRating = commentRating/count;
		movie.setRating(movieRating);
		tagger.feedback();
		collector.emit(new Values(movie));
	}
}