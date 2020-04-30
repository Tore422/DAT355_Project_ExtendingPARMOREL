package hvl.projectparmorel.ecore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import hvl.projectparmorel.exceptions.DistanceUnavailableException;
import hvl.projectparmorel.modelrepair.Solution;
import it.cs.gssi.similaritymetamodels.EComparator;
import it.gssi.cs.quality.ConsoleOutputCapturer;
import it.gssi.cs.quality.QualityEvalEngine;

public class EcoreSolution extends Solution { 

	private Logger logger;

	private List<Double> metrics;
	private double distanceFromOriginal;

	public EcoreSolution() {
		super();
		logger = Logger.getLogger("MyLog");
		metrics = new ArrayList<Double>();

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
			logger.warning("Could not calculate the distance between the models because of a " + e.getClass().getName()
					+ "\nStack trace:\n" + ExceptionUtils.readStackTrace(e));
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

	private List<Double> calculateMetrics() {
		final String qualityModel = String.format("././model/quality.model", 2);
		ConsoleOutputCapturer c = new ConsoleOutputCapturer();
		c.start();
		try {
			new QualityEvalEngine().execute(getModel().getAbsolutePath(), qualityModel);
		} catch (Exception e) {
			e.printStackTrace();
			logger.warning("Could not calculate the metrics");
			metrics = Arrays.asList(-1.0, -1.0, -1.0, -1.0, -1.0);
			return metrics;
		}
		String s = c.stop();
		double d = 0.0;
		String metric = new String();
		String[] arrOfStr = s.split("\r\n", 20);

		for (int i = 9; i < 14; i++) {
			metric = arrOfStr[i].substring(arrOfStr[i].indexOf(":") + 2, arrOfStr[i].length());
			d = Double.valueOf(metric);
			metrics.add(d);
		}
		return metrics;
	}

	@Override
	public double calculateMaintainability() {
		if (metrics == null || metrics.size() == 0)
			calculateMetrics();
		return metrics.get(0);
	}

	@Override
	public double calculateUnderstandability() {
		if (metrics == null || metrics.size() == 0)
			calculateMetrics();

		return metrics.get(1);
	}

	@Override
	public double calculateComplexity() {
		if (metrics == null || metrics.size() == 0)
			calculateMetrics();

		return metrics.get(2);
	}

	@Override
	public double calculateReuse() {
		if (metrics == null || metrics.size() == 0)
			calculateMetrics();

		return metrics.get(3);
	}

	@Override
	public double calculateRelaxation() {
		if (metrics == null || metrics.size() == 0)
			calculateMetrics();

		return metrics.get(4);
	}

}