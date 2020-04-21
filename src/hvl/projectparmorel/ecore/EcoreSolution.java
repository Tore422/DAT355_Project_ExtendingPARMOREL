package hvl.projectparmorel.ecore;

import java.util.logging.Logger;

import org.junit.platform.commons.util.ExceptionUtils;

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
	 * Calculates distance from original. If the distance is measurable, it is cached for future calls. Otherwise, it returns -1. 
	 */
	@Override
	public double calculateDistanceFromOriginal() {
		if(distanceFromOriginal >= 0) {
			return distanceFromOriginal;
		}
		double distance = -1;
		EComparator comparator = new EComparator(getOriginal().getAbsolutePath(), getModel().getAbsolutePath());
		try {
			logger.info("Calculating distance between " + getOriginal().getName() + " and " + getModel().getName());
			distance = comparator.execute(getOriginal().getAbsolutePath(), getModel().getAbsolutePath());
			logger.info("Calculated the distance between the models to " + distance);
		} catch (Exception e) {
			logger.warning("Could not calculate the distance between the models because of a " + e.getClass().getName()+ "\nStack trace:\n" + ExceptionUtils.readStackTrace(e));
		}
		distanceFromOriginal = distance;
		return distance;
	}
	
	/**
	 * Resets the cached distance, making the next call to {@link hvl.projectparmorel.ecore.EcoreSolution#calculateDistanceFromOriginal()} calculate the distance again.
	 */
	public void resetDistance() {
		distanceFromOriginal = -1;
	}
}