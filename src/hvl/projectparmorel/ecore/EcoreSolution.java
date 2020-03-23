package hvl.projectparmorel.ecore;

import java.util.logging.Logger;

import hvl.projectparmorel.modelrepair.Solution;
import it.cs.gssi.similaritymetamodels.EComparator;

public class EcoreSolution extends Solution {

	private Logger logger;

	public EcoreSolution() {
		super();
		logger = Logger.getLogger("MyLog");
	}

	@Override
	public double calculateDistanceFromOriginal() {
		double distance = -1;
		EComparator comparator = new EComparator(getOriginal().getAbsolutePath(), getModel().getAbsolutePath());
		try {
			logger.info("Calculating distance between " + getOriginal().getName() + " and " + getModel().getName());
			distance = comparator.execute(getOriginal().getAbsolutePath(), getModel().getAbsolutePath());
			logger.info("Calculated the distance between the models to " + distance);
		} catch (Exception e) {
			logger.warning("Could not calculate the distance between the models.");
		}
		return distance;
	}
}