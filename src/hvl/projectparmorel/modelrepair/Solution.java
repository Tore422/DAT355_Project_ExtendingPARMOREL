package hvl.projectparmorel.modelrepair;

import java.io.File;
import java.util.List;

import hvl.projectparmorel.general.AppliedAction;
import hvl.projectparmorel.reward.RewardCalculator;

/**
 * @author Magnus Marthinsen
 * @author Angela Barriga Rodriguez, abar@hvl.no 
 * 2019 
 * Western Norway University of Applied Sciences Bergen - Norway
 */
public class Solution implements Comparable<Solution> {
	private int id;
	private List<AppliedAction> sequence;
	private double weight;
	private File model;
	private File original;
	private RewardCalculator rewardCalculator;

	public Solution() {
		super();
		weight = 0.0;
	}

	public Solution(int id, List<AppliedAction> seq, double weight, File model) {
		super();
		this.id = id;
		this.sequence = seq;
		this.weight = weight;
		this.model = model;
	}
	
	/**
	 * Deletes the associated file;
	 */
	public void discard() {
		model.delete();
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public List<AppliedAction> getSequence() {
		return sequence;
	}

	public void setSequence(List<AppliedAction> sequence) {
		this.sequence = sequence;
	}

	public double getWeight() {
		return weight;
	}

	public void setWeight(double weight) {
		this.weight = weight;
	}

	@Override
	public String toString() {
		return "Sequence [id=" + id + ", seq=" + sequence + ", weight=" + weight + "]" + System.getProperty("line.separator")
				+ System.getProperty("line.separator");
	}

	public void setModel(File model) {
		this.model = model;
	}

	public File getModel() {
		return model;
	}

	@Override
	public int compareTo(Solution solution) {
		return Double.compare(weight, solution.getWeight());
	}

	
	/**
	 * Awards the solution by boosting all the actions taken
	 * 
	 * @param shouldSave indicates that the knowledge should be saved to file immediately
	 */
	public void reward(boolean shouldSave) {
		rewardCalculator.rewardSolution(this, -1, shouldSave);
	}

	/**
	 * Set the original model
	 * 
	 * @param originalModel
	 */
	public void setOriginal(File originalModel) {
		original = originalModel;
	}
	
	/**
	 * Gets the original file
	 * @return the original model
	 */
	public File getOriginal() {
		return original;
	}

	/**
	 * Set the reward calculator used to generate the solution
	 * 
	 * @param rewardCalculator
	 */
	public void setRewardCalculator(RewardCalculator rewardCalculator) {
		this.rewardCalculator = rewardCalculator;
	}
}
