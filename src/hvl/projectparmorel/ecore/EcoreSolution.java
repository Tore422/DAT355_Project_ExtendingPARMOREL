package hvl.projectparmorel.ecore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import org.junit.platform.commons.util.ExceptionUtils;

import hvl.projectparmorel.modelrepair.Solution;
import it.cs.gssi.similaritymetamodels.EComparator;
import it.gssi.cs.quality.ConsoleOutputCapturer;
import it.gssi.cs.quality.QualityEvalEngine;

public class EcoreSolution extends Solution { 

	private Logger logger;
	private List<Double> metrics;

	public EcoreSolution() {
		super();
		logger = Logger.getLogger("MyLog");
		metrics = new ArrayList<Double>();
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
			logger.warning("Could not calculate the distance between the models because of a " + e.getClass().getName()
					+ "\nStack trace:\n" + ExceptionUtils.readStackTrace(e));
		}
		return distance;
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