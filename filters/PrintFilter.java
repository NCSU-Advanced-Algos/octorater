/**
 * @author: George Mathew (george2@ncsu.edu)
 */
package storm.starter.trident.octorater.filters;

import storm.starter.trident.octorater.models.Movie;
import storm.starter.trident.octorater.utilities.Utils;
import storm.trident.operation.BaseFilter;
import storm.trident.tuple.TridentTuple;

/**
 * Default Filter to print a movie.
 * Prints the name, number of comments predicted
 *  positive and number predicted negative
 * 
 * @author
 *  George Mathew (george2)
 *  Kapil Somani  (kmsomani)
 *	Kumar Utsav	  (kutsav)
 *	Shubham Bhawsinka (sbhawsi)
 */
public class PrintFilter extends BaseFilter {

	private static final long serialVersionUID = 1930891314386410366L;

	/* (non-Javadoc)
	 * @see storm.trident.operation.Filter#isKeep(storm.trident.tuple.TridentTuple)
	 */
	@Override
	public boolean isKeep(TridentTuple tuple) {
		Movie movie = (Movie)tuple.get(0);
		int pos = movie.getPositives();
		int neg = movie.getNegatives();
		int total = pos + neg;
		Utils.writeToFile(movie.getName() + " : ");
		Utils.writeToFile(pos + " out of " + total + " rated the movie as positive.");
		Utils.writeToFile(neg + " out of " + total + " rated the movie as negative.");
		Utils.writeToFile(" ");
		return true;
	}

}
