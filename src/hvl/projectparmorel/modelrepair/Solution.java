package hvl.projectparmorel.modelrepair;

import java.io.File;
import java.util.ArrayList;
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
		sequence = new ArrayList<AppliedAction>();
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
	 */
	public void award() {
		System.out.println("Awarding solution. The following actions are affected:");
		for(AppliedAction appliedAction : sequence) {
			System.out.println("Error: " + appliedAction.getError().getCode());
			System.out.println("\t Context: " + appliedAction.getAction().getContextId());
			System.out.println("\t Action: " + appliedAction.getAction().getCode());
			System.out.println("\t \t Old weight: " + appliedAction.getAction().getWeight());
		}
		rewardCalculator.rewardSolution(this, -1, true);
		System.out.println("After award this is the result:");
		for(AppliedAction appliedAction : sequence) {
			System.out.println("Error: " + appliedAction.getError().getCode());
			System.out.println("\t Context: " + appliedAction.getAction().getContextId());
			System.out.println("\t Action: " + appliedAction.getAction().getCode());
			System.out.println("\t \t New weight: " + appliedAction.getAction().getWeight());
		}
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
