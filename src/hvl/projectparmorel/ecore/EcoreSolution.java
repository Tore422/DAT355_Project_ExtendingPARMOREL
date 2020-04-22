package hvl.projectparmorel.ecore;

import java.util.logging.Logger;

import hvl.projectparmorel.exceptions.DistanceUnavailableException;
import hvl.projectparmorel.modelrepair.Solution;
import it.cs.gssi.similaritymetamodels.EComparator;

public class EcoreSolution extends Solution {

	private Logger logger;
	private double distanceFromOriginal;

	public EcoreSolution() {
		super();
		logger = Logger.getLogger("MyLog");
		distanceFromOriginal = -1;
	}

	/**
	 * Calculates distance from original. If the distance is measurable, it is cached for future calls. 
	 * 
	 * @throws DistanceUnavailableException if something goes wrong with the calculation.
	 */
	@Override
	public double calculateDistanceFromOriginal() throws DistanceUnavailableException {
		if(distanceFromOriginal >= 0) {
			return distanceFromOriginal;
		}
		EComparator comparator = new EComparator(getOriginal().getAbsolutePath(), getModel().getAbsolutePath());
		try {
			distanceFromOriginal = comparator.execute(getOriginal().getAbsolutePath(), getModel().getAbsolutePath());
			logger.info("Calculated the distance between the models to " + distanceFromOriginal);
		} catch (Exception e) {
			throw new DistanceUnavailableException("The distance could not be calculated.", e);
		}
		return distanceFromOriginal;
	}
	
	/**
	 * Resets the cached distance, making the next call to {@link hvl.projectparmorel.ecore.EcoreSolution#calculateDistanceFromOriginal()} calculate the distance again.
	 */
	public void resetDistance() {
		distanceFromOriginal = -1;
	}
}