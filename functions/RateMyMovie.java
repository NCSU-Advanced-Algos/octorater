package storm.starter.trident.octorater.functions;

import java.util.ArrayList;
import java.util.List;

import storm.starter.trident.octorater.models.Movie;
import storm.starter.trident.octorater.utilities.Constants;
import storm.starter.trident.octorater.utilities.POSTagger;
import storm.trident.operation.BaseFunction;
import storm.trident.operation.TridentCollector;
import storm.trident.tuple.TridentTuple;
import backtype.storm.tuple.Values;

/***
 * Storm Function that classifies each comment of a movie as
 * positive or negative based on categorizing each word and
 * weighing it using TFIDF.
 * 
 * @author
 *  George Mathew (george2),
 *  Kapil Somani  (kmsomani),
 *	Kumar Utsav	  (kutsav),
 *	Shubham Bhawsinka (sbhawsi)
 */
public class RateMyMovie extends BaseFunction {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3066944687705357971L;
	
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
		int positiveComment = 0;
		int negativeComment = 0;
		for(String comment : comments){
			score = tagger.evaluate(comment, movie.getScore());
			if (score <= Constants.MIN_THRESHOLD) {
				continue;
			}
			if (score > Constants.SCORE_THRESHOLD) {
				positiveComment++;
			} else {
				negativeComment++;
			}
			commentRating += score;
			count++;
		}
		movieRating = commentRating/count;
		movie.setRating(movieRating);
		movie.setPositives(positiveComment);
		movie.setNegatives(negativeComment);
		tagger.feedback();
		collector.emit(new Values(movie));
	}
}