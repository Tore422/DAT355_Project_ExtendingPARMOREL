package no.hvl.projectparmorel.qlearning.ecore;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

//import org.junit.platform.commons.util.ExceptionUtils;

import it.cs.gssi.similaritymetamodels.EComparator;
import it.gssi.cs.quality.ConsoleOutputCapturer;
import it.gssi.cs.quality.QualityEvalEngine;
import no.hvl.projectparmorel.exceptions.DistanceUnavailableException;
import no.hvl.projectparmorel.qlearning.QSolution;

public class EcoreSolution extends QSolution { 

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
			String stackTrace = getStackTraceAsString(e);
			logger.warning("Could not calculate the distance between the models because of a " + e.getClass().getName()
					+ "\nStack trace:\n" + stackTrace);
					//+ ExceptionUtils.readStackTrace(e)); // Does not work anymore, due to class not accessible?
			throw new DistanceUnavailableException("The distance could not be calculated.", e);
		}
		return distanceFromOriginal;
	}
	
	private String getStackTraceAsString(Exception exception) {
		StringWriter sw = new StringWriter();
		PrintWriter printWriter = new PrintWriter(sw);
		exception.printStackTrace(printWriter);
		String stackTrace = sw.toString();
		return stackTrace;
	}
	
	/**
	 * Resets the cached distance, making the next call to {@link no.hvl.projectparmorel.qlearning.ecore.EcoreSolution#calculateDistanceFromOriginal()} calculate the distance again.
	 */
	public void resetDistance() {
		distanceFromOriginal = -1;
	}

	// TODO: Fix so the method retrieves the metrics as numbers, and does not read from console.
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
		if(arrOfStr.length == 1) { // Mac
			arrOfStr = s.split("\n", 20);
			
			for (int i = 10; i < 15; i++) {
				metric = arrOfStr[i].substring(arrOfStr[i].indexOf(":") + 2, arrOfStr[i].length());
				d = Double.valueOf(metric);
				metrics.add(d);
			}
		} else { // Windows
			for (int i = 9; i < 14; i++) {
				metric = arrOfStr[i].substring(arrOfStr[i].indexOf(":") + 2, arrOfStr[i].length());
				d = Double.valueOf(metric);
				metrics.add(d);
			}
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