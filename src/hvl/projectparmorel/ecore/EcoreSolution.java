package hvl.projectparmorel.ecore;

import hvl.projectparmorel.modelrepair.Solution;
import it.cs.gssi.similaritymetamodels.EComparator;

public class EcoreSolution extends Solution {
	
	public EcoreSolution() {
		super();
	}

	@Override
	public double calculateDistanceFromOriginal() {
		double distance = 0;
		EComparator comparator = new EComparator(getOriginal().getAbsolutePath(), getModel().getAbsolutePath()); 
		try {
			distance = comparator.execute(getOriginal().getAbsolutePath(), getModel().getAbsolutePath());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return distance;
	}
}