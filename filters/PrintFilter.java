/**
 * @author: George Mathew (george2@ncsu.edu)
 */
package storm.starter.trident.octorater.filters;

import storm.starter.trident.octorater.models.Movie;
import storm.starter.trident.octorater.utilities.Utils;
import storm.trident.operation.BaseFilter;
import storm.trident.tuple.TridentTuple;

/**
 * @author
 *  George Mathew (george2)
 *  Kapil Somani  (kmsomani)
 *	Kumar Utsav	  (kutsav)
 *	Shubham Bhawsinka (sbhawsi)
 */
public class PrintFilter extends BaseFilter {

	/* (non-Javadoc)
	 * @see storm.trident.operation.Filter#isKeep(storm.trident.tuple.TridentTuple)
	 */
	@Override
	public boolean isKeep(TridentTuple tuple) {
		Movie movie = (Movie)tuple.get(0);
		System.out.println(movie.getName() + " is rated as " + Utils.categorizeRating(movie.getRating()));
		return true;
	}

}
